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


class StorageManagerTest {
    @Test fun testToJsonAndBack() {
		val userJson = """
			"id": "username",
			"name": "User Name",
		"""

		val storageManager = StorageManagerTest()

		// val userRead = storageManager.readUserFromStorable(userJson)

		// println("User Read: $userRead")
    }
}
