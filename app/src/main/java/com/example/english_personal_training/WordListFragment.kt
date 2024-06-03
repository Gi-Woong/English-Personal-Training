package com.example.englishquiz

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.english_personal_training.R
import com.example.english_personal_training.data.Item
import com.example.english_personal_training.data.ItemViewModel


class WordListFragment : Fragment() {

    private lateinit var wordListAdapter: WordListAdapter
    private lateinit var wordViewModel: ItemViewModel
    private lateinit var tagTextView: TextView
    private lateinit var tagListView: LinearLayout
    private var allItems: List<Item> = listOf()
    private var isTagListVisible = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_word_list, container, false)

        // 태그와 관련된 뷰 초기화
        tagTextView = view.findViewById(R.id.tagTextView)
        tagListView = view.findViewById(R.id.tagListView)

        // RecyclerView 초기화 및 어댑터 설정
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        wordListAdapter = WordListAdapter()
        recyclerView.adapter = wordListAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // ViewModel 설정 및 데이터 관찰
        wordViewModel = ViewModelProvider(this).get(ItemViewModel::class.java)
        wordViewModel.allItems.observe(viewLifecycleOwner) { items ->
            items?.let {
                allItems = it
                if (it.isNotEmpty()) {
                    val initialTag = it[0].tag
                    updateTagTextView(initialTag)
                    filterItemsByTag(initialTag)
                }
                updateTagListView()
            }
        }

        // 태그 텍스트뷰 클릭 리스너 설정
        tagTextView.setOnClickListener {
            toggleTagListView()
        }

        return view
    }

    // 태그 텍스트뷰를 업데이트하는 메서드
    private fun updateTagTextView(tag: String) {
        tagTextView.text = tag
    }

    // 태그 목록을 업데이트하는 메서드
    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateTagListView() {
        tagListView.removeAllViews()
        val currentTag = tagTextView.text.toString()
        val tags = allItems.map { it.tag }.distinct().filter { it != currentTag }
        for (tag in tags) {
            val tagView = TextView(context).apply {
                text = tag
                textSize = 18f
                setPadding(8, 8, 8, 8)
                setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
                setOnClickListener {
                    updateTagTextView(tag)
                    filterItemsByTag(tag)
                    tagListView.visibility = View.GONE
                    isTagListVisible = false
                }
            }
            tagListView.addView(tagView)
        }
    }

    // 특정 태그에 따라 아이템을 필터링하는 메서드
    private fun filterItemsByTag(tag: String) {
        val filteredItems = allItems.filter { it.tag == tag }
        wordListAdapter.setWords(filteredItems.map { item ->
            WordTestItem(
                word = item.word,
                meaning = item.meaning,
                options = listOf(), // 필요에 따라 옵션 리스트를 추가하세요
                userChoice = null
            )
        })
    }

    // 태그 목록의 가시성을 토글하는 메서드
    @RequiresApi(Build.VERSION_CODES.M)
    private fun toggleTagListView() {
        if (isTagListVisible) {
            tagListView.visibility = View.GONE
            isTagListVisible = false
        } else {
            updateTagListView()
            tagListView.visibility = View.VISIBLE
            isTagListVisible = true
        }
    }
}
