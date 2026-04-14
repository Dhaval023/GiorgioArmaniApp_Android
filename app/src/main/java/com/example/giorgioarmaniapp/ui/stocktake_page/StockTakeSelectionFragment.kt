package com.example.giorgioarmaniapp.ui.login_page.stocktake_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.giorgioarmaniapp.R
import com.google.android.material.appbar.MaterialToolbar

class StockTakeSelectionFragment : Fragment() {

    private lateinit var viewModel: StockTakeSelectionViewModel

    private lateinit var spGender: Spinner
    private lateinit var spCategory: Spinner
    private lateinit var spBrand: Spinner
    private lateinit var btnSubmit: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_stock_take_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[StockTakeSelectionViewModel::class.java]

        spGender = view.findViewById(R.id.spGender)
        spCategory = view.findViewById(R.id.spCategory)
        spBrand = view.findViewById(R.id.spBrand)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        observeData()
        viewModel.loadData()

        btnSubmit.setOnClickListener {
            navigateToStockTakePage()
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationIconTint(resources.getColor(R.color.white, null))
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeData() {

        viewModel.genderList.observe(viewLifecycleOwner) {
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, it)
            spGender.adapter = adapter
        }

        viewModel.categoryList.observe(viewLifecycleOwner) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, it)
            spCategory.adapter = adapter
        }

        viewModel.brandList.observe(viewLifecycleOwner) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, it)
            spBrand.adapter = adapter
        }

        spGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                viewModel.selectedGender.value = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                viewModel.selectedCategory.value = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                viewModel.selectedBrand.value = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun navigateToStockTakePage() {
        val gender = viewModel.selectedGender.value ?: ""
        val category = viewModel.selectedCategory.value ?: ""
        val brand = viewModel.selectedBrand.value ?: ""

        val bundle = Bundle().apply {
            putString("gender", gender)
            putString("category", category)
            putString("brand", brand)
        }

        findNavController().navigate(R.id.action_stockTakeSelection_to_stockTakePage, bundle)
    }
}