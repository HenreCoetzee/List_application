package com.example.shopping_list_application

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ShoppingListApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListApp(viewModel: ShoppingViewModel = viewModel()) {
    val categories by viewModel.allCategories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val itemsInSelectedCategory by viewModel.itemsInSelectedCategory.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Add Item", "Categories")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TaskTide") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = currentTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (currentTab) {
                0 -> AddItemTab(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onItemAdded = { name, quantity ->
                        selectedCategoryId?.let { viewModel.addItem(name, it, quantity) }
                    },
                    onCategorySelected = viewModel::selectCategory
                )
                1 -> CategoryListTab(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    itemsInSelectedCategory = itemsInSelectedCategory,
                    onAddCategory = viewModel::addCategory,
                    onDeleteCategory = viewModel::deleteCategory,
                    onCategorySelected = viewModel::selectCategory,
                    onItemDelete = viewModel::deleteItem,
                    onToggleCategorySelection = viewModel::toggleCategorySelection,
                    onShareSelectedCategories = {
                        coroutineScope.launch {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, viewModel.shareSelectedCategories())
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Shopping List"))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AddItemTab(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onItemAdded: (String, Int) -> Unit,
    onCategorySelected: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        CategoryDropdown(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = onCategorySelected
        )
        Spacer(modifier = Modifier.height(16.dp))
        ShoppingItemInput(onItemAdded = onItemAdded)
    }
}

@Composable
fun CategoryListTab(
    categories: List<Category>,
    selectedCategoryId: Int?,
    itemsInSelectedCategory: List<ShoppingItem>,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onCategorySelected: (Int) -> Unit,
    onItemDelete: (ShoppingItem) -> Unit,
    onToggleCategorySelection: (Category) -> Unit,
    onShareSelectedCategories: () -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = newCategoryName,
            onValueChange = { newCategoryName = it },
            label = { Text("New Category") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (newCategoryName.isNotBlank()) {
                    onAddCategory(newCategoryName)
                    newCategoryName = ""
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add Category")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            categories.forEach { category ->
                item(key = "category_${category.id}") {
                    CategoryItem(
                        category = category,
                        isExpanded = category.id == selectedCategoryId,
                        onCategorySelected = {
                            if (category.id == selectedCategoryId) {
                                onCategorySelected(-1) // Deselect if already selected
                            } else {
                                onCategorySelected(category.id)
                            }
                        },
                        onDeleteCategory = { onDeleteCategory(category) },
                        onToggleSelection = { onToggleCategorySelection(category) }
                    )
                }
                if (category.id == selectedCategoryId) {
                    items(
                        items = itemsInSelectedCategory,
                        key = { "item_${it.id}" }
                    ) { item ->
                        ShoppingListItem(
                            item = item,
                            onDelete = { onItemDelete(item) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onShareSelectedCategories,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Share, contentDescription = "Share")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Selected Categories")
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isExpanded: Boolean,
    onCategorySelected: () -> Unit,
    onDeleteCategory: () -> Unit,
    onToggleSelection: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(onClick = onCategorySelected)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = category.isSelected,
                onCheckedChange = { onToggleSelection() }
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            IconButton(onClick = onDeleteCategory) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
        if (isExpanded) {
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedCategory?.name ?: "Select a category",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ShoppingItemInput(onItemAdded: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item name") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it.filter { char -> char.isDigit() } },
            label = { Text("Qty") },
            modifier = Modifier.width(64.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (name.isNotBlank() && quantity.isNotBlank()) {
                    onItemAdded(name, quantity.toInt())
                    name = ""
                    quantity = "1"
                }
            }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Item")
        }
    }
}

@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${item.name} (${item.quantity})",
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete")
        }
    }
}