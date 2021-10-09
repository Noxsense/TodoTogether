package de.nox.todotogether.core.data

import kotlin.math.floor
import kotlin.math.roundToInt

import de.nox.todotogether.core.exceptions.UnbalancableBillException
import de.nox.todotogether.core.exceptions.UnmatchingBalanceUsersException
import de.nox.todotogether.core.exceptions.AlreadyPaidException

/**
 * Balance Bill.
 * This bill is created for selected expenses.
 * It will list who paid what, and how much should be paid, with eclusions.
 */
public class Bill(
	vararg expenses: Expense,
) {
	val expenses: Set<Expense> = expenses.asList().toSet() // cast from <out Expense>

	/** Number of users who are participating. */
	val userCount: Int by lazy { userItems.count() }

	/** For each User:
	 * What have they paid, what are they using
	 * (for each expense of the Bill). */
	val userItems: Map<User, Map<Expense, Pair<Int, Boolean>>> = run {
		// User to Expense: How much are paid (also zero), Are they using it (true/false).
		val usersData = mutableMapOf<User, MutableMap<Expense, Pair<Int, Boolean>>>()

		// 1. for each user: what did they pay
		// 2. for each user: what did they use, what to exlude (more especially).
		// 3. (maybe) for each user: what are they co-using?

		// payers and sharers.
		expenses.forEach { e ->
			// all paying users
			e.payers.forEach { (user, paid) ->
				val using = user in e.sharers

				// init list
				if (!usersData.containsKey(user))
					usersData[user] = mutableMapOf()

				usersData.get(user)!!.put(e, paid to using)
			}

			// all using users who did not pay
			(e.sharers - e.payers.keys).forEach { user ->
				if (!usersData.containsKey(user))
					usersData[user] = mutableMapOf()

				// uses the expense, but did not pay
				usersData.get(user)!!.put(e, 0 to true)
			}
		}

		// finalize
		usersData
	}

	/** Sum of how much each user has paid in the end. */
	val userPaid: Map<User, Int>
		= userItems.mapValues { (_, bills) -> bills.values.sumOf { (paid, _) -> paid } }

	/** Get the price each member pays without the personal exclusions.
	  * This is a weighted price per sharer (items.price divided by sharers.count).
	  * Each personal price of the user is the shared price minus their exclusions.
	  */
	val sharedPrice: Double = expenses.sumOf(Expense::sharedPrice)

	/** Price for each user: Shared price - exclusions. */
	val userPrices: Map<User, Double>
		= userItems.keys.associateWith { user ->
			this.sharedPrice - expenses.sumOf { if (this.get(user to it).second) 0.0 else it.sharedPrice }
		}

	/** Balances for each user: What do they need to pay minus what they already paid.
	 * If the amount is negative, the user will get money back (from the pot),
	 * otherwise the user pays into that pot. */
	val userBalances: Map<User, Double>
		= userPrices.mapValues { (user, price) -> price - (userPaid.get(user) ?: 0) }

	/** Get the personal items for the user. */
	public operator fun get(user: User) = userItems.get(user)

	/** Get the paid price and the information if user
	 * shares the price later on for a user and a given expense.
	 *
	 * If the user is not found, it will return (0 to false),
	 * because the user paid nothing and will also not share the price.
	 */
	public operator fun get(userExpense: Pair<User, Expense>) : Pair<Int, Boolean>
		= userExpense.let { (u, e) -> (get(u)?.get(e)) ?: (0 to false) }

	/** Two Bills are equal, if they are baout the same expenses. */
	public override fun equals(other: Any?)
		= other != null && other is Bill
		&& other.expenses.count() == this.expenses.count()
		&& other.expenses.all { o -> o in this.expenses } // contains all
		&& this.expenses.all { t -> t in other.expenses } // contains all

	// TODO public override fun hashCode() : Int

	/** Title of the Bill. */
	public override fun toString() = "Bill for $expenses"

	/** Create a new Balance Item, which balances this bill.
	 * @param minimum an agreed value the users will transfer and cannot be broken to lesser pieces
	 * @param payers manually inserted, what each user will pay or receive, default
	 *
	 * @see MoneyItem.roundBalancedShares()
	 * @throws UnbalancableBillException if the value cannot be balanced
	 * @throws AlreadyPaidException if at least one of the expenses is already balanced.
	 */
	public fun toBalance(minimum: Int = 1, payers: Map<User, Int> = MoneyItem.roundBalancedShares(minimum, userBalances) ) : Balance
		= Balance.new(payers, this)
}
