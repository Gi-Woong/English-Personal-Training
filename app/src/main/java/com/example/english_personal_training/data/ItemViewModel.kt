package com.example.english_personal_training.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ItemRepository(application)
    private val _allItems = MutableLiveData<List<Item>>()

    val allItems: LiveData<List<Item>> get() = _allItems

    init {
        viewModelScope.launch {
            _allItems.value = repository.getAllItems()
        }
    }

    fun insert(item: Item) {
        viewModelScope.launch {
            repository.insert(item)
            _allItems.value = repository.getAllItems()
        }
    }

    fun insertItems(items: List<Item>) {
        viewModelScope.launch {
            repository.insertItems(items)
            _allItems.value = repository.getAllItems()
        }
    }

    fun update(item: Item) {
        viewModelScope.launch {
            repository.update(item)
            _allItems.value = repository.getAllItems()
        }
    }

    fun delete(item: Item) {
        viewModelScope.launch {
            repository.delete(item)
            _allItems.value = repository.getAllItems()
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
            _allItems.value = repository.getAllItems()
        }
    }
}


