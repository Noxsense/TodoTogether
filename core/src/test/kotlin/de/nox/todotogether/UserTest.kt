package de.nox.todotogether.data

import kotlin.test.Test
import kotlin.test.fail
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

import de.nox.todotogether.core.data.User
import de.nox.todotogether.core.exceptions.UserIdException
import de.nox.todotogether.core.exceptions.InvalidUserIdException
import de.nox.todotogether.core.exceptions.AlreadyUsedUserIdException
import de.nox.todotogether.core.exceptions.NoSuchUserException


class UserTest {
    @Test fun invalidUserIds() {
		try {
			User.new(" ", "Empty User ID")
			fail("Invalid User ID: empty (padded) string, no Exception")
		} catch (e: InvalidUserIdException) {
			// pass
		}

		try {
			User.new("1+1", "Not only Alphanumeric User ID")
			fail("Invalid User ID: not a just alphanumeric string, no Exception")
		} catch (e: InvalidUserIdException) {
			// pass
		}

		try {
			User.new("a 2", "Not only Alphanumeric User ID (spaces)")
			fail("Invalid User ID: not a just alphanumeric string, no Exception")
		} catch (e: InvalidUserIdException) {
			// pass
		}
    }

	@Test fun testUserEquality() {
		val name = "User Name"

		// two users with the same display name
		val u1 = User.new("testUserEquality1", name)
		val u2 = User.new("testUserEquality2", name)

		assertEquals(u1.name, u2.name, "Same display name")
		assertNotEquals(u1, u2, "Different ids, smae name.")
		assertNotEquals(u1.hashCode(), u2.hashCode(), "Not equal -> no same hashcode")

		// get the already created user 1 again
		val u3 = User.getUser("testUserEquality1")

		assertEquals(u1.name, u3.name, "Same display name")
		assertEquals(u1, u3)
		assertEquals(u1.hashCode(), u3.hashCode(), "Equal, same hashcode")
	}

	@Test fun deleteUser() {
		val uname = "tobedeleted"
		val uname2 = "justthere"

		val user = User.new(uname)
		val user2 = User.new(uname2)

		val getUser = User.getUser(uname)
		assertEquals(user, getUser)

		val getUserV2 = User.getUser(uname.replaceFirstChar(Char::uppercase)) // still the same
		assertEquals(user, getUser)

		User.delete(uname)
		try {
		User.getUser(uname)
			fail("Should not be able to get a deleted user")
		} catch (e: NoSuchUserException) {
			// pass
		}

		User.getUser(uname2) // just still ok.
	}
}
