package com.example.shopping_list_application

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ShoppingRepository
    val allCategories: StateFlow<List<Category>>
    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()
    val itemsInSelectedCategory: StateFlow<List<ShoppingItem>>

    init {
        val database = ShoppingDatabase.getDatabase(application)
        val dao = database.shoppingDao()
        repository = ShoppingRepository(dao)
        allCategories = repository.allCategories.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        itemsInSelectedCategory = _selectedCategoryId.flatMapLatest { categoryId ->
            categoryId?.let { repository.getItemsByCategory(it) } ?: flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun selectCategory(categoryId: Int) {
        _selectedCategoryId.value = if (categoryId == -1 || categoryId == _selectedCategoryId.value) null else categoryId
    }

    fun addItem(name: String, categoryId: Int, quantity: Int) {
        viewModelScope.launch {
            repository.insertItem(ShoppingItem(name = name, categoryId = categoryId, quantity = quantity))
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name))
        }
    }

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun toggleCategorySelection(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category.copy(isSelected = !category.isSelected))
        }
    }

    suspend fun shareSelectedCategories(): String {
        val selectedCategories = allCategories.value.filter { it.isSelected }
        return buildString {
            selectedCategories.forEach { category ->
                appendLine("Category: ${category.name}")
                val items = repository.getItemsByCategory(category.id).first()
                items.forEach { item ->
                    appendLine("- ${item.name} (Quantity: ${item.quantity})")
                }
                appendLine()
            }
        }
    }
}