package com.example.english_personal_training
import androidx.recyclerview.widget.DefaultItemAnimator

class FastItemAnimator : DefaultItemAnimator() {

    init {
        // Set the animation durations to be faster
        addDuration = 0
        removeDuration = 0
        moveDuration = 0
        changeDuration = 0
    }
}