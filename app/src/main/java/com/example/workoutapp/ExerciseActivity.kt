package com.example.workoutapp

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.net.Uri.parse
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workoutapp.databinding.ActivityExerciseBinding
import com.example.workoutapp.databinding.DialogCustomBackConfirmationBinding
import java.net.URI
import java.util.*
import java.util.logging.Level.parse
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // Define variables to store the rest timer and progress
    private var restTimer: CountDownTimer? = null
    private var restProgress = 0
    private var restTimerDuration:Long = 1

    // Define variables to store the exercise timer and progress
    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0
    private var exerciseTimerDuration:Long = 3
    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    //Define a variable to store the text to speech
    private var tts: TextToSpeech? = null

    private  var player : MediaPlayer? = null

    private var exerciseAdapter : ExerciseStatusAdapter ? = null

    // Define a variable to store the binding for the ExerciseActivity layout
    private var binding:ActivityExerciseBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the ExerciseActivity layout
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Set up the toolbar and navigation
        setSupportActionBar(binding?.toolbarExercise)
        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarExercise?.setNavigationOnClickListener {
            customDialogForBackButton()
        }

        tts = TextToSpeech(this,this)

        // Get the default exercise list and set up the rest view
        exerciseList = Constants.defaultExercise()
        setupRestView()
    }

    private fun customDialogForBackButton() {
        val customDialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)
        dialogBinding.tvYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }
        dialogBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    // Set up the rest view
    private fun setupRestView() {

        try {
            val soundURI = (Uri.parse("android.resource://com.example.workoutapp/" + R.raw.app_src_main_res_raw_press_start))
            player = MediaPlayer.create(applicationContext, soundURI)
            player?.isLooping = false
            player?.start()

        } catch (e: Exception)
        {
            e.printStackTrace()
        }

        // Hide exercise-related views and show rest-related views
        binding?.flRestView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        binding?.tvExerciseName?.visibility = View.INVISIBLE
        binding?.flExerciseView?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.VISIBLE
        binding?.upcomingLabel?.visibility = View.VISIBLE


        // Set the upcoming exercise name and progress bar
        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }
        binding?.tvUpcomingExerciseName?.text  = exerciseList!![currentExercisePosition +1 ].getName()
        speakOut("Rest now,next up" + "${exerciseList!![currentExercisePosition +1 ].getName()}")
        setRestProgressBar()
        setupExerciseStatusRecyclerView()
    }

    // Set up the rest timer and progress bar
    private fun setRestProgressBar() {

        // Set the progress of the progress bar and timer text
        binding?.progressBar?.progress = restProgress
        restTimer = object : CountDownTimer(restTimerDuration*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                binding?.progressBar?.progress = (restProgress-restTimerDuration ).toInt()
                binding?.tvTimer?.text =
                    (restProgress-restTimerDuration ).toString()
            }

            // When the rest timer finishes, move to the next exercise
            override fun onFinish() {
                currentExercisePosition++
                exerciseList!![currentExercisePosition].setIsSelected(true) // Current Item is selected
                exerciseAdapter!!.notifyDataSetChanged()
                setupExerciseView()
            }
        }.start()
    }

    override fun onBackPressed() {
        customDialogForBackButton()
    }
    private fun setupExerciseStatusRecyclerView(){
        binding?.rvExerciseStatus?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter = exerciseAdapter
    }

    // Set up the exercise view
    private fun setupExerciseView() {

        // Hide rest-related views and show exercise-related views
        binding?.flRestView?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility = View.INVISIBLE
        binding?.tvExerciseName?.visibility = View.VISIBLE
        binding?.flExerciseView?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.INVISIBLE
        binding?.upcomingLabel?.visibility = View.INVISIBLE

        // Set the exercise image
        if (exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }
        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExerciseName?.text = exerciseList!![currentExercisePosition].getName()

        //Speak out the exercise name
        speakOut(exerciseList!![currentExercisePosition].getName())
        // Start the exercise timer and progress bar
        setExerciseProgressBar()

    }

    // Set up the exercise timer and progress bar
    private fun setExerciseProgressBar() {

        // Set the progress of the progress bar and timer text
        binding?.progressBarExercise?.progress = exerciseProgress
        exerciseTimer = object : CountDownTimer(exerciseTimerDuration * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                binding?.progressBarExercise?.progress = exerciseTimerDuration.toInt() - exerciseProgress
                binding?.tvTimerExercise?.text = (exerciseTimerDuration.toInt() - exerciseProgress).toString()
            }

            // When the exercise timer finishes, either move to the next exercise or end the workout
            override fun onFinish() {

                if (currentExercisePosition < exerciseList?.size!! - 1) {
                    exerciseList!![currentExercisePosition].setIsSelected(false) // exercise is completed so selection is set to false
                    exerciseList!![currentExercisePosition].setIsCompleted(true) // updating in the list that this exercise is completed
                    exerciseAdapter!!.notifyDataSetChanged()
                    setupRestView()
                } else {
                    finish()
                    val intent = Intent(this@ExerciseActivity,FinishActivity::class.java)
                    startActivity(intent)
                }
            }
        }.start()

    }

    // Release resources when the activity is destroyed, always needed when using viewBinding
    public override fun onDestroy() {
        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }

        //Shutting down the Text to Speech feature when activity is destroyed
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }

        //Stopping player when not null when activity is destroyed
        if (player != null) {
            player!!.stop()
        }

        super.onDestroy()
        binding = null
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = tts?.setLanguage(Locale.UK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}