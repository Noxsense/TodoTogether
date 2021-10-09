package de.nox.todotogether.core.data

import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlin.math.floor

/**
 * Private (supportive) library to store all the valid items
 * and count the items, which were ever created (even if deleted).
 */
private object MoneyLibrary {
	/** All active Expense tasks. */
	val items: MutableList<MoneyItem> = mutableListOf()

	/** Internal counter. */
	var count: Long = 0
}

/**
 * MoneyItem.
 * This can be an expense or a balance act.
 * @param id own id
 * @param title title of the paid item
 * @param payers users who paid and what they paid (in cent)
 * @param cdate creation date (default: now)
 * @param sharers users who wants to share the price then
 * @param description more information about the paid item
 */
public abstract class MoneyItem protected constructor(
	val id: Long,
	val cdate: Long,
	val title: String,
	val payers: Map<User, Int>,
	var description: String, // mutable
	){
		companion object {
			/** Get an imuatble list of all items. */
			public fun listAll() : List<MoneyItem>
				= MoneyLibrary.items.toList()

			/** For MoneyItem creators: Get the next ID for the next item. */
			// XXX get protected / no access from not MoneyItem
			// protected
			fun getNextCreationId() : Long
				= MoneyLibrary.count

			/** Delete an Expense with the given ID.
			* If there is none, nothing will be changed.
			* XXX handle removed balance (remove link from balanced) and removed item (balance then 'contains missing item').
			*/
			public fun delete(id: Long)
				= MoneyLibrary.items.run {
					find { e -> e.id == id }?.let { toRemove ->
						when (toRemove) {
							is Expense -> toRemove.balanced?.apply<Balance> {
								// postRemoveExpese(toRemove)
							}
							// XXX
							// is Balance -> toRemove.expenses.forEach<Expense> { balancedExpense ->
							// 	// all expenses are now unbalanced again
							// 	balancedExpense.balanced = null // reset
							// }
						}

						// remove from list
						remove(toRemove)
					}
				}

			/**
			 * Round the given balanced shares, so that only the "minimum" value will be moved around.
			 * Balanced means that the sum is zero, so everything put into the pot will also be used to pay the receivers.
			 *
			 * @param minimum like a minimum coin or value that moves around and cannot be broken
			 *
			 * @param shares the actual "real" shares each user should have
			 *               paid, where users with negative values will receive money and the
			 *               others have to pay that amount.
			 *
			 * @return new map of rounded and balanced shares.
			 */
			public fun roundBalancedShares(minimum: Int, shares: Map<User, Double>) : Map<User, Int> {
				// TODO algorithm to share like that.

				val checkZeroSum = shares.entries.sumOf { it.value }

				/* Check if the users are balanced (at least by 0.000). */
				if (((checkZeroSum) * 1000).roundToInt() != 0)
					throw RuntimeException("The users are already not balanced: $shares")

				/* First step - Use Best Balance Attempt:
				 * Let each member receive or pay their closest amount to the minimum.
				 * This may also lead to overpaying or underpaying (normal rounding).
				 *
				 * For positive values (the payer): Find their closest payable amount (can be more or less than initally asked).
				 * For negative values (the receivers): Find their closest returnable amount (that will be the amount they should expect).
				 */
				var rounded: MutableMap<User, Int> = shares.mapValues {
					(it.value / minimum).roundToInt() * minimum
				}.toMutableMap()

				/* Check for if the collected pot (by payers) is to much or to less. */
				val (roundedRecvPot, roundedPayersPot) = rounded.entries.fold((0 to 0)) { base, acc -> when {
					acc.value < 0 -> (base.first + acc.value) to base.second
					else -> base.first to (base.second + acc.value)
				}}

				val unbalanced = roundedRecvPot + roundedPayersPot // {what to pay} - {what can be paid}

				/* Check if the unbalanced parts must and can be balanced with the minimum value.
				 * If the unbalanced amount can be balanced, pick {n} users
				 * that will be picked to pay/get back the minimum value. */
				if ((unbalanced != 0) && (abs(unbalanced / minimum) >= 1)) {
					var n = unbalanced / minimum

					if (n < 0) {
						/* Users need to pay more: Let the $n most debtful users pay the minimum. */
						rounded.entries
							.filter { it.value > 0 } // filter all payers
							.sortedByDescending { (u,v) -> (shares[u]!! - v) } // sort by rest debt to pay
							.take(-n)
							.forEach { (u, v) -> rounded[u] = v + minimum }
					}
					else {
						/* Users already paid too much: Return the minimum to the n most debtless users. */
						rounded.entries
							.sortedBy { it.value }
							.dropWhile { it.value <= 0 } // do not change the receivers
							.take(n)
							.forEach { (u, v) -> rounded[u] = v - minimum }
					}
				}

				val roundedCheckZeroSum = rounded.values.sumOf { it }

				if (roundedCheckZeroSum != 0)
					throw RuntimeException("User shares could not be balanced and rounded (so far: $rounded = ${rounded.values.sumOf { it }}).")

				return rounded
			}
		}

		/** Common String representation for MoneyItem is its title.
		 */
		override fun toString() : String = title

		/** Check equality of this todo taks to any object.
		* It equals another todo task, if the id is the same.
		*/
		override fun equals(other: Any?) : Boolean
			= other != null && other is MoneyItem && (other.id == this.id)

		/** Add a new Item to the MoneyLibrary.items
		 * and increase the number of ever created (valid) items.
		 * Attention: It is upon the caller to validate the correctness of the items
		 * before adding them to the library.
		 * TODO workaround for not protected companion object functions.
		 */
		protected fun addItem() {
			MoneyLibrary.items.add(this)
			MoneyLibrary.count += 1
		}
	}
