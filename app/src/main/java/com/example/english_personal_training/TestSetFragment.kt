package com.example.english_personal_training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.english_personal_training.databinding.FragmentWordSetBinding

class TestSetFragment : Fragment() {
    lateinit var binding: FragmentWordSetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWordSetBinding.inflate(inflater, container, false)

        return binding.root
    }
}
