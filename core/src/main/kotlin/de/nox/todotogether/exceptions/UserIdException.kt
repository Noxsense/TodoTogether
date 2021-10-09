package de.nox.todotogether.core.exceptions

abstract public class UserIdException(val userId: String, msg: String) : RuntimeException(msg)

/** Exception if the string is invalid as a User ID. */
public class InvalidUserIdException(userId: String)
	: UserIdException(userId, "User ID ($userId) is unvalid")

/** Exception if the string cannot be used for a new User as ID, because it is already used. */
public class AlreadyUsedUserIdException(userId: String)
	: UserIdException(userId, "User ID ($userId) already used")

/** Exception if there is no User with the given ID. */
public class NoSuchUserException(userId: String)
	: UserIdException(userId, "No such user with ID $userId")
