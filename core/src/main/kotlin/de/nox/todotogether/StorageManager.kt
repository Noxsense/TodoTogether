package de.nox.todotogether.core

import java.io.File

import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.databind.ObjectMapper
// import com.fasterxml.jackson.ObjectMapper

// TODO do it with jackson.
// TODO read jackson with kotlin: https://www.bezkoder.com/kotlin-convert-json-to-object-jackson/

import de.nox.todotogether.core.data.Todo
import de.nox.todotogether.core.data.User
import de.nox.todotogether.core.data.MoneyItem

public class StorageManager {

	private class Storable(val string: String);

	private val mapper = ObjectMapper()

	private fun writeUserToStorable(user: User) : Storable  {
		// write a user to a storable structure

		var jsonResult = mapper.writeValueAsString(user)
		return Storable(jsonResult)
	}

	private fun readUserFromStorable(storable: Storable) : User {
		// parse a single todo from file
		return mapper.readValue(storable.string, User::class.java)
	}

	private fun writeTodoToStorable(todo: Todo) : Storable  {
		// write a todo to a storable structure
		return Storable(mapper.writeValueAsString(todo))
	}

	private fun readTodoFromStorable(storable: Storable) : Todo {
		// parse a single todo from file
		// XXX
		// return Todo.new()
		return mapper.readValue(storable.string, Todo::class.java)
	}

	private fun writeMoneyItemToStorable(moneyItem: MoneyItem) : Storable  {
		// write a moneyitem to a storable structure
		// XXX moneyitem: Expense | Balance .... similar, but different.
		return Storable(mapper.writeValueAsString(moneyItem))
	}

	private fun readMoneyItemFromStorable(storable: Storable) : MoneyItem {
		// parse a single todo from file
		// XXX
		return mapper.readValue(storable.string, MoneyItem::class.java)
	}
}
