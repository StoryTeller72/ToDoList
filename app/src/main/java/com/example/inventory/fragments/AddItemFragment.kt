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
package com.example.inventory.fragments

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.inventory.*
import com.example.inventory.data.Item
import com.example.inventory.databinding.FragmentAddItemBinding

/**
 * Fragment to add or update an item in the Inventory database.
 */
class AddItemFragment : Fragment() {

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    // to share the ViewModel across fragments.
    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database
                .itemDao()
        )
    }
    private val navigationArgs: ItemDetailFragmentArgs by navArgs()
    private val navigationArgsFrom: AddItemFragmentArgs by navArgs()

    lateinit var item: Item

    // Binding object instance corresponding to the fragment_add_item.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.itemName.text.toString()
        )
    }

    /**
     * Binds views with the passed in [item] information.
     */
    private fun bind(item: Item) {
        binding.apply {
            itemName.setText(item.itemName, TextView.BufferType.SPANNABLE)
            when(item.itemPriority){
                Importance.high.value -> binding.importanceGroup.check(R.id.high_priority)
                Importance.medium.value -> binding.importanceGroup.check(R.id.middle_priority)
                else -> binding.importanceGroup.check(R.id.low_priority)
            }
            saveAction.setOnClickListener { updateItem() }
            when(item.itemDuration){
                Constance.YEAR -> binding.durationGroup.check(R.id.year)
                Constance.MONTH -> binding.durationGroup.check(R.id.month)
                Constance.WEEK -> binding.durationGroup.check(R.id.month)
                else -> binding.durationGroup.check(R.id.day)
            }
        }
    }

    /**
     * Inserts the new Item into database and navigates up to list fragment.
     */
    private fun addNewItem() {
        val action: NavDirections
        if (isEntryValid()) {
            viewModel.addNewItem(
                binding.itemName.text.toString().trimStart(),
                when(binding.importanceGroup.checkedRadioButtonId){
                    R.id.high_priority -> Importance.high.value
                    R.id.middle_priority -> Importance.medium.value
                    else -> Importance.low.value
                },
                when(binding.durationGroup.checkedRadioButtonId){
                    R.id.year -> {
                        action = AddItemFragmentDirections.actionAddItemFragmentToItemYear()
                        Constance.YEAR
                    }
                    R.id.month -> {
                        action = AddItemFragmentDirections.actionAddItemFragmentToItemMonth()
                        Constance.MONTH
                    }
                    R.id.week -> {
                        action = AddItemFragmentDirections.actionAddItemFragmentToItemWeek()
                        Constance.WEEK
                    }
                    else -> {
                        action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
                        Constance.DAY
                    }
                }
            )
            findNavController().navigate(action)
        }
        else{
            Toast.makeText(activity, "You could not add empty task", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Updates an existing Item in the database and navigates up to list fragment.
     */
    private fun updateItem() {
        if (isEntryValid()) {
            viewModel.updateItem(
                this.navigationArgs.itemId,
                this.binding.itemName.text.toString(),
                when(this.binding.importanceGroup.checkedRadioButtonId){
                    R.id.high_priority -> Importance.high.value
                    R.id.middle_priority -> Importance.medium.value
                    else -> Importance.low.value
                },
                when(this.binding.durationGroup.checkedRadioButtonId){
                    R.id.year -> Constance.YEAR
                    R.id.month -> Constance.MONTH
                    R.id.week -> Constance.WEEK
                    else -> Constance.DAY
                }
            )
            when(navigationArgsFrom.title){
                getString(R.string.from_day) ->  {
                    val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
                    findNavController().navigate(action)
                }

                getString(R.string.from_week) ->{
                    val action = AddItemFragmentDirections.actionAddItemFragmentToItemWeek()
                    findNavController().navigate(action)
                }

                getString(R.string.from_month)->{
                    val action = AddItemFragmentDirections.actionAddItemFragmentToItemMonth()
                    findNavController().navigate(action)
                }

                getString(R.string.from_year)->{
                    val action = AddItemFragmentDirections.actionAddItemFragmentToItemYear()
                    findNavController().navigate(action)
                }
            }

        }
        else{
            Toast.makeText(activity, "You could not add empty task", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Called when the view is created.
     * The itemId Navigation argument determines the edit item  or add new item.
     * If the itemId is positive, this method retrieves the information from the database and
     * allows the user to update it.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(navigationArgsFrom.title){
            getString(R.string.from_day) ->  {
               binding.durationGroup.check(R.id.day)
            }

            getString(R.string.from_week) ->{
                binding.durationGroup.check(R.id.week)
            }

            getString(R.string.from_month)->{
                binding.durationGroup.check(R.id.month)
            }

            getString(R.string.from_year)->{
                binding.durationGroup.check(R.id.year)
            }
        }

        val id = navigationArgs.itemId
        if (id > 0) {
            viewModel.retrieveItem(id).observe(this.viewLifecycleOwner) { selectedItem ->
                item = selectedItem
                bind(item)
            }
        } else {
            binding.saveAction.setOnClickListener {
                addNewItem()
            }
        }
    }

    /**
     * Called before fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }
}
