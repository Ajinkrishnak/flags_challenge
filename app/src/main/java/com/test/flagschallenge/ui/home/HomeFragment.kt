package com.test.flagschallenge.ui.home

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.test.flagschallenge.R
import com.test.flagschallenge.databinding.FragmentHomeBinding
import com.test.flagschallenge.utils.shortToast
import com.test.flagschallenge.utils.showErrorSnackbar
import java.util.Calendar

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewmodel by viewModels()
    private var scheduledTime: Calendar? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
    }

    private fun observe() {
        viewModel.apply {
            reset()
            itemClicks.observe(viewLifecycleOwner) {
                when (it) {
                    HomesClicks.SAVE -> {
                        scheduleChallenge()
                    }

                    HomesClicks.HOUR -> {
                        timePicker()
                    }

                    HomesClicks.MINUTE -> {
                        timePicker()
                    }

                    HomesClicks.SECOND -> {
                        timePicker()
                    }

                    null -> {}
                }
            }
            startQuiz.observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToQuizFragment())
                }
            }
        }
    }

    private fun scheduleChallenge() {
        when {
            scheduledTime == null -> {
                binding.root.showErrorSnackbar(getString(R.string.please_select_a_time))
            }

            scheduledTime!!.timeInMillis > System.currentTimeMillis() -> {
                shortToast(getString(R.string.successfully_scheduled_please_wait_for_the_time))
                viewModel.scheduleChallenge(scheduledTime!!.timeInMillis)
            }

            else -> {
                binding.root.showErrorSnackbar(getString(R.string.the_scheduled_time_is_in_the_past))
            }
        }
    }

    private fun timePicker() {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            scheduledTime = cal.apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            viewModel.setTime(hour, minute, cal.get(Calendar.SECOND))
        }
        TimePickerDialog(
            requireContext(),
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

}