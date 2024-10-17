package com.example.shopping_list_application

import kotlinx.coroutines.flow.Flow

class ShoppingRepository(private val shoppingDao: ShoppingDao) {
    fun getItemsByCategory(categoryId: Int): Flow<List<ShoppingItem>> = shoppingDao.getItemsByCategory(categoryId)
    val allCategories: Flow<List<Category>> = shoppingDao.getAllCategories()

    suspend fun insertItem(item: ShoppingItem) = shoppingDao.insertItem(item)
    suspend fun insertCategory(category: Category) = shoppingDao.insertCategory(category)
    suspend fun updateItem(item: ShoppingItem) = shoppingDao.updateItem(item)
    suspend fun updateCategory(category: Category) = shoppingDao.updateCategory(category)
    suspend fun deleteItem(item: ShoppingItem) = shoppingDao.deleteItem(item)
    suspend fun deleteCategory(category: Category) {
        shoppingDao.deleteItemsByCategory(category.id)
        shoppingDao.deleteCategory(category)
    }
}

