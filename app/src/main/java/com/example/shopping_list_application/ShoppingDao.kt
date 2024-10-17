package com.example.shopping_list_application

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items WHERE categoryId = :categoryId")
    fun getItemsByCategory(categoryId: Int): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM shopping_items WHERE categoryId = :categoryId")
    suspend fun deleteItemsByCategory(categoryId: Int)
}
