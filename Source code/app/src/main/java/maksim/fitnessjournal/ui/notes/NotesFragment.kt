package maksim.fitnessjournal.ui.notes


/***
 * This class handles the Notes tab
 * @author MakDid7 on GitHub
 ***/

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import maksim.fitnessjournal.R
import maksim.fitnessjournal.databinding.FragmentNotesBinding


class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    // attach custom keyboard-opening functionality to text edit fields
    private fun EditText.showKeyboard() {
        post {
            requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }


    // this is used to convert text size dimensions to their SP (scaled pixels, the preferred text size format) values
    private fun Float.toSp(): Int {
        return (this / resources.displayMetrics.scaledDensity).toInt()
    }


    // this is executed every time the "My Notes" tab is opened
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // create the components required for saving content to device
        val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // store references to ui elements in variables for more readable code
        val notesSpace: EditText = binding.etNotes
        val textSizePicker: NumberPicker = binding.npNotesTextSize

        // configures the text size picker
        textSizePicker.minValue = 5  // disables scrolling to/typing a number smaller than 5
        textSizePicker.maxValue = 40  // disables scrolling to/typing a number greater than 40
        textSizePicker.wrapSelectorWheel = false  // disables infinite scrolling
        // define what happens when user scrolls the text size picker
        textSizePicker.setOnValueChangedListener { _, _, newVal ->
            // change the notes content's text size
            notesSpace.textSize = newVal.toFloat()
            // save the user's selected text size to a file in device and label it "NOTES_TEXT_SIZE"
            /* for some reason, by default, textSize was saved as 3*(textSize),
               hence the division by 3. */
            /* while division by 3 makes it less inaccurate, the textSize saved into
               sharedPreferences is still inaccurate (some devices way more than others though.) */
            /* fixed: the problem happened because notesSpace.textSize is a value of pixels, but I needed sp units*/
            /* solution from https://stackoverflow.com/questions/6263250/convert-pixels-to-sp/9219417#9219417 */
            editor.putInt("NOTES_TEXT_SIZE", notesSpace.textSize.toSp()).apply()
        }

        // retrieve user's notes; if there aren't any, use an empty string
        val loadedNotesContent = prefs.getString("NOTES_CONTENT", "")
        // put the retrieved text in the notes space
        notesSpace.setText(loadedNotesContent)
        // retrieve user's selected text size (stored in device by the app)...
        val loadedTextSize = prefs.getInt(
            "NOTES_TEXT_SIZE",
            //... but if there isn't one, use the default text size I declared in dimens.xml
            resources.getDimension(R.dimen.default_notes_text_size).toSp()
        )
        println(loadedTextSize)
        // change notes text size to the loaded text size
        notesSpace.textSize = loadedTextSize.toFloat()
        // change text size picker value to display the loaded text size
        textSizePicker.value = loadedTextSize

        // move caret to text end (by default, caret is placed in the beginning)
        notesSpace.setSelection(notesSpace.length())

        // open keyboard
        notesSpace.showKeyboard()

        // define what happens when user makes any changes to their notes
        notesSpace.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                notes: CharSequence, start: Int, before: Int, count: Int
            ) {
                // save notes content to a file in device and label it "NOTES_CONTENT"
                editor.putString("NOTES_CONTENT", notesSpace.text.toString()).apply()
            }

            // these two are unused (but they must be initialized, otherwise the program does not work)
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })

        return root
    }


    // this is executed every time the user opens another tab
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}