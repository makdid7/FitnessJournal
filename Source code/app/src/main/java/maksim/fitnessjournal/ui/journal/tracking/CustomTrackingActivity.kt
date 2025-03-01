package maksim.fitnessjournal.ui.journal.tracking


/***
 * This class handles the custom tracking screen, opened by
 * clicking the Custom Option button in the Journal tab
 * @author MakDid7 on GitHub
 ***/

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_custom_tracking.*
import kotlinx.android.synthetic.main.activity_tracking.*
import maksim.fitnessjournal.R

class CustomTrackingActivity : AppCompatActivity() {

    private var activityName: String = ""
    private var activityNameInKeyFormat: String = ""
    private var activityUnit: String = ""
    private var imagePath: String = ""

    private val PICTURE_REQUEST_OK: Int = 200

    private lateinit var prefs: SharedPreferences
    private lateinit var editor: Editor


    // user's soft keyboard opens when this function is called
    private fun showKeyboard(context: Context, editText: EditText) {
        editText.requestFocus()
        editText.postDelayed(
            {
                val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.showSoftInput(editText, 0)
            }, 300
        )
    }


    // returns `true` if user typed something in 'name' and 'unit' fields and chose an image
    private fun allInfoIsPresent(): Boolean {
        println(activityName)
        println(activityUnit)
        println(imagePath)
        return etCustomTrackingName.text.trim().isNotBlank() &&
                etCustomTrackingUnit.text.trim().isNotBlank() &&
                imagePath != ""
    }


    // enable the 'save' button if user provided all required info
    private fun updateSaveButton() {

        // if user gave all required info to create their custom discipline:
        if (allInfoIsPresent()) {

            // scroll to the top, where the 'save' button is
            customTrackingMainView.smoothScrollTo(0, 0)  // todo: fix - doesn't scroll.

            // since scrolling doesn't work, I'll at least include a toast
            Toast.makeText(
                applicationContext,
                "All boxes filled! Scroll up to the Save button.", Toast.LENGTH_LONG
            ).show()

            // make 'save' button clickable
            clSave.isClickable = true
            // make 'save' button green
            clSave.backgroundTintList = getColorStateList(R.color.green)


        } else {  // if any info is missing:
            // make 'save' button unclickable
            clSave.isClickable = false
            // make 'save' button grayed-out
            clSave.backgroundTintList = getColorStateList(R.color.light_gray)
        }

    }


    // persistently check whether user filled out required info. was made to remove redundant duplicate code.
    private fun EditText.listenForAllInfoIsPresent() {

        this.addTextChangedListener(object : TextWatcher {

            // when user types/removes any text into any text field:
            override fun onTextChanged(content: CharSequence, start: Int, before: Int, count: Int) {

                // persistently update the app's remembered name and unit for writing to device later
                activityName = etCustomTrackingName.text.toString()
                activityUnit = etCustomTrackingUnit.text.toString()

                // the enabled/disabled state of the 'save' button is updated accordingly
                updateSaveButton()
            }

            // these two are unused (but they must be initialized, otherwise the program does not work)
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })

    }


    private fun String.existsAsActivity(): Boolean {
        // user previously created activity with same name
        val userAlreadyCreatedThis = prefs.contains(this)
        // user is trying to create activity that comes with the app (e.g. long jump, shot put)
        val defaultActivitiesContainThis = getDefaultActivityKeys().contains(this)

        return userAlreadyCreatedThis.or(defaultActivitiesContainThis)
    }


    // this was also made to combat code duplicacy
    private fun EditText.scrollToNextStepOnSubmission(
        scroller: ScrollView,
        originContainer: ConstraintLayout,
        targetContainer: ConstraintLayout,
    ) {

        this.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->

            // if user presses Enter before typing anything, no further action within this input field will happen
            if (this.text.trim().isBlank())
                return@OnEditorActionListener false

            // if Enter/Done pressed while current input field is in focus
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                activityNameInKeyFormat = activityName.replace(" ", "_").uppercase()


                // if user typed name of activity that already exists
                if (activityNameInKeyFormat.existsAsActivity()) {

                    // a warning is generated and the provided data isn't saved
                    Toast.makeText(
                        applicationContext,
                        "$activityName already exists :)",
                        Toast.LENGTH_LONG
                    ).show()

                    return@OnEditorActionListener false
                }

                // automatically scroll so that the next required input field is in the screen center
                scroller.smoothScrollBy(0, (targetContainer.y - originContainer.y).toInt())

                // deselect current field
                originContainer.clearFocus()

                // select the next field
                targetContainer.requestFocus()
            }
            return@OnEditorActionListener true
        }
        )

    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // check whether image request was successful
        if (resultCode == RESULT_OK && requestCode == PICTURE_REQUEST_OK) {

            // get path of user's selected image
            val selectedImageUri: Uri? = data?.data
            imagePath = selectedImageUri.toString()
            // update the preview image in the layout
            imgBtnUploadImage.setImageURI(selectedImageUri)

            // try to update 'save' button state
            updateSaveButton()

        }

    }


    // https://www.geeksforgeeks.org/how-to-select-an-image-from-gallery-in-android/
    // this function is triggered when the 'upload image' button is pressed
    private fun openImageChooser() {
        // create an instance of the
        // intent of the type image
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, getString(R.string.image_chooser_title)), PICTURE_REQUEST_OK)
    }


    private fun getDefaultActivityKeys(): List<String> {
        return listOf(
            getString(R.string.skip_rope),
            getString(R.string.high_jump),
            getString(R.string.hit_ups),
            getString(R.string.javelin_throw),
            getString(R.string.shot_put),
            getString(R.string.push_ups),
            getString(R.string.kick_ups),
            getString(R.string._400m),
            getString(R.string._800m),
            getString(R.string.long_jump),
            getString(R.string.custom_option_btn_text)
        )
    }


    // define what happens when the tracking Activity loads
    override fun onCreate(savedInstanceState: Bundle?) {

        // initiate/load/create the actual layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_tracking)

        // create the components required for saving/retrieving content to/from device
        prefs = this.getPreferences(Context.MODE_PRIVATE)
        editor = prefs.edit()

        // give focus to 'activity name' edit field
        etCustomTrackingName.requestFocus()
        // keyboard opens
        showKeyboard(this, etCustomTrackingName)

        // make it so that the 'save' button becomes clickable when user submits sport name, unit, and image
        etCustomTrackingName.listenForAllInfoIsPresent()
        etCustomTrackingUnit.listenForAllInfoIsPresent()

        // make it so that when info is submitted, the next step is centered on the user's display
        etCustomTrackingName.scrollToNextStepOnSubmission(
            customTrackingMainView,
            clCustomTrackingName,
            clCustomTrackingUnit
        )
        etCustomTrackingUnit.scrollToNextStepOnSubmission(
            customTrackingMainView,
            clCustomTrackingUnit,
            clCustomTrackingUploadImage
        )

        // configure close button functionality
        clCloseCustomTracking.setOnClickListener {
            // remove current Activity from the activity stack (take user back to journal menu in MainActivity)
            finish()
        }

        // "saving image picked from gallery for future use" https://stackoverflow.com/a/18668433/19469399

        // configure 'save' button functionality
        imgBtnUploadImage.setOnClickListener {

            val picturePath = prefs.getString(activityName, "")

            // if a picture path exists:
            if (picturePath != "" && picturePath != null) {

                // create preview of chosen image inside the 'upload' button
                imgBtnUploadImage.setImageBitmap(BitmapFactory.decodeFile(picturePath))

                // check whether 'save' button should be enabled now
                updateSaveButton()

            } else {  // if user didn't select a picture yet:

                // open image chooser for the user to find and select the image they want to use
                openImageChooser()
            }

        }

        // configure 'save' button functionality
        clSave.setOnClickListener {

            // save the provided sport info to a file in device
            editor.putString(getString(R.string.custom_option_key), "[$activityName,$activityUnit,$imagePath]").apply()

            // remove current Activity from the activity stack (take user back to journal menu in MainActivity)
            finish()
        }

        // set the 'save' button's initial state to non-clickable
        /* defining this before its setOnClickListener or in the xml doesn't work,
        since setOnClickListener overrides its clickability, setting it to `true` */
        clSave.isClickable = false

    }
}