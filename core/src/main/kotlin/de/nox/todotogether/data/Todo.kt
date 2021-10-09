package de.nox.todotogether.core.data

import kotlin.math.max

import de.nox.todotogether.core.exceptions.CyclingTodoParentException
import de.nox.todotogether.core.exceptions.InvalidTodoParentException
import de.nox.todotogether.core.exceptions.TodoEmptyTitleException
import de.nox.todotogether.core.exceptions.TodoNestingException

typealias TodoList = MutableList<Todo>

private object TodoLibrary {
	// TODO secure accesses
	/** All active Todo tasks. */
	val todos: TodoList = mutableListOf()

	/** Internal counter. */
	var count: Long = 0

	/** Archived Todo tasks. */
	val archived: TodoList = mutableListOf()
}

/**
 * Task
 * It can be nested and will group more todos.
 * @param id own id
 * @param maintainers users who are permitted to edit the task, if none is given, everyone can edit.
 * @param title first/primary representation (eg. in todolists)
 * @param cdate creation date (default: now)
 * @param parent id of the parent task (default: no parent)
 * @param level counter of the tree depth, eg. one parent on root (0) level makes own level 1, own children will have level 2, ...
 * @param description description an text (default: empty)
 * @param ddate optional due date (default: no due date)
 * @param progress progress of the task (default: 0 %)
 */
public class Todo private constructor(
	val id: Long,
	val maintainers: MutableList<User>,
	val title: String,
	val cdate: Long = System.currentTimeMillis(),

	parent: Todo? = null,

	var description: String = "",
	var ddate: Long? = null,
	progress: Int = 0,
	)
	{
		public var parent: Todo? = parent
			set(newParent) {
				if (newParent == null) {
					// reset indentation
					field = newParent
					// ok.
				}
				else if (newParent == this) {
					// cannot be its own parent
					throw InvalidTodoParentException()
					// aborted.
				} else {
					/* Check if parent is actually a subtask of this todo task. */
					var newGrandparent = newParent.parent
					while (newGrandparent != null) {
						if (newGrandparent == this@Todo) {
							// parent-to-be is already a subtask => cycle
							throw CyclingTodoParentException()
						}
						newGrandparent = newGrandparent.parent
					}

					/* No cycle detected. */
					field = newParent
					// ok.
				}
			}

		/** Return nesting level (parents until root). */
		public val level: Int
			get () = parent?.level?.plus(1) ?: 0 // [parent's level + 1] or [0]

		public var progress: Int = progress
			set(value) {
				field = max(0, value) // never negative.
			}

		companion object {
			/* List all active todos. */
			fun allActive() : List<Todo>
				= TodoLibrary.todos.toList()

			/* List all active todos. */
			fun allArchived() : List<Todo>
				= TodoLibrary.archived.toList()

			/**
			* Create a new Todo task, proxy for @see new().
			* @param creator (initial single maintainer of the task)
			*/
			fun new(
				creator: User,
				title: String,
				cdate: Long = System.currentTimeMillis(),

				parent: Todo? = null,

				description: String = "",
				ddate: Long? = null,
				progress: Int = 0,
			) : Todo = Todo.new(
				maintainers = mutableListOf(creator),
				title = title,
				cdate = cdate,
				parent = parent,
				ddate = ddate,
				description = description,
				progress = progress)

			/**
			* Create a new Todo task.
			* This automatically adds each created todo to the TodoLibrary
			* and increases the overall created Todo tasks count (eg. for IDs).
			*/
			fun new(
				maintainers: MutableList<User>,
				title: String,
				cdate: Long = System.currentTimeMillis(),

				parent: Todo? = null,

				description: String = "",
				ddate: Long? = null,
				progress: Int = 0,
			) : Todo = Todo(
				id = TodoLibrary.count,
				maintainers = maintainers,
				title = when {
					title.trim().length < 1 -> throw TodoEmptyTitleException()
					else -> title.trim()
				},
				cdate = cdate,

				// default first, if given set after creation.
				parent = null,

				description = description,
				ddate = ddate,
				progress = progress,
			).also { todo ->
				/* If parent is given, add as child to the given parent.
				 * set own nesting level to parent.level + 1 */
				todo.parent = parent
				// (may throw an exception and abort adding to library.)

				// also add to all active todos.
				TodoLibrary.todos.add(todo)

				// increase counter
				TodoLibrary.count += 2
			}
		}

		/** Check equality of this todo taks to any object.
		* It equals another todo task, if the id is the same.
		*/
		override fun equals(other: Any?) : Boolean
			= other != null && other is Todo && other.id == this.id

		/** String representation: Simple Title. */
		override fun toString() = ">".repeat(level) + title

		/** Create a new task with the same properties and creation date to today. */
		public fun copy() : Todo
			= Todo.new(
				maintainers = this.maintainers,
				title = this.title,
				cdate = System.currentTimeMillis(),

				parent= this.parent,

				description = this.description,
				ddate = this.ddate,
				progress = this.progress,
			)

		/** Toggle state of this task's archiviation status.
		 * @return true of the tasks ends to be in the archivate list.
		 */
		fun toggleArchivate() : Boolean
			= if (this in TodoLibrary.todos) {
				TodoLibrary.todos.remove(this)
				TodoLibrary.archived.add(this)
				// TODO add subtasks to the archive as well.
				true
			} else {
				TodoLibrary.archived.remove(this)
				TodoLibrary.todos.add(this)
				// TODO move subtasks to the active todos
				false
			}
	}

