package com.example.englishquiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.englishquiz.databinding.FragmentWordsetBinding

class WordsetFragment : Fragment() {
    lateinit var binding: FragmentWordsetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWordsetBinding.inflate(inflater, container, false)

        return binding.root
    }
}
