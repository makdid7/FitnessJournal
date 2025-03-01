package maksim.fitnessjournal.ui.tips


/***
 * This class handles the Tips tab
 * @author MakDid7 on GitHub
 ***/

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_tips.*
import maksim.fitnessjournal.R
import maksim.fitnessjournal.databinding.FragmentTipsBinding

class TipsFragment : Fragment() {

    private var _binding: FragmentTipsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    @SuppressLint("ClickableViewAccessibility")
    // this function solves the problem of writing duplicated code for every CardView
    private fun CardView.setCustomOnClick(title: TextView, details: TextView, layout: LinearLayout) {

        // create color references for more readable code
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val watermelon = ContextCompat.getColor(requireContext(), R.color.watermelon)
        val lightPink = ContextCompat.getColor(requireContext(), R.color.light_pink)
        val darkGray = ContextCompat.getColor(requireContext(), R.color.dark_gray)

        // define behavior when cards are pressed
        this.setOnClickListener {

            // determine whether card was open or closed before it was clicked
            val oppositeVisibility = if (details.visibility == View.GONE) View.VISIBLE else View.GONE
            val cardIsBeingOpened = oppositeVisibility == View.GONE

            // flip color scheme with a smooth transition
            val currentTitleColor = if (cardIsBeingOpened) white else watermelon
            val desiredTitleColor = if (cardIsBeingOpened) watermelon else white

            val currentCardColor = if (cardIsBeingOpened) lightPink else darkGray
            val desiredCardColor = if (cardIsBeingOpened) darkGray else lightPink

            val titleColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), currentTitleColor, desiredTitleColor)
            val cardColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), currentCardColor, desiredCardColor)

            titleColorAnimation.duration = 250  // milliseconds
            cardColorAnimation.duration = 250  // milliseconds

            titleColorAnimation.addUpdateListener { animator -> title.setTextColor(animator.animatedValue as Int) }
            cardColorAnimation.addUpdateListener { animator -> this.setCardBackgroundColor(animator.animatedValue as Int) }

            // perform the title text color change animation
            titleColorAnimation.start()
            // perform the card background color change animation
            cardColorAnimation.start()

            // perform the smooth card opening/closing animation
            TransitionManager.beginDelayedTransition(layout, AutoTransition())

            // flip the visibility of the detail text
            details.visibility = oppositeVisibility

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // connect (bind) this file (backend) to fragment_tips.xml (frontend)
        _binding = FragmentTipsBinding.inflate(inflater, container, false)

        // cards move smoothly when others are pressed (with android:animateLayoutChanges="true" in fragment_tips.xml)
        binding.tipsMainView.layoutTransition.enableTransitionType(4)

        // define what happens when the cards are pressed
        binding.cvStarting.setCustomOnClick(binding.tvStartTitle, binding.tvStartDetails, binding.llStart)
        binding.cvMotivation.setCustomOnClick(binding.tvMotivTitle, binding.tvMotivDetails, binding.llMotiv)
        binding.cvDiet.setCustomOnClick(binding.tvDietTitle, binding.tvDietDetails, binding.llDiet)
        binding.cvCaution.setCustomOnClick(binding.tvCautionTitle, binding.tvCautionDetails, binding.llCaution)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}