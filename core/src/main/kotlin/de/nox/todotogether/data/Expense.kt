package de.nox.todotogether.core.data

import de.nox.todotogether.core.exceptions.NegativeExpenseContributionException

/* A MoneyItem. */

/** An expense is a never negative MoneyItem which describes the group's
 * afforded price for a service, object or any other item which needed to be paid.
 * @param id to identify saved expenses
 * @param cdate creation date
 * @param title title of this expense item
 * @param payers User(s) who paid for the item (and their parts)
 * @param sharers User(s) who use and must finally pay the price the item (maybe with balances)
 * @param balanced the optionally connected balance item, that if not null, also hints it is balanced
 * @param description description of the item
 * @param (connectedTask) connected task of the expense (nullable)
 */
public class Expense private constructor (
	id: Long,
	cdate: Long,
	title: String,
	payers: Map<User, Int>,

	val sharers: List<User>,

	var balanced: Balance?,

	description: String, // mutable
	var connectedTask: Todo?, // mutable

	) : MoneyItem(id, cdate, title, payers, description)
	{
		/** Get the summed price of the final Expense. */
		val price: Int by lazy {
			payers.values.sum()
		}

		val sharedPrice: Double by lazy {
			price.toDouble() / sharers.count()
		}

		public companion object Factory {
			/** Create a new Expense with just a price and one payer.
			 * @param cdata creation date (default now)
			 * @param title of the new expense item
			 * @param price the one payer paid
			 * @param payer single payer
			 * @param sharers user(s) who will share the price (default: payers)
			 * @param description note about the expense
			 * @param connectedTask optionally connected task (none)
			 * @throws NegativeExpenseContributionException
			 */
			public fun new(
				title: String,
				payer: User,
				price: Int,
				cdate: Long = System.currentTimeMillis(),
				sharers: List<User> = mutableListOf(payer),
				description: String = "",
				connectedTask: Todo? = null,
			) : Expense
				= new(
					cdate = cdate,
					title = title,
					payers = mutableMapOf(payer to price),
					sharers = sharers,
					description = description,
					connectedTask = connectedTask,
				)

			/** Create a new Expense.
			 * @param title of the new expense item
			 * @param payers user(s) who paid and their paid part
			 * @param cdata creation date (default now)
			 * @param sharers user(s) who will share the price (default: payers)
			 * @param description note about the expense
			 * @param connectedTask optionally connected task (none)
			 * @throws Exception
			 */
			public fun new(
				title: String,
				payers: Map<User, Int>,
				cdate: Long = System.currentTimeMillis(),
				sharers: List<User> = payers.keys.toList(),
				balanced: Balance? = null,
				description: String = "",
				connectedTask: Todo? = null,
			) : Expense
				= Expense(
					id = MoneyItem.getNextCreationId(),
					cdate = cdate,
					title = title,
					payers = payers,
					sharers = sharers,
					balanced = balanced,
					description = description,
					connectedTask = connectedTask,
				).also { expense ->
					if (payers.values.any { it < 0 })
						throw NegativeExpenseContributionException(payers.filter { (_,v) -> v < 0 })

					// if all ok
					expense.addItem()
				}
		}

		override fun toString() : String
			= "$title ($price)"

		override fun equals(other: Any?) : Boolean
			= other != null && other is Expense && other.id == this.id

		override fun hashCode() : Int
			= this.id.hashCode()
	}
