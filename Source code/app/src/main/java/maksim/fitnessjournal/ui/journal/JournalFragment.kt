package maksim.fitnessjournal.ui.journal


/***
 * This class handles the Journal tab
 * @author MakDid7 on GitHub
 ***/

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import maksim.fitnessjournal.R
import maksim.fitnessjournal.databinding.FragmentJournalBinding
import maksim.fitnessjournal.ui.journal.tracking.CustomTrackingActivity
import maksim.fitnessjournal.ui.journal.tracking.TrackingActivity

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = this._binding!!


    private fun ImageButton.openDefaultTrackingOnClick(
        promptStart: String,
        promptEnd: String,
        disciplineName: String,
        disciplineUnit: String,
    ) {

        this.setOnClickListener { _ ->

            // create a data container for the tracking Activity
            val intent = Intent(context, TrackingActivity::class.java)

            // put the provided data in the data container
            intent.putExtra("PROMPT_START", promptStart)
            intent.putExtra("PROMPT_END", promptEnd)
            intent.putExtra("DISCIPLINE_NAME", disciplineName)
            intent.putExtra("DISCIPLINE_UNIT", disciplineUnit)

            // open the Activity and pass the data container to it
            startActivity(intent)

        }
    }


    private fun ImageButton.openCustomTrackingOnClick() {
        this.setOnClickListener { _ ->
            startActivity(Intent(context, CustomTrackingActivity::class.java))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        this._binding = FragmentJournalBinding.inflate(inflater, container, false)

        // Configure the behaviour of each button (make tracking screen open)

        /* placing these in MainActivity.kt made them work only until user switched tabs in nav */
        binding.imgBtnSkiprope.openDefaultTrackingOnClick(
            getString(R.string.skip_rope_tracking_prompt_start),
            getString(R.string.skip_rope_tracking_prompt_end),
            getString(R.string.skip_rope),
            getString(R.string.skip_rope_unit)
        )
        binding.imgBtnHitups.openDefaultTrackingOnClick(
            getString(R.string.hit_ups_tracking_prompt_start),
            getString(R.string.hit_ups_tracking_prompt_end),
            getString(R.string.hit_ups),
            getString(R.string.hit_ups_unit)
        )
        binding.imgBtnKickups.openDefaultTrackingOnClick(
            getString(R.string.kick_ups_tracking_prompt_start),
            getString(R.string.kick_ups_tracking_prompt_end),
            getString(R.string.kick_ups),
            getString(R.string.kick_ups_unit)
        )
        binding.imgBtn400m.openDefaultTrackingOnClick(
            getString(R.string._400m_tracking_prompt_start),
            getString(R.string._400m_tracking_prompt_end),
            getString(R.string._400m),
            getString(R.string._400m_unit)
        )
        binding.imgBtn800m.openDefaultTrackingOnClick(
            getString(R.string._800m_tracking_prompt_start),
            getString(R.string._800m_tracking_prompt_end),
            getString(R.string._800m),
            getString(R.string._800m_unit)
        )
        binding.imgBtnLongjump.openDefaultTrackingOnClick(
            getString(R.string.long_jump_tracking_prompt_start),
            getString(R.string.long_jump_tracking_prompt_end),
            getString(R.string.long_jump),
            getString(R.string.long_jump_unit)
        )
        binding.imgBtnJavelin.openDefaultTrackingOnClick(
            getString(R.string.javelin_throw_tracking_prompt_start),
            getString(R.string.javelin_throw_tracking_prompt_end),
            getString(R.string.javelin_throw),
            getString(R.string.javelin_throw_unit)
        )
        binding.imgBtnShotput.openDefaultTrackingOnClick(
            getString(R.string.shot_put_tracking_prompt_start),
            getString(R.string.shot_put_tracking_prompt_end),
            getString(R.string.shot_put),
            getString(R.string.shot_put_unit)
        )
        binding.imgBtnHighjump.openDefaultTrackingOnClick(
            getString(R.string.high_jump_tracking_prompt_start),
            getString(R.string.high_jump_tracking_prompt_end),
            getString(R.string.high_jump),
            getString(R.string.high_jump_unit)
        )
        binding.imgBtnPushups.openDefaultTrackingOnClick(
            getString(R.string.push_ups_tracking_prompt_start),
            getString(R.string.push_ups_tracking_prompt_end),
            getString(R.string.push_ups),
            getString(R.string.push_ups_unit)
        )
        binding.imgBtnCustomOption.openCustomTrackingOnClick()

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        this._binding = null
    }
}