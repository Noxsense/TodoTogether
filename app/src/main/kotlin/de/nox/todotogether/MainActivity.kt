package de.nox.todotogether

import android.content.Context
import android.os.Bundle
import android.widget.ListView
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.PagerAdapter

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.Tab

import de.nox.todotogether.core.data.*
import de.nox.todotogether.core.exceptions.*

class MainActivity : AppCompatActivity() {

	private lateinit var context: Context

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		context = this

		val user1 = User.new("noxsense", "Houng Heinzel")
		val user2 = User.new("mheinzel", "Heinrich Heinzel")
		val user3 = User.new("haskello", "Haskell Heinzel")

		Todo.new(
			creator = user2,
			title = "TODO First",
			ddate = 0,
			description = "",
			)

		val todo2 = Todo.new(
			creator = user3,
			title = "TODO Second",
			ddate = 0,
			description = "",
			)

		(1..5).map { Todo.new(
			creator = user1,
			title = "TODO 2.$it",
			ddate = 0,
			description = "",
			parent = todo2,
			)
		}

		val todo3 = Todo.new(
			creator = user1,
			title = "TODO 2.Third",
			ddate = 0,
			description = "",
			parent = todo2,
			)

		(1..5).map { Todo.new(
			creator = user1,
			title = "TODO 2.3.$it",
			ddate = 0,
			description = "",
			parent = todo3,
			)
		}

		Todo.new(
			creator = user1,
			title = "TODO Fourth",
			ddate = 0,
			description = "",
			parent = null,
			)

		Todo.new(
			title = "TODO 2.3.Late",
			ddate = 0,
			creator = user1,
			description = "",
			parent = todo3,
			)

		// findViewById<ListView>(R.id.todolist).apply {
		// 	adapter = ArrayAdapter(
		// 		context,
		// 		android.R.layout.simple_list_item_1,
		// 		Todo.allActive(),
		// 		)
		// }

		val tablayout = findViewById<TabLayout>(R.id.tablayout)

		tablayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
			override fun onTabSelected(tab: TabLayout.Tab?) {
				// handle tab select
			}

			override fun onTabReselected(tab: TabLayout.Tab?) {
				// handle tab reselect
			}

			override fun onTabUnselected(tab: TabLayout.Tab?) {
				// handle tab unselect
			}
		})

		// badges

		// using tabs with ViewPager (androidx.viewpager.widget.ViewPager)
		// in order to
		// - create TabItems based on the number of pages and their titles, etc
		// - synchronize the selected tab and tab indecator position with page swipes
		// PagerAdapter to override getPageTitle()

		/*
		tablayout.setupWithViewPager(object: PagerAdapter() {
			override fun getCount() : Int
				= 1

			override fun isViewFromObject(p0: View, p1: Any) : Boolean
				= false

			override fun getPageTitle(position: Int) : CharSequence? {
				// return tab text label for position
				return "Tab text Label for position $position"
			}
		})
		*/

		val todoFragment = TodoFragment()
		val moneyFragment = MoneyFragment()

		supportFragmentManager.beginTransaction().apply { // this: FragmentTransaction
			replace(R.id.viewPager, todoFragment)
		}

		// should be protective.
		// MoneyItem.getNextCreationId()
	}
}
