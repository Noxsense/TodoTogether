package de.nox.todotogether.core.exceptions

import de.nox.todotogether.core.data.User
import de.nox.todotogether.core.data.Expense

public abstract class MoneyException(msg: String) : RuntimeException(msg)

/** This Exception is called if the Expense
 * contains a payer who paid a negative value */
public class NegativeExpenseContributionException(val negativeContributions: Map<User, Int>)
	: MoneyException("Expense contains negative contribtions.")


/** This Exception is called if an Bill is created,
 * but cannot be balanced with the given values. */
public class UnbalancableBillException(val minimum: Int, val shares: Map<User, Double>)
	: MoneyException("Bill cannot be balanced.")


/** This Exception is called if a Balance is created for an Expense
 * which was already paid. */
public class AlreadyPaidException(val alreadyPaid: Set<Expense>)
	: MoneyException("Balance cannot be created for an already paid Expense.")


/** This Exception is called if a balance should have been created for a bill,
 * but the balance payers are not matching the Bill's users. */
public class UnmatchingBalanceUsersException(val missingUsers: Set<User>)
	: MoneyException("Balance's payers do not match with the Bill's users.")
