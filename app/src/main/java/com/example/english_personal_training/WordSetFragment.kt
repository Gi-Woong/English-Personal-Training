
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.english_personal_training.R
import com.example.english_personal_training.WordSetAdapter
import com.example.english_personal_training.data.Item
import com.example.english_personal_training.data.ItemViewModel
import com.example.english_personal_training.data.ItemViewModelFactory
import com.example.english_personal_training.databinding.FragmentDbBinding
import com.example.englishquiz.WordListFragment
import com.github.doyaaaaaken.kotlincsv.client.CsvReader

class WordSetFragment : Fragment() {
    private var _binding: FragmentDbBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WordSetAdapter
    private lateinit var itemViewModel: ItemViewModel

    // CSV file launcher
    private val csvFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val items = parseCsv(requireContext(), it)
            itemViewModel.insertItems(items)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDbBinding.inflate(inflater, container, false)
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

        // 단어목록 버튼 listener 처리
        binding.buttonWordList.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WordListFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // CSV 선택 버튼 listener 처리
        binding.buttonImportCsv.setOnClickListener {
            csvFileLauncher.launch("text/comma-separated-values")
        }

        // 전체삭제 버튼 listener 처리
        binding.buttonDeleteAll.setOnClickListener {
            itemViewModel.deleteAll()
            // deleteAll 호출 후 즉시 LiveData를 관찰하여 RecyclerView를 업데이트
            itemViewModel.allItems.observe(viewLifecycleOwner, { items ->
                items?.let { adapter.updateItems(it) }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parseCsv(context: Context, uri: Uri): List<Item> {
        val itemList = mutableListOf<Item>()
        val inputStream = context.contentResolver.openInputStream(uri)

        inputStream?.use { stream ->
            val reader = CsvReader()
            val result = reader.readAllWithHeader(inputStream)

            result.forEach { row ->
                val tag = row["tag"]?.trim()
                val word = row["word"]?.trim()
                val meaning = row["meaning"]?.trim()
                if (!tag.isNullOrEmpty() && !word.isNullOrEmpty() && !meaning.isNullOrEmpty()) {
                    itemList.add(Item(tag = tag, word = word, meaning = meaning))
                }
            }
        }
        return itemList
    }
}
