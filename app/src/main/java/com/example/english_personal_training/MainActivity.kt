package com.example.english_personal_training

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.english_personal_training.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter
    private lateinit var itemList: MutableList<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize data
        itemList = ArrayList()
        // Add sample items
        itemList.add(Item("Tag1", "Word1", "Meaning1"))
        itemList.add(Item("Tag2", "Word2", "Meaning2"))
        itemList.add(Item("Tag3", "Word3", "Meaning3"))

        // Initialize adapter
        adapter = MyAdapter(itemList)
        binding.recyclerView.adapter = adapter

        // Set up add button click listener
        binding.addButton.setOnClickListener {
            val tag = binding.addTagEditText.text.toString()
            val word = binding.addWordEditText.text.toString()
            val meaning = binding.addMeaningEditText.text.toString()

            if (tag.isNotEmpty() && word.isNotEmpty() && meaning.isNotEmpty()) {
                val newItem = Item(tag, word, meaning)
                itemList.add(newItem)
                adapter.notifyItemInserted(itemList.size - 1)

                // Clear the input fields
                binding.addTagEditText.text.clear()
                binding.addWordEditText.text.clear()
                binding.addMeaningEditText.text.clear()
            }
        }
    }
}
