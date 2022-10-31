package com.example.inventory.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.*
import com.example.inventory.databinding.ItemListFragmentBinding


class YearTaskFragment : Fragment() {
   private val viewModel: InventoryViewModel by activityViewModels {
      InventoryViewModelFactory(
         (activity?.application as InventoryApplication).database.itemDao()
      )
   }

   private var _binding: ItemListFragmentBinding? = null
   private val binding get() = _binding!!

   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View? {
      _binding = ItemListFragmentBinding.inflate(layoutInflater, container, false)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
//        binding.itemName.text = "Tasks for week"
      val adapter = ItemListAdapter {
         val action =
            YearTaskFragmentDirections.actionItemYearToItemDetailFragment(it.id)
         this.findNavController().navigate(action)
      }
      binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
      binding.recyclerView.adapter = adapter
      // Attach an observer on the allItems list to update the UI automatically when the data
      // changes.
      viewModel.itemsForYear.observe(this.viewLifecycleOwner) { items ->
         items.let {
            adapter.submitList(it)
         }
      }

      binding.floatingActionButton.setOnClickListener {
         val action = YearTaskFragmentDirections.actionItemYearToAddItemFragment(
            getString(R.string.from_year)
         )
         this.findNavController().navigate(action)
      }
   }
}