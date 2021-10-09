package de.nox.todotogether.core.data

import de.nox.todotogether.core.exceptions.UserIdException
import de.nox.todotogether.core.exceptions.InvalidUserIdException
import de.nox.todotogether.core.exceptions.AlreadyUsedUserIdException
import de.nox.todotogether.core.exceptions.NoSuchUserException

private object UserLibrary {
	// TODO secure accesses
	val users: MutableMap<String,User> = mutableMapOf()
}

/**
* A user.
*/
public data class User private constructor (
	val id: String,
	var name: String
)
{
	public companion object {
		/** List all users. */
		fun listAll() : List<User>
			= UserLibrary.users.values.toList()

		/** Find and delete the user. */
		fun delete(id: String) {
			val lowercase = id.lowercase()
			val success = UserLibrary.users.remove(lowercase)
			if (success == null) {
				throw NoSuchUserException(lowercase)
			}
		}

		/** Check if the user with the given id exists. */
		fun exists(id: String) : Boolean
			= UserLibrary.users.containsKey(id.lowercase())

		/**
		* This will retrieve the User with the requested ID.
		* If no such User exists, it will throw an Exception.
		* @throws NoSuchUserException if the requesting ID is not used by any user
		*/
		fun getUser(id: String) : User
			= UserLibrary.users.get(id.lowercase()) ?: throw NoSuchUserException(id)

		/**
		* This may create a new User.
		* No User is created, if the preferredId is already used.
		* @param id a preferred ID for the new User, valid only alphanumeric strings (transformed to lowercase)
		* @param name (mutable) String representation of the User
		* @throws InvalidUserIdException if the wanted ID is not valid
		* @throws AlreadyUsedUserIdException if the wanted ID is already used by an existing user
		*/
		fun new(id: String, name: String = id) : User
			= id.lowercase().let { lowerId ->
				when {
					lowerId.length < 1 -> throw InvalidUserIdException(id) // accept only non-empty strings
					lowerId.any { c -> !c.isAlphaNumeric() } -> throw InvalidUserIdException(id) // accept only alpha numeric chars for id
					UserLibrary.users.containsKey(lowerId) -> throw AlreadyUsedUserIdException(id)

					// register new user ID. (id surely to lowercase)
					else -> User(lowerId, name)
						.also { newUser ->
							UserLibrary.users.put(lowerId, newUser)
						}
				}
			}


		private fun Char.isAlphaNumeric() : Boolean
			= when (this.lowercase().first()) {
				in ('0' .. '9') -> true
				in ('a' .. 'z') -> true
				else -> false
			}
	}

	/** Show the current String representation of the User. */
	override fun toString() = name

	/** User equals another object, if it is a No-Null User, with the same ID. */
	override fun equals(other: Any?) : Boolean
		= other != null && other is User && this.id.equals(other.id)

	/** User's hashcode depends on their ID. */
	override fun hashCode() : Int
		= this.id.hashCode()
}

