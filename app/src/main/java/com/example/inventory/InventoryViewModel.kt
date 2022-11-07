/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory

import androidx.lifecycle.*
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.launch

class InventoryViewModel(private val itemDao: ItemDao) : ViewModel() {

    // Cache all items form the database using LiveData.
    val itemsForDay: LiveData<List<Item>> = itemDao.getAllItemsForDay().asLiveData()
    val doneItemsForDay: LiveData<List<Item>> = itemDao.getDoneItemsForDay().asLiveData()
    val unDoneItemsForDay: LiveData<List<Item>> = itemDao.getUnDoneItemsForDay().asLiveData()
    val onlyImportantItemsForDay: LiveData<List<Item>> = itemDao.getOnlyImportanceItemsForDay().asLiveData()
    val allDaySorted: LiveData<List<Item>> = itemDao.getSortedByImportanceDay().asLiveData()

    val itemsForWeek: LiveData<List<Item>> = itemDao.getItemsForWeek().asLiveData()
    val itemsForMonth: LiveData<List<Item>> = itemDao.getItemsForMonth().asLiveData()
    val itemsForYear: LiveData<List<Item>> = itemDao.getItemsForYear().asLiveData()

    fun updateItem(
        itemId: Int,
        itemName: String,
        itemPriority: Int,
        itemDuration: String
    ) {
        val updatedItem = getUpdatedItemEntry(itemId, itemName, itemPriority, itemDuration)
        updateItem(updatedItem)
    }

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }

    fun itemDone(item: Item){
        val doneItem = item.copy(itemDone = true)
        updateItem(doneItem)
    }

    /**
     * Inserts the new Item into database.
     */
    fun addNewItem(itemName: String, itemPriority: Int, itemDuration: String) {
        val newItem = getNewItemEntry(itemName, itemPriority, itemDuration)
        insertItem(newItem)
    }

    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    /**
     * Launching a new coroutine to delete an item in a non-blocking way
     */
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    /**
     * Retrieve an item from the repository.
     */
    fun retrieveItem(id: Int): LiveData<Item> {
        return itemDao.getItem(id).asLiveData()
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(itemName: String): Boolean {
        if (itemName.isBlank()) {
            return false
        }
        return true
    }

    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the Inventory database.
     */
    private fun getNewItemEntry(itemName: String, itemPriority: Int, itemDuration: String): Item {
        return Item(
            itemName = itemName,
            itemPriority = itemPriority,
            itemDuration = itemDuration
        )
    }

    /**
     * Called to update an existing entry in the Inventory database.
     * Returns an instance of the [Item] entity class with the item info updated by the user.
     */
    private fun getUpdatedItemEntry(
        itemId: Int,
        itemName: String,
        itemPriority: Int,
        itemDuration: String

    ): Item {
        return Item(
            id = itemId,
            itemName = itemName,
            itemPriority = itemPriority,
            itemDuration = itemDuration
        )
    }
}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class InventoryViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

