package de.nox.todotogether.core.exceptions

public class TodoEmptyTitleException()
	: RuntimeException("Todo must have a non empty title")

abstract public class TodoNestingException(msg: String) : RuntimeException(msg)

/** Exception the Parent is the same as the child. */
public class InvalidTodoParentException()
	: TodoNestingException("Invalid Todo Parent: Todo cannot be its own parent")

public class CyclingTodoParentException()
	: TodoNestingException("Invalid Todo Parent: Todo cannot have a subtask as parent")

