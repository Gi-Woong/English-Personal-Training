package com.example.english_personal_training.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(context: Context) {
    private val itemDao: ItemDao

    init {
        val database = ItemDatabase.getDatabase(context)
        itemDao = database.itemDao()
    }

    suspend fun insert(item: Item) {
        itemDao.insert(item)
    }

    suspend fun insertItems(items: List<Item>) = withContext(Dispatchers.IO) {
        itemDao.insertItems(items)
    }

    suspend fun update(item: Item) {
        itemDao.update(item)
    }

    suspend fun delete(item: Item) {
        itemDao.delete(item)
    }

    suspend fun deleteAll() {
        return itemDao.deleteAll()
    }

    suspend fun getAllItems(): List<Item> {
        return itemDao.getAllItems()
    }
}
