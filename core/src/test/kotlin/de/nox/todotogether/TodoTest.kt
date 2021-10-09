package de.nox.todotogether.data

import kotlin.test.Test
import kotlin.test.fail
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

import de.nox.todotogether.core.data.Todo
import de.nox.todotogether.core.data.User
import de.nox.todotogether.core.exceptions.CyclingTodoParentException
import de.nox.todotogether.core.exceptions.InvalidTodoParentException
import de.nox.todotogether.core.exceptions.TodoEmptyTitleException
import de.nox.todotogether.core.exceptions.TodoNestingException

class TodoTest {

	/** A task can have the same properties, but is still not equal to another. */
	@Test fun sameButNotEqual() {
		val title =  "The Only Title"
		val creator = User.new("sameButNotEqual")
		val parent = Todo.new(creator, "Parent")
		val ddate = System.currentTimeMillis() + 3600000
		val description = "Lorem Ipsum ... and so On."
		val progress = 42

		val task = Todo.new(
			title = title,
			creator = creator,
			parent = parent,
			ddate = System.currentTimeMillis() + 3600000,
			description = description,
			progress = progress,
		)

		assertEquals(title, task.title)
		assertTrue(creator in task.maintainers && task.maintainers.size == 1)
		assertEquals(parent, task.parent)
		assertEquals(ddate, task.ddate)
		assertEquals(description, task.description)
		assertEquals(progress, task.progress)

		val copied = task.copy()

		// properities match
		assertEquals(title, copied.title)
		assertTrue(creator in copied.maintainers && copied.maintainers.size == 1)
		assertEquals(parent, copied.parent)
		assertEquals(ddate, copied.ddate)
		assertEquals(description, copied.description)
		assertEquals(progress, copied.progress)

		// not the same
		assertNotEquals(task, copied)

		task.progress += 1

		// independent
		assertNotEquals(task.progress, copied.progress)
	}

	/** Invalid Names: Just the empty or almost empty string. (and null)
	* Names with not-only whitespaces will be trimmed and be accepted.
	*/
	@Test fun emptyTodoName() {
		val user = User.new("emptyTodoName")

		try {
			Todo.new(title = "", creator = user)
			fail("Invalid Todo Name Exception not thrown (empty string).")
		} catch (e: TodoEmptyTitleException) {
			// passed
		}

		try {
			Todo.new(title = "    \n    ", creator = user)
			fail("Invalid Todo Name Exception not thrown (almost empty / only whitespaces).")
		} catch (e: TodoEmptyTitleException) {
			// passed
		}

		Todo.new(title = "   Content\n with text", creator = user)
	}

    /** Task cannot be it's own parent. */
    @Test fun todoOwnParent() {
		val user = User.new("todoOwnParent")
		try {
			var ownParent = Todo.new(user, "Own parent")
			ownParent.parent = ownParent
			fail("Passed being the own parent todo initiation")
		} catch (e: InvalidTodoParentException) {
			// passed
		}
	}

	/** A task cannot be the child of it's own subtasks. */
	@Test fun cyclingParent() {
		val user = User.new("cyclingParent")

		val todoRoot = Todo.new(user, "Root Task")
		assertTrue(todoRoot.parent == null)

		// valid child-parent relation
		val todoChild = Todo.new(user, "Child", parent = todoRoot)
		assertNotNull(todoChild.parent, "Child should have a parent assigned")
		assertEquals(todoRoot, todoChild.parent, "Child's parent is Root")

		assertEquals(1, todoChild.level, "Valid Child has a higher nesting level (should be 1)")

		// valid grand child (root -> child -> child)
		val todoGrandChild = Todo.new(user, "Grandchild", parent = todoChild)
		assertEquals(todoChild, todoGrandChild.parent)

		assertEquals(todoChild.level + 1, todoGrandChild.level, "Valid Grandchild has a higher nesting level (should be 2)")

		// cycle: root -> child -> child -> root
		try {
			todoRoot.parent = todoGrandChild
			fail("Tasks's subtask/decendant cannot be the parent of the task, but it was accepted")
		} catch (e: CyclingTodoParentException) {
			// passed
		}
    }
}
