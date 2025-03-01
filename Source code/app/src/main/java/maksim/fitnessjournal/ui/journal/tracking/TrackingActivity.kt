package maksim.fitnessjournal.ui.journal.tracking


/***
 * This class handles the tracking screen, opened by clicking
 * any sport button in the Journal tab except Custom Option button
 * @author MakDid7 on GitHub
 ***/

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_tracking.*
import maksim.fitnessjournal.R
import java.text.SimpleDateFormat
import java.util.*

// todo: this week/month/year, you've improved by...

class TrackingActivity : AppCompatActivity() {

    private var disciplineName = ""
    private var unit = ""
    private var total = 0
    private var earliestToPeakPercent = 0.0
    private var minToAvgPercent = 0.0
    private var earliestValue = 0
    private var earliestDateString = ""
    private var lowestValue = 0  // unused
    private var lowestValueDateString = ""  // unused
    private var highestValueDateString = ""
    private var highestValue = 0
    private var average = 0.0
    private var changeDirection = "an increase"

    private lateinit var containers: MutableList<String>

    private val TIMESTAMP_FORMAT_SIMPLE = "dd MMM"  // example:  // 22 Jan
    private val TIMESTAMP_FORMAT_DETAILED = "dd MMM yyyy HH:mm:ss a"  // example: 22 Jan 2023 12:15:34 AM

    // create the components required for saving/retrieving content to/from device
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: Editor


    private fun hideKeyboard() {
        // https://www.tutorialspoint.com/how-to-close-or-hide-the-virtual-keyboard-on-android
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(etNewNumber.applicationWindowToken, 0)
    }


    // adapted from https://stackoverflow.com/a/47588457/19469399
    private fun showKeyboard(context: Context, editText: EditText) {
        editText.requestFocus()
        editText.postDelayed(
            {
                val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.showSoftInput(editText, 0)
            }, 300
        )
    }


    // https://stackoverflow.com/a/67845025/19469399
    private fun String.capitalize() = replaceFirstChar(Char::titlecase)


    private fun String.getContainerValue(): Int {
        return this.substringAfter("[").substringBefore(",").toInt()
    }


    private fun resetActivity() {

        /* after many days, I realized that the separate unconnected lines were
        created because multiple series are added but never removed...
        this line single-handedly solved this huge, seemingly unsolvable problem #~0330 */
        graph.removeAllSeries()

        // reset vars so that their pre-reset values don't show up in the 'My stats' button dialog
        containers = mutableListOf()
        earliestValue = 0
        earliestDateString = ""
        lowestValue = 0
        lowestValueDateString = ""
        highestValue = 0
        highestValueDateString = ""
        earliestToPeakPercent = 0.0
        minToAvgPercent = 0.0
        average = 0.0
        total = 0
        changeDirection = "an increase"

    }


    private fun getFormattedContainerDate(containerIndex: Int, detailed: Boolean): String {

        val container = containers[containerIndex]
        val millis: Long
        // in the initial (inside onCreate()) customizeGraph() call, there's no data which causes error
        try {
            millis = (container.substringAfter(",").substringBefore("]")).toLong()
        } catch (_: NumberFormatException) {
            return ""
        }
        val format = if (detailed) TIMESTAMP_FORMAT_DETAILED else TIMESTAMP_FORMAT_SIMPLE
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        val formattedDate = formatter.format(Date(millis))

        return formattedDate

    }


    private fun customizeGraph() {

        val lightWhite = getColor(R.color.light_white)

        // grid color
        graph.gridLabelRenderer.gridColor = lightWhite

        // customize x-axis
        graph.gridLabelRenderer.horizontalAxisTitle = getString(R.string.graph_horizontal_axis_title)
        graph.gridLabelRenderer.horizontalLabelsColor = lightWhite
        graph.gridLabelRenderer.horizontalAxisTitleColor = lightWhite
        graph.gridLabelRenderer.setHorizontalLabelsAngle(135)
        // configure x-axis label format https://www.youtube.com/watch?v=knlbFXRHff8
        graph.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(containerIndex: Double, isValueX: Boolean): String {
                return if (isValueX) {

                    // make the x-axis element (submission timestamp) as FORMATTED (as opposed to e.g. 1673995588828)
                    getFormattedContainerDate(containerIndex.toInt(), false)

                } else
                    super.formatLabel(containerIndex, false)  // isValueX is always `false` in this case
            }
        }

        // customize y-axis
        graph.gridLabelRenderer.verticalAxisTitle = unit.capitalize()
        graph.gridLabelRenderer.verticalLabelsColor = lightWhite
        graph.gridLabelRenderer.verticalAxisTitleColor = lightWhite

        // both axes
        graph.gridLabelRenderer.padding = 50
        graph.gridLabelRenderer.labelsSpace = 20

    }


    private fun Double.rounded(): Double {
        // return the same Double but rounded to 3 decimal places
        return (this * 1000.0).toInt() / 1000.0
    }


    private fun updateStatsVariables() {
        // todo: fix: when line has negative slope, message is "a decrease of 0%!"
        earliestValue = containers.first().getContainerValue()
        earliestDateString = " on ${getFormattedContainerDate(0, false)}"
        // calculate mean value of all recorded values, rounded to 3 decimal places
        average = (total.toDouble() / containers.size).rounded()
        // calculate the percentage by which user progressed from their first value to their peak value
        earliestToPeakPercent = ((highestValue.toDouble() - earliestValue) / earliestValue * 100.0).rounded()
        // determine whether peak is an increase or a decrease compared to user's first submission
        changeDirection = if (highestValue > earliestValue) "an increase" else "a decrease"
        // minToAvgPercent is X in: average value is X% of earliestValue
        minToAvgPercent = (average * 100.0 / earliestValue).rounded()
    }


    private fun deleteDataPoint(x: Int) {
        // todo

        println("abc=" + prefs.getString(disciplineName, ""))
        // remove the user-selected element
        containers.removeAt(x)

        // since this changed the dataset, update the graph!
        updateGraph()
println("\n\n\n${containers}")
        // save the updated dataset to device
//        editor.putString(disciplineName, containers.toString().)

        println("cde=" + prefs.getString(disciplineName, ""))

    }


    private fun updateGraph() {

        /* thanks to a translucent graph background, I realized series get
        * stacked on top of each other every time updateGraph() is called.
        * Hence, I added this line here.*/
        graph.removeAllSeries()

        // retrieve saved data for the current discipline
        val loadedDataset = prefs.getString(disciplineName, "") ?: return

        // split the data into separate data points/containers (ex: [54,1673995588828])
        containers = loadedDataset.split(" ").toMutableList()

        // if there isn't any data:
        if (loadedDataset == "") {

            resetActivity()

            // hide graph
            graph.visibility = INVISIBLE
            graphTitle.visibility = INVISIBLE

            // exit function
            return
        } else if (containers.size == 1)
            return

        // if app reaches this point, it means there is some data to work with!

        // make the graph visible (it may have been made invisible if there was no data)
        graph.visibility = VISIBLE
        graphTitle.visibility = VISIBLE

        // create an empty data series https://www.youtube.com/watch?v=zbTvJZX0UDk
        val series = LineGraphSeries<DataPoint>()

        // reset total value counter
        total = 0

        // fill the series using existing data points from earlier sessions
        for (i in containers.indices) {

            // y-axis element: user's performance (unit reps)
            val value = containers[i].substringAfter("[").substringBefore(",").toInt()
            val millis = containers[i].substringAfter(",").substringBefore("]")

            if (value > highestValue) {
                highestValue = value
                highestValueDateString =
                    " on " + getFormattedContainerDate(containers.indexOf("[$highestValue,$millis]"), false)
            } else {
                lowestValue = value
                lowestValueDateString =
                    " on " + getFormattedContainerDate(containers.indexOf("[$lowestValue,$millis]"), false)
            }
            // add data point to series
            /* the second great issue with the graph was its series line going out of its borders;
            * the data points did exist, but were present "outside the screen." The intervals
            * between each data point depended on the millis value, which is based on user submission
            * times (=inconsistent). And the x-axis labels (dates) were spread equally on the axis,
            * instead of under their respective data points. 21 Jan I solved the problem by assigning an
            * incremental id (0, 1, 2 ...) to the x-axis values, and using them as list indexes for
            * the containers list to show the actual dates on the x-axis and not the 0, 1, 2, etc. */
            series.appendData(DataPoint(i.toDouble(), value.toDouble()), true, Int.MAX_VALUE)

            total += value

        }

        updateStatsVariables()

        // customize the series line

        // make the line coral-colored
        series.color = getColor(R.color.coral)
        // fill in the space between line and x-axis
        series.isDrawBackground = true
        // set color of that filling
        series.backgroundColor = getColor(R.color.translucent_dark_blue)
        // set line thickness
        series.thickness = 6
        // enable the displaying of data points
        series.isDrawDataPoints = true
        // set their radius
        series.dataPointsRadius = 15f
        // define onClick behavior ("press data point to inspect")
        series.setOnDataPointTapListener { _, dataPoint ->

            // create data point inspection dialog
            val dataPointDialog =
                MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert).create()

            // give it a title
            dataPointDialog.setTitle(R.string.datapoint_dialog_title)

            // define its contents
            val dataPointDateSimple = getFormattedContainerDate(dataPoint.x.toInt(), false)
            val dataPointDateDetailed = getFormattedContainerDate(dataPoint.x.toInt(), true)
            val dataPointReps = dataPoint.y.toInt()
            val message = "x = $dataPointDateDetailed\ny = $dataPointReps $unit"
            val deleteWarningTitle = getString(R.string.delete_data_point_a_confirm_title).replace(
                "***",
                "\n[x = $dataPointDateSimple, y = $dataPointReps $unit]"
            )

            dataPointDialog.setMessage(message)

            // add Got it / 'ok' button to it
            dataPointDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.got_it)) { it, _ -> it.dismiss() }

            // add delete / 'remove datapoint' button to it
            dataPointDialog.setButton(
                AlertDialog.BUTTON_NEUTRAL, getString(R.string.datapoint_dialog_negative)
            ) { _, _ ->

                // create dialog for confirming the deletion of data
                val confirmDialog =
                    MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert).create()
                // give it a title
                confirmDialog.setTitle(deleteWarningTitle)
                // give it a 'cancel' button
                confirmDialog.setButton(
                    AlertDialog.BUTTON_POSITIVE,
                    getString(R.string.cancel)
                ) { _, _ -> }
                // give it a 'delete' button
                confirmDialog.setButton(
                    AlertDialog.BUTTON_NEGATIVE,
                    getString(R.string.yes)
                ) { _, _ ->
                    // when pressed, it deletes that specific data point
                    deleteDataPoint(dataPoint.x.toInt())
                    // since this changed the dataset, update the graph!
                    updateGraph()
                }

                // show the confirmation dialog
                confirmDialog.show()
            }

            dataPointDialog.show()
        }

        // date labels will only be under their respective data points
        graph.gridLabelRenderer.numHorizontalLabels = containers.size

        // todo: make series line touch no sides (minY = 0; maxY = maxY * 1.1; minX = 0; maxX = maxX * 1.1)

        // display the collected data on the graph
        graph.addSeries(series)

    }


    private fun saveNumberToDevice() {

        // retrieve user's typed number
        val userInput = etNewNumber.text.toString()
        // erase user's typed number
        etNewNumber.text.clear()
        // hide keyboard
        hideKeyboard()
        // remove the focus
        etNewNumber.clearFocus()

        // get standardized exact time at which it was submitted
        val currentMillis = System.currentTimeMillis()
        // generate a new dataset piece with user's input and submission timestamp
        val newContainer = "[$userInput,$currentMillis]"
        // retrieve any data recorded previously - if there isn't any, load an empty set
        val loadedDataSet = prefs.getString(disciplineName, "")

        // append the newly generated data to the dataset end (.trim() to account for leading space)
        val updatedDataset = "$loadedDataSet $newContainer".trim()

        // save the typed number to a file in device
        editor.putString(disciplineName, updatedDataset).apply()

    }


    private fun createStatsDialog() {

        // create user's stats dialog
        val statsDialog = MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert).create()

        // put together dialog contents
        val message =
            "\nYou started with $earliestValue $unit$earliestDateString, " +
                    "and peaked at $highestValue $unit$highestValueDateString." +
                    "\n\nThat is $changeDirection of about $earliestToPeakPercent%! " +
                    "\n\nYour total data average is ~$average $unit, " +
                    "which is about $minToAvgPercent% of where you started." +
                    "\n"

        // give it a title
        statsDialog.setTitle(R.string.tracking_progress_dialog_title)

        // define its contents
        statsDialog.setMessage(message)  // todo: highlight the figures https://stackoverflow.com/q/7221930/19469399

        // add 'got it' button to it
        statsDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.got_it)) { _, _ -> }

        // add 'remove all graph data' button to it
        statsDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.clear_data)) { _, _ ->

            // create a nested confirmation dialog
            val confirmDialog =
                MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert).create()

            // give it a title
            confirmDialog.setTitle(
                getString(R.string.delete_all_discipline_data_confirm_title).replace(
                    "***",
                    disciplineName
                )
            )

            // add 'got it' button
            confirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.cancel)) { _, _ -> }
            // add 'delete' button that deletes all recorded data for current discipline
            confirmDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.remove_it)) { _, _ -> clearData() }

            // show the confirmation dialog
            confirmDialog.show()
        }

        statsDialog.show()

    }


    private fun clearData() {
        // replace data of corresponding discipline with empty string
        // (clears ALL recorded data points for the specific discipline)
        editor.putString(disciplineName, "").apply()
        // reset variables stored in activity
        resetActivity()
        updateGraph()
    }


    private fun maximizeGraphSpace() {

//        // hide the user input field and labels
//        tvTrackingPromptStart.visibility = View.GONE
//        tvTrackingPromptEnd.visibility = View.GONE
//        etNewNumber.visibility = View.GONE
//
//        // fill the newly free space with the graph
//        // todo: constrain graph height to Return/My progress buttons
//     might be useful https://stackoverflow.com/a/16456117/19469399
//     source for the 2 lines below https://stackoverflow.com/a/49641851/19469399
//        val layoutParams = graph.layoutParams
//        graph.layoutParams = layoutParams
    }


    // define what happens when the tracking Activity loads
    override fun onCreate(savedInstanceState: Bundle?) {

        // initiate/load/create the actual layout/UI
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        // initialize the components required for saving/retrieving content to/from device
        prefs = this.getPreferences(Context.MODE_PRIVATE)
        editor = prefs.edit()

        // open keyboard
        showKeyboard(applicationContext, etNewNumber)

        // retrieve everything from the data container that was passed from journal menu
        val promptStart = intent.getStringExtra("PROMPT_START")
        val promptEnd = intent.getStringExtra("PROMPT_END")
        disciplineName = intent.getStringExtra("DISCIPLINE_NAME").toString()
        unit = intent.getStringExtra("DISCIPLINE_UNIT").toString()

        // display the retrieved text on screen
        if ((promptStart != null) && (promptEnd != null)) {
            tvTrackingPromptStart.text = promptStart
            tvTrackingPromptEnd.text = promptEnd
        }

        // I moved all the graph customizations here because there are a lot of them
        customizeGraph()

        // load graph of existing data
        updateGraph()

        // configure 'close' button functionality
        clCloseTracking.setOnClickListener {
            // remove current Activity from the activity stack (takes user back to journal menu in MainActivity)
            finish()
        }

        // configure number edit field functionality
        etNewNumber.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->

            // if Done/Enter button pressed:
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                // get submitted number
                val submittedText = etNewNumber.text.toString()

                // if user pressed Done before typing anything:
                if (submittedText.isBlank())
                    return@OnEditorActionListener true
                // else if user typed 0:
                else if (submittedText.toInt() == 0) {  // added .toInt() conversion to account for repeated zeros (e.g. 000)
                    // show warning
                    Toast.makeText(applicationContext, "Cannot submit 0.", Toast.LENGTH_LONG).show()
                    // no further code within this edit field will be executed
                    return@OnEditorActionListener true
                }

                // save user's typed number to device
                saveNumberToDevice()

                // fill the newly available screen space with the graph
                maximizeGraphSpace()
            }

            // now that there's a new data point, update graph
            updateGraph()

            // if there's only 1 data point (in which case, no point in plotting a graph):
            // ... also, GraphView does not plot singular points - only updates axis ranges
            if (containers.size == 1) {

                // graph isn't displayed -> there's no visual feedback
                // -> hence, create a Toast to tell user their that their number was saved
                Toast.makeText(
                    applicationContext, getString(R.string.first_submission_toast_message), Toast.LENGTH_LONG
                ).show()
            }
            return@OnEditorActionListener true
        })

        // configure 'progress' button
        clOpenProgress.setOnClickListener {
            createStatsDialog()
        }

//        clearData()  // mark

    }
}