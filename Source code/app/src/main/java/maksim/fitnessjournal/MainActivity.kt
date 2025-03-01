package maksim.fitnessjournal


/***
 * This class handles the launching process of the app
 * @author MakDid7 on GitHub
 ***/

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MotionEvent
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_journal.*
import maksim.fitnessjournal.databinding.ActivityMainBinding
import kotlin.math.abs


/*
todo: increase navbar item sizes https://stackoverflow.com/questions/31204320/how-can-i-change-the-navigationviews-item-text-size/31206095#31206095 https://learn.microsoft.com/en-us/answers/questions/580584/customizing-menu-items-in-navigationview
todo: fix enabling/disabling user interaction (tried https://stackoverflow.com/a/36927858/19469399, didn't work)
todo: swiping right or pressing back button = open navbar
*/

class MainActivity : AppCompatActivity() {

    private var x1 = 0F
    private var x2 = 0F
    private val MIN_DISTANCE = 150

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    private fun showcaseApp(drawerLayout: DrawerLayout, prefs: SharedPreferences) {

        // create welcome dialog
        val welcomeDialog =
            MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert).create()

        // give it a title
        welcomeDialog.setTitle(R.string.tutorial_dialog_title)

        // define its contents
        welcomeDialog.setMessage(getString(R.string.tutorial_start_dialog_message))

        // add button to it
        welcomeDialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            getString(R.string.tutorial_start_dialog_button_text)
        ) { _, _ ->

            // when the button is clicked, do this:

            // todo: disable window interaction

            // to show the user the journal, scroll to the bottom of the journal menu...
            journalMainView.postDelayed({ journalMainView.smoothScrollBy(0, journalMainView.height) }, 500)

            // ... and scroll back up.
            journalMainView.postDelayed({ journalMainView.smoothScrollBy(0, -journalMainView.height) }, 1500)

            // open navigation drawer so that the user instantly gets acquainted with the app's navigation
            journalMainView.postDelayed({ drawerLayout.openDrawer(GravityCompat.START) }, 2500)

            val endWelcomeDialog =
                MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert).create()

            // give it a title
            endWelcomeDialog.setTitle(R.string.tutorial_dialog_title)

            // define its contents
            endWelcomeDialog.setMessage(getString(R.string.tutorial_end_dialog_message))

            // add button to it
            endWelcomeDialog.setButton(
                AlertDialog.BUTTON_POSITIVE,
                getString(R.string.ok)
            ) { _, _ -> }

            // show tutorial end dialog
            journalMainView.postDelayed({ endWelcomeDialog.show() }, 3250)


            // tell the end device that it won't be the user's first time launching the app anymore.
            prefs.edit().putBoolean("FIRST_LAUNCH", false).apply()

            // todo: enable window interaction

        }

        welcomeDialog.show()

    }

// todo test this
    // adapted from https://stackoverflow.com/a/22091163
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> x1 = event.x
            MotionEvent.ACTION_UP -> {
                x2 = event.x
                val deltaX: Float = x2 - x1
                if (abs(deltaX) > MIN_DISTANCE) {
                    Toast.makeText(this, "left2right swipe", Toast.LENGTH_SHORT).show()
                } else {
                    // consider as something else - a screen tap for example
                }
            }
        }
        return super.onTouchEvent(event)
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout = binding.drawerLayout
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_journal, R.id.nav_tips, R.id.nav_notes
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val prefs = this.getPreferences(Context.MODE_PRIVATE)
        val firstLaunch = prefs.getBoolean("FIRST_LAUNCH", true)
        // if app was just launched for the first time ever on a user's device:
        if (firstLaunch)
            showcaseApp(drawerLayout, prefs)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        // configure the search button
        val menuItem = menu.findItem(R.id.app_bar_search)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)

        // todo: add search functionality
        //  ideas:
        //  1. highlight JournalFragment TextViews with matching text
        //  2. only render JournalFragment TextViews with matching text

        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}