package com.example.english_personal_training

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.english_personal_training.data.Item
import com.example.english_personal_training.data.ItemViewModel
import com.example.english_personal_training.data.ItemViewModelFactory
import com.example.english_personal_training.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter
    private lateinit var itemViewModel: ItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView 초기화
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter(mutableListOf())
        binding.recyclerView.adapter = adapter

        // ViewModelProvider로 itemViewModel 초기화
        val factory = ItemViewModelFactory(application)
        itemViewModel = ViewModelProvider(this, factory).get(ItemViewModel::class.java)

        // item 계속 관찰하기(변화시 adapter 업데이트)
        itemViewModel.allItems.observe(this, { items ->
            items?.let { adapter.updateItems(it) }
        })

        // MyAdapter로 itemViewModel 전달
        adapter.setItemViewModel(itemViewModel)

        // 등록 버튼 listener 처리
        binding.addButton.setOnClickListener {
            val tag = binding.addTagEditText.text.toString()
            val word = binding.addWordEditText.text.toString()
            val meaning = binding.addMeaningEditText.text.toString()

            if (tag.isNotEmpty() && word.isNotEmpty() && meaning.isNotEmpty()) {
                val newItem = Item(tag = tag, word = word, meaning = meaning)
                itemViewModel.insert(newItem)

                // Clear the input fields
                binding.addTagEditText.text.clear()
                binding.addWordEditText.text.clear()
                binding.addMeaningEditText.text.clear()
            }
        }
    }
}

