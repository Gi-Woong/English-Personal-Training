package com.example.english_personal_training

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.english_personal_training.data.Item
import com.example.english_personal_training.data.ItemViewModel
import com.example.english_personal_training.data.ItemViewModelFactory
import com.example.english_personal_training.databinding.FragmentDbBinding

class WordSetFragment : Fragment() {
    private lateinit var binding: FragmentDbBinding
    private lateinit var adapter: WordSetAdapter
    private lateinit var itemViewModel: ItemViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDbBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = WordSetAdapter(mutableListOf())
        binding.recyclerView.adapter = adapter

        // ViewModelProvider로 itemViewModel 초기화
        val factory = ItemViewModelFactory(requireActivity().application)
        itemViewModel = ViewModelProvider(requireActivity(), factory).get(ItemViewModel::class.java)

        // item 계속 관찰하기(변화시 adapter 업데이트)
        itemViewModel.allItems.observe(viewLifecycleOwner, { items ->
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
