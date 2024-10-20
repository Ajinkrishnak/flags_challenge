package com.test.flagschallenge.ui.home

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.test.flagschallenge.utils.formatTime
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class HomesClicks {
    SAVE, HOUR, MINUTE, SECOND
}

@HiltViewModel
class HomeViewmodel @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {

    private val _itemClicks = MutableLiveData<HomesClicks?>()
    val itemClicks: LiveData<HomesClicks?> = _itemClicks

    private val _showStartTime = MutableLiveData<Boolean>()
    val showStartTime: LiveData<Boolean> = _showStartTime

    private val _startQuiz = MutableLiveData<Boolean>()
    val startQuiz: LiveData<Boolean> = _startQuiz

    private val _remainingTimeShow = MutableLiveData<String>()
    val remainingTimeShow: LiveData<String> = _remainingTimeShow

    private val _hours = MutableLiveData<Pair<Int, Int>>()
    val hours: LiveData<Pair<Int, Int>> = _hours

    private val _minutes = MutableLiveData<Pair<Int, Int>>()
    val minutes: LiveData<Pair<Int, Int>> = _minutes

    private val _seconds = MutableLiveData<Pair<Int, Int>>()
    val seconds: LiveData<Pair<Int, Int>> = _seconds

    private var countDownTimer: CountDownTimer? = null

    val startTimeInMillis: Long = 20000

    private var remainingTime: Long
        get() = state["remainingTime"] ?: 30L
        set(value) {
            state["remainingTime"] = value
        }

    init {
        _hours.value = Pair(0, 0)
        _minutes.value = Pair(0, 0)
        _seconds.value = Pair(0, 0)
    }

    fun itemClicked(it: HomesClicks) {
        _itemClicks.value = it
    }

    fun reset() {
        _itemClicks.value = null
        _startQuiz.value= false
    }

    fun setTime(hour: Int, min: Int, sec: Int) {
        _hours.value = Pair(hour / 10, hour % 10)
        _minutes.value = Pair(min / 10, min % 10)
        _seconds.value = Pair(sec / 10, sec % 10)
    }

    fun finalCountDown(time: Long) {
        _showStartTime.value = true
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(time, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                _remainingTimeShow.value = formatTime(millisUntilFinished)
            }

            override fun onFinish() {
                remainingTime = 0
                _remainingTimeShow.value = "00:00"
                _startQuiz.value = true
                _showStartTime.value = false
            }
        }.start()
    }

    fun scheduleChallenge(timeInMillis: Long) {
        countDownTimer?.cancel()
        val currentTime = System.currentTimeMillis()
        val delayMillis = timeInMillis - currentTime
        countDownTimer = object : CountDownTimer(delayMillis - 20000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                _remainingTimeShow.value = formatTime(millisUntilFinished)
            }

            override fun onFinish() {
                finalCountDown(startTimeInMillis)
            }
        }.start()
    }

    fun onResume() {
        if (_showStartTime.value == true) {
            finalCountDown(remainingTime)
        }
    }

    fun onPause() {
        stopTimer()
    }

    fun stopTimer() {
        countDownTimer?.cancel()
    }

}