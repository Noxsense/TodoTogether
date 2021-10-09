package de.nox.todotogether.data

import kotlin.test.Test
import kotlin.test.fail
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

import kotlin.math.round

import de.nox.todotogether.core.data.MoneyItem
import de.nox.todotogether.core.data.Expense
import de.nox.todotogether.core.data.Bill
import de.nox.todotogether.core.data.Balance
import de.nox.todotogether.core.data.Todo
import de.nox.todotogether.core.data.User

import de.nox.todotogether.core.exceptions.AlreadyPaidException
import de.nox.todotogether.core.exceptions.NegativeExpenseContributionException
import de.nox.todotogether.core.exceptions.UnbalancableBillException
import de.nox.todotogether.core.exceptions.UnmatchingBalanceUsersException

class ExpenseTest {

	/** A task can have the same properties, but is still not equal to another. */
	@Test fun sameButNotEqual() {
		// unique IDs
		val user = User.new("sameButNotEqualExpense")

		val e1 = Expense.new("Name", user, 1)
		val e2 = Expense.new("Name", user, 1)
		val e3 = Expense.new("Name", user, 1)

		// not the same.

		assertNotEquals(e1, e2)
		assertNotEquals(e2, e3)
		assertNotEquals(e1, e3)

		assertEquals("Name", e1.title)
		assertEquals("Name", e2.title)
		assertEquals("Name", e3.title)

		assertEquals(1, e1.price)
		assertEquals(1, e2.price)
		assertEquals(1, e3.price)
	}

	/** An expense should never have a negative price. */
	@Test fun noNegativePriceSingle() {
		val user1 = User.new("noNegativeExpense1")
		try {
			Expense.new("Expense", user1, -1)
			fail("An Expense should not be negative.")
		} catch (e: Throwable) {
			assertTrue(e is NegativeExpenseContributionException, "Expect only NegativeExpenseContributionException")
		}

		/** An expense should never have a negative price. */

		val user2 = User.new("noNegativeExpense2")
		val user3 = User.new("noNegativeExpense3")

		try {
			Expense.new(
				title = "noNegativePriceSummed",
				payers = mutableMapOf(
					user1 to 10,
					user2 to 20,
					user3 to -5, // one negative
				),
			)
			fail("One Payer of the Expense should pay a negative price.")
		} catch (e: Throwable) {
			assertTrue(e is NegativeExpenseContributionException, "Expect only NegativeExpenseContributionException")
		}

		try {
			Expense.new(
				title = "noNegativePriceSummed",
				payers = mutableMapOf(
					user1 to 10,
					user2 to -20,
					user3 to 5,
					// summed -5
				),
			)
			fail("Expense should not be summed to a negative price.")
		} catch (e: Throwable) {
			assertTrue(e is NegativeExpenseContributionException, "Expect only NegativeExpenseContributionException")
		}
	}


	/** Test the Balance and Bill features.
	 * Example:
	 * 1. Entries
	 * Expense.new("Y", payers = { A to 7, B to 3 }, membets = {A, C, D}) // 10
	 * Expense.new("X", payers = { A to 2, B to 3 }, sharers = {B, D}) // 5
	 * Expense.new("Z", payers = { A to 5, B to 10, C to 15, C to 20}, sharers = {A,B,C,D}) // 50
	 *
	 * 3. Gainable Data: Who paid.
	 * => A paid: 7 + 2 + 5 = 14, but excluded to pay later for X
	 * => B paid: 3 + 3 + 10 = 16, but excluded to pay any for Y
	 * => C paid: 15, but does not have to pay for X
	 * => D paid: 20, and will pay parts for everything
	 *
	 * 4l. Gainable Datq: What is shared / How to share
	 * => X, full price: 10, shared price (3): 3.33 (A,C,D)
	 * => Y, full price: 05, shared price (2): 2.5 (B,D)
	 * => Z, full price: 50, shated price (4): 12.5 (A,B,C,D)
	 *
	 * 3. Personal bills, Fix amounts.
	 * A: (3.33+12.5) - (7+3+5)  = 15.83-14 = 1.83
	 * B: (2.5+12.5) - (3+3+10)  = 15.00-16 = -1
	 * C: (3.33+12.5) - (10)     = 15.83-10 = 5.83
	 * D: (2.5+3.33+12.5) - (20) = 18.33-20 = -1.67
	 *
	 * Alternative thoughts:
	 * - BigBill: 3.33 + 2.5 + 12.5 = 18.33
	 * - for A exlude: 2.5 => 15.83
	 * - for C exlude: 2.5 => 15.83
	 * - for B exlude: 3.33 => 15.0
	 * - for D exclude 0 => 18.33
	 *
	 * - Targeted Price Sum : 10 + 5 +50 = 65
	 * - Summed Price: 15.83 + 15.83 + 15.0 + 18.33 = 64.99
	 */
	@Test fun balanceOverview() {
		val u1 = User.new("balanceOverview1", "A")
		val u2 = User.new("balanceOverview2", "B")
		val u3 = User.new("balanceOverview3", "C")
		val u4 = User.new("balanceOverview4", "D")

		val e1 = Expense.new("X", payers = mapOf(u1 to 700, u2 to 300), sharers = listOf(u1, u3, u4)) // 10
		val e2 = Expense.new("Y", payers = mapOf(u1 to 200, u2 to 300), sharers = listOf(u2, u4)) // 5
		val e3 = Expense.new("Z", payers = mapOf(u1 to 500, u2 to 1000,  u3 to 1500, u4 to 2000), sharers = listOf(u1, u2, u3, u4)) // 50
		// sum: 6500 (eg. 65 eur)

		println("\n# Pretty Print Expenses:")
		listOf(e1, e2, e3).forEach { e -> println("* ${e.title}, price: ${e.price}, paid by: ${e.payers}, used by: ${e.sharers}") }

		val bill = Bill(e1, e2, e3)

		// all expenses are inside for this bill

		assertEquals(3,  bill.expenses.count())
		assertTrue(e1 in bill.expenses)
		assertTrue(e2 in bill.expenses)
		assertTrue(e3 in bill.expenses)

		// all users are inside for this bill

		assertEquals(4, bill.userItems.count(), "Four Bills for four sharers.")
		assertEquals(4, bill.userCount, "Four Bills for four sharers.")

		assertEquals(1400, bill.userPaid[u1], "User 1 paid (7.00 + 2.00 + 5.00)")
		assertEquals(1600, bill.userPaid[u2], "User 2 paid (3.00 + 3.00 + 10.00)")
		assertEquals(1500, bill.userPaid[u3], "User 3 paid (15.00)")
		assertEquals(2000, bill.userPaid[u4], "User 4 paid (20.00)")

		// pick some user x expenses

		assertEquals(0 to true, bill.get(u4 to e1), "User 4 paid not for Expense 1, but will pay")
		assertEquals(0 to false, bill.get(u3 to e2), "User 3 paid not for Expense 1, and will not pay")

		val roundTwoToInt = { x: Double -> round(x * 100).toInt() }

		// Billing features.

		assertEquals(183333, roundTwoToInt(bill.sharedPrice), "Shared Price is around 18.3333")

		assertEquals(158333, roundTwoToInt(bill.userPrices[u1]!!), "A needs to pay around 15.83 EUR")
		assertEquals(158333, roundTwoToInt(bill.userPrices[u3]!!), "C needs to pay around 15.83 EUR")
		assertEquals(150000, roundTwoToInt(bill.userPrices[u2]!!), "B needs to pay around 15.00 EUR")
		assertEquals(183333, roundTwoToInt(bill.userPrices[u4]!!), "D needs to pay around 18.33 EUR (full price)")

		assertEquals(650000, roundTwoToInt(bill.userPrices.values.sum()), "Paid Price and summed individual User prices are around equal.")

		assertEquals(18333,  roundTwoToInt(bill.userBalances[u1]!!), "User u1 balance is  15.83 - 14.00 = +1.83 EUR (must pay)")
		assertEquals(-10000, roundTwoToInt(bill.userBalances[u2]!!), "User u2 balance is  15.00 - 16.00 = -1.00 EUR (recv money)")
		assertEquals(8333,   roundTwoToInt(bill.userBalances[u3]!!), "User u3 balance is  15.83 - 15.00 = +0.83 EUR (must pay)")
		assertEquals(-16667, roundTwoToInt(bill.userBalances[u4]!!), "User u4 balance is  18.33 - 20.00 = -1.67 EUR (recv money)")

		assertEquals(0, roundTwoToInt(bill.userBalances.values.sum()), "Balances will fill and reduce the shared pot, to all are even")

		println("\n# Pretty print Bill:\n${
			bill.run {
			userItems.toList().joinToString("\n") { (user, items) ->
				var paid = userPaid[user]
				var price = userPrices[user]
				var balance = userBalances[user]

				("* User: $user"
				+ items.toList().joinToString("\n  * ", "\n  * ", "\n") { (item, usage) ->
					"%-20s (price: %5d, sharers' price: %9.3f), ... paid: %5d (%s)".format(item.title, item.price, item.sharedPrice, usage.first, if (usage.second) "and used it" else "but does not use it")
				}
				+ "  --------\n"
				+ "  * Paid:             %4d.000\n".format(paid)
				+ "  * Should have paid: %8.3f\n".format(price)
				+ "  * Balance:          %8.3f\n".format(balance)
				)
			}
		}}")

		// rounding features.

		val balanced = bill.toBalance(1)
		val balancedPayers = balanced.payers

		println("\n# Pretty Print Balance:")
		println("- $balanced\n${balancedPayers.entries.joinToString("\n"){ (u, v) ->
			"* %-15s %15d (%s)".format(u.name, v, if (v < 0) "received" else "paid")
		}}")

		assertEquals(4, balancedPayers.size, "All members are part of the balance")

		assertEquals(0, balancedPayers.values.sum(), "Balanced Sum is zero.")
		assertEquals(
			balancedPayers.values.sumOf { v -> if (v <= 0) v else 0 },
			-balancedPayers.values.sumOf { v -> if (v > 0) v else 0 },
			"Put into the pot equals take out (-x, +x).")

		assertTrue(183 == balancedPayers[u1] || 184 == balancedPayers[u1], "User 1 should have paid 183ct or 184ct (1.83 EUR) (0.33 to less, maybe 1ct more)")
		assertTrue(83 == balancedPayers[u3] || 84 == balancedPayers[u3], "User 1 should have paid 83ct or 84ct (0.83 EUR) (0.33 to less, maybe 1ct more)")
		assertEquals(-100, balancedPayers[u2], "User 1 should have received -100ct (-1.00 EUR)")
		assertEquals(-167, balancedPayers[u4], "User 1 should have received -167ct (-1.67 EUR)")
	}

	/**
	 * Round the given parts which should be balanced (sum 0) to the next full "minimum value".
	 *
	 * Normal Case:
	 * Eg. {a: -11 eur (1100ct), b: 3.666.. eur, c: 3.666.. eur, d: 3.666... eur }, minimum 1.00 eur
	 * 1. Each one pays the 3 euros (3x 3 eur)
	 * 2. Two euros is not yet balanced
	 * 3. Split the unbalanced value (2 euro) into n * "minumum value" parts (2x)
	 * 4.a) Sort by the rest amounts, Take first n = 2 {b,d} of the back-payers (positive) and let each of them pay the "minimum value"
	 * 5. OUTPUT: { a: -11 EUR, b: 4 EUR, c: 3 EUR, d: 4 EUR }
	 *
	 * Low Case:
	 * If the highested exchanged amount per person is lower than the "minumum value", this bill will not be balanced.
	 * Eg. Round {a:-0.4 EUR, b:0.3 EUR, c: 0.1 EUR}, minimum: 1 EUR,
	 * to {a: -0 EUR,  b: 0 EUR, c: 0 EUR}
	 * As Human example: One Person bought something for 40ct and wants it balanced. But it was agreed, we pay at least one Euro coins,
	 * so the 40ct will be droppped.
	 * OUTPUT: {a: 0, b: 0, c: 0 }
	 *
	 * Low case 2: TODO
	 * - minimum: 7
	 * - values: {a: -3.5, b: -3.5, c: -8, d: 8}
	 * - a's |3.5| is neither closer to zero than to 7 (minumum).
	 * - but the others are more or less balanced
	 * - option 1: drop it ... output: {a: 0, b: 0, c: -7, d: 7} => will result following account balances: {a: -3.5, b: 3.5, c: -1, d: 1}
	 * - option 2: pay it, no doubts, outpit: {a: -7, b: 7, c: -7, d: 7} => will result to: {a: +3.5, b: 3.5, c: -1, d: -1}
	 * - option 3: let them have 0 or full for now and carry that debt away. like a or b but "create new expenses from left"
	 * (option b could be added anyways)
	 *
	 * More Receivers Case:
	 * --- a and b went shopping, each one for a time, c and d must pay
	 * the rest to balance the rest but d does not use everyting of all
	 * or so, etc. (and there is a 7 Unit coin)
	 * Eg. {a: -15, b: -15, c: 20, d: 10}, minumum value: 7
	 * 1. Human solution attempt
	 * 2. The payers put at least their n * "minimum value" parts of the bill into the pot (c: 14, d: 7) => 3x minumum, {b: 6, d: 3 }
	 * 3. The receives take (one after each other) the amount out o the pot (a: -7, b: -7, a: -7) => { a:-1, b:-8 }
	 * 4. B still needs to be paid 8, (at least one minumum amount), C and D together can pay it
	 * 5. Let C pay it: Now it's {a: -1, b: -1, c: -1, d; 2}
	 * 6. C stays in enternal debt to the other three.
	 * 7. OUTPUT: {a: 14, b: 14, c: 21, d: 7}
	 *
	 * The Other Tuner Case - Nope:
	 * ??? no Idea how to turn a receiver into a payer, bc. the payer can then pay the other receiver, for example.
	 * Is it possible to turn a receiver into a payer.
	 * {a: -15, b: 14, c: 2, d: -1} // c could give it to a, but it can also devide it and pay it to both a and d.
	 * => I think there is no situation, a receiver turns into a payer, * bc. if they become a payer,
	 * they received too much and need to pay it to someone else,
	 * but why haven't the previous payer paid the second receiver instead of overflowing the first receiver?
	 */
	@Test fun testFairRounding() {
		val user1 = User.new("Fair1")
		val user2 = User.new("Fair2")
		val user3 = User.new("Fair3")
		val user4 = User.new("Fair4")

		val minimum100 = 100 // 1 euro
		val normalCase = mapOf (
			user1 to -1100.0,
			user2 to 1100.0 / 3,
			user3 to 1100.0 / 3,
			user4 to 1100.0 / 3,
		)

		val roundedNormalCase = MoneyItem.roundBalancedShares(minimum100, normalCase)
		assertEquals(4, roundedNormalCase.size)
		assertEquals(-1100, roundedNormalCase[user1])
		// assertEquals(4, roundedNormalCase[user2])
		// assertEquals(4, roundedNormalCase[user3])
		// assertEquals(4, roundedNormalCase[user4])

		val lowCase = mapOf (
			user1 to -0.4,
			user2 to 0.1,
			user3 to 0.2,
			user4 to 0.1,
		)

		val roundedLowCase = MoneyItem.roundBalancedShares(minimum100, lowCase)

		assertEquals(4, roundedLowCase.size)
		// assertEquals(0, roundedLowCase[user1])
		// assertEquals(4, roundedLowCase[user2])
		// assertEquals(4, roundedLowCase[user3])
		// assertEquals(4, roundedLowCase[user4])

		val minimum7 = 7
		val lowCase2 = mapOf (
			user1 to -3.5,
			user2 to 3.5,
			user3 to -8.0,
			user4 to 8.0,
		)

		val roundedLowCase2 = MoneyItem.roundBalancedShares(minimum7, lowCase2)

		assertEquals(4,    roundedLowCase2.size)
		assertEquals(-minimum7, roundedLowCase2[user3], "They can balance each other (-7 from -8)")
		assertEquals(minimum7, roundedLowCase2[user4], "They can balance each other (+7 from +8)")

		assertEquals(0, roundedLowCase2[user1], "Rounds up -3.5 to 0, so it does not ask for it's payback.")
		assertEquals(0, roundedLowCase2[user2], "With user1 rounding up (or down / not asking for anything), user2 does not have to pay)")


		// -30 to 30, user3 best pays than needed (user4 also in user3 new debt)
		val moreReceivers = mapOf (
			user1 to -15.0,
			user2 to -15.0,
			user3 to 20.0,
			user4 to 10.0,
		)

		val roundedMoreRecvs = MoneyItem.roundBalancedShares(minimum7, moreReceivers)

		assertEquals(4, roundedMoreRecvs.size)
		assertEquals(-2 * minimum7, roundedMoreRecvs[user1])
		assertEquals(-2 * minimum7, roundedMoreRecvs[user2])
		assertEquals( 3 * minimum7, roundedMoreRecvs[user3])
		assertEquals( 1 * minimum7, roundedMoreRecvs[user4])
	}

	/** Removing an expense which was already balanced will make the balance still balancing but for an unknwon amount then. */
	@Test fun removeBalancedExpenses() {
		fail("Needs to be implemented.")
		// XXX
	}

	/** Removing a balance will set all with it resolved expenses to be unpaid again. */
	@Test fun removeBalances() {
		val u1 = User.new("removeBalances1")
		val u2 = User.new("removeBalances2")

		var e1 = Expense.new("e1", u1, 20)
		var e2 = Expense.new("e2", u2, 7)

		// together 27 => 13.5
		// u1: 20 - 6.5
		// u2: 7 + 6.5
		val balance = Bill(e1, e2).toBalance()
		// MoneyItem.delete(balance)
		MoneyItem.delete(balance.id)

		fail("Needs to be implemented.")
		// XXX
	}
}
