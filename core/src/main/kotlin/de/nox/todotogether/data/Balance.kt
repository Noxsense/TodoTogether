package de.nox.todotogether.core.data

import de.nox.todotogether.core.exceptions.UnmatchingBalanceUsersException
import de.nox.todotogether.core.exceptions.AlreadyPaidException

/* A MoneyItem. */

/**
 * A Balance is a money item where all sharers of the expense list will
 * finally come to pay or be paid
 * to have an equal amount of expenses (they took part in) until the then.
 * @param bill the bill this balance is a pot for (contains expenses and the users)
 * @param containsPostDeletedExpenses if true, it indicates, that it contains expenses which were deleted after being balanced
 */
public class Balance private constructor (
	id: Long,
	cdate: Long,
	payers: Map<User, Int>,
	val bill: Bill,
	) : MoneyItem(id, cdate, "Balance", payers, description = "TODO") // TODO what to print into the balance text.
	{
		public companion object Factory {
			/** Create a new Balance Item for the money list.
			 * Must contain payers and the bill (with the connected expenses).
			 */
			public fun new(
				payers: Map<User, Int>,
				bill: Bill,
				cdate: Long = System.currentTimeMillis(),
			) = Balance(
				id = MoneyItem.getNextCreationId(),
				cdate = cdate,
				payers = payers,
				bill = bill,
			).also { balance ->
				// Exception: Unmatching users
				if (payers.count() != bill.userCount)
					throw UnmatchingBalanceUsersException(setOf())

				// Exception: Already balanced expenes
				if (bill.expenses.any { it.balanced != null })
					throw AlreadyPaidException(bill.expenses.filter { it.balanced != null }.toSet())

				// everything ok

				// update expenses (now balanced)
				bill.expenses.forEach { e -> e.balanced = balance }

				// add to list.
				balance.addItem()
			}
		}
	}
