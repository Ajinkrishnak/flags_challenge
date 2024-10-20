package com.test.flagschallenge.ui.quiz

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.test.flagschallenge.R
import com.test.flagschallenge.data.model.QuestionsItem
import com.test.flagschallenge.data.model.QuestionsResponse
import com.test.flagschallenge.utils.formatTime
import com.test.flagschallenge.utils.getJsonFromAssets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QuizClicks {
    OPTION1, OPTION2, OPTION3, OPTION4
}

@HiltViewModel
class QuizViewmodel @Inject constructor(
    private val state: SavedStateHandle,
    private val application: Application,
) : ViewModel() {
    private var context = application

    private val _itemClicks = MutableLiveData<QuizClicks?>()
    val itemClicks: LiveData<QuizClicks?> = _itemClicks

    private val _gameOver = MutableLiveData<Boolean>(false)
    val gameOver: LiveData<Boolean> = _gameOver

    private val _showQuizTime = MutableLiveData<Boolean>()
    val showQuizTime: LiveData<Boolean> = _showQuizTime

    private val _showQuiz = MutableLiveData<Boolean>()
    val showQuiz: LiveData<Boolean> = _showQuiz

    private val _quizStarted = MutableLiveData<Boolean>()
    val quizStarted: LiveData<Boolean> = _quizStarted

    private val _remainingTimeShow = MutableLiveData<String>()
    val remainingTimeShow: LiveData<String> = _remainingTimeShow

    private val _qusNo = MutableLiveData<Int>()
    val qusNo: LiveData<Int> = _qusNo

    private val _questionsList = MutableLiveData<List<QuestionsItem?>?>()

    private val _questionsItem = MutableLiveData<QuestionsItem>()
    val questionsItem: LiveData<QuestionsItem> = _questionsItem

    private val _flags = MutableLiveData<HashMap<String, Int>>()
    val flags: LiveData<HashMap<String, Int>> = _flags

    private val _option1Clicked = MutableLiveData<Boolean>()
    val option1Clicked: LiveData<Boolean> = _option1Clicked

    private val _option2Clicked = MutableLiveData<Boolean>()
    val option2Clicked: LiveData<Boolean> = _option2Clicked

    private val _option3Clicked = MutableLiveData<Boolean>()
    val option3Clicked: LiveData<Boolean> = _option3Clicked

    private val _option4Clicked = MutableLiveData<Boolean>()
    val option4Clicked: LiveData<Boolean> = _option4Clicked

    private val _nextQusLoading = MutableLiveData<Boolean>()
    val nextQusLoading: LiveData<Boolean> = _nextQusLoading

    private val _correctAns = MutableLiveData<Int>()
    val correctAns: LiveData<Int> = _correctAns


    private val quizTimeInMillis: Long = 30000
    private val nextQusTimeInMillis: Long = 10000
    private var countDownTimer: CountDownTimer? = null

    var score = 0

    private var currentQuestion: Int
        get() = state["currentQuestion"] ?: 0
        set(value) {
            state["currentQuestion"] = value
        }

    private var remainingTime: Long
        get() = state["remainingTime"] ?: 30L
        set(value) {
            state["remainingTime"] = value
        }

    init {
        loadFlags()
        loadQus()
        _nextQusLoading.value = false
        _showQuizTime.value = true
        _showQuiz.value = true
        startTimer(quizTimeInMillis)
    }


    private fun startTimer(time: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(time, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                _remainingTimeShow.value = formatTime(millisUntilFinished)
                _quizStarted.value = true
            }

            override fun onFinish() {
                remainingTime = 0
                if (_nextQusLoading.value == true) {
                    _nextQusLoading.value = false
                    _showQuiz.value = true
                    _showQuizTime.value = true
                    startTimer(quizTimeInMillis)
                    return
                }
                _quizStarted.value = false
                loadNextQues()
            }
        }.start()
    }

    fun stopTimer() {
        countDownTimer?.cancel()
    }

    fun loadNextQues() {
        if (_qusNo.value!! < 15) {
            viewModelScope.launch {
                delay(2000)
                _option1Clicked.value = false
                _option2Clicked.value = false
                _option3Clicked.value = false
                _option4Clicked.value = false
                _nextQusLoading.value = true
                _showQuiz.value = false
                _showQuizTime.value = false
                _correctAns.value = 0
                _questionsItem.value = _questionsList.value?.get(_qusNo.value ?: 0)
                _qusNo.value = _qusNo.value!! + 1
                startTimer(nextQusTimeInMillis)
            }
            return
        }
        _showQuiz.value = false
        _showQuizTime.value = false
        _gameOver.value = true
    }

    private fun loadQus() {
        val json = getJsonFromAssets(context, "questions_list.json")
        val gson = Gson()
        val res = gson.fromJson(json, QuestionsResponse::class.java)
        _questionsList.value = res.questions
        _qusNo.value = 1
        _questionsItem.value = res.questions?.get(0)
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    fun itemClicked(it: QuizClicks) {
        when (it) {
            QuizClicks.OPTION1 -> {
                if (checkOptionsClicked()) return
                _option1Clicked.value = true
                selectOption(questionsItem.value?.countries?.get(0)?.id ?: 0)
            }

            QuizClicks.OPTION2 -> {
                if (checkOptionsClicked()) return
                _option2Clicked.value = true
                selectOption(questionsItem.value?.countries?.get(1)?.id ?: 0)
            }

            QuizClicks.OPTION3 -> {
                if (checkOptionsClicked()) return
                _option3Clicked.value = true
                selectOption(questionsItem.value?.countries?.get(2)?.id ?: 0)
            }

            QuizClicks.OPTION4 -> {
                if (checkOptionsClicked()) return
                _option4Clicked.value = true
                selectOption(questionsItem.value?.countries?.get(3)?.id ?: 0)
            }
        }
        _itemClicks.value = it
    }

    private fun checkOptionsClicked(): Boolean {
        return _option1Clicked.value == true || _option2Clicked.value == true || _option3Clicked.value == true || _option4Clicked.value == true
    }

    private fun selectOption(option: Int) {
        val ans = questionsItem.value?.answerId ?: 0
        questionsItem.value?.countries?.forEachIndexed() { index, item ->
            if (ans == item?.id) {
                _correctAns.value = index + 1
            }
        }
        if (ans == option) {
            score++
        }
        stopTimer()
        loadNextQues()
    }

    private fun loadFlags() {
        _flags.value = hashMapOf(
            "NZ" to R.drawable.flag_nz,
            "AW" to R.drawable.flag_aw,
            "EC" to R.drawable.flag_ec,
            "PY" to R.drawable.flag_py,
            "KG" to R.drawable.flag_kg,
            "PM" to R.drawable.flag_pm,
            "JP" to R.drawable.flag_jp,
            "TM" to R.drawable.flag_tm,
            "GA" to R.drawable.flag_ga,
            "MQ" to R.drawable.flag_mq,
            "BZ" to R.drawable.flag_bz,
            "CZ" to R.drawable.flag_cz,
            "AE" to R.drawable.flag_ae,
            "JE" to R.drawable.flag_je,
            "LS" to R.drawable.flag_ls
        )
    }

    fun onResume() {
        if (_quizStarted.value == true || _nextQusLoading.value == true) {
            _qusNo.value = currentQuestion
            startTimer(remainingTime)
        }
    }

    fun onPause() {
        currentQuestion = qusNo.value ?: 0
        stopTimer()
    }

    fun reset() {
        _itemClicks.value = null
    }
}