package com.example.english_personal_training.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Insert
    suspend fun insertItems(items: List<Item>)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("DELETE FROM items")
    suspend fun deleteAll()

    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<Item>

    // 선택된 tag에 해당하는 word 개수 세기
    @Query("SELECT COUNT(*) FROM items WHERE tag = :tag")
    suspend fun countItemsByTag(tag: String): Int
}
