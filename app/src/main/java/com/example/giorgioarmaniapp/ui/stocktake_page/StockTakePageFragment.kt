package com.example.giorgioarmaniapp.ui.login_page.stocktake_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.StockTakeModel
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.google.android.material.appbar.MaterialToolbar

class StockTakePageFragment : Fragment() {

    private lateinit var viewModel: StockTakePageViewModel
    private lateinit var adapter: StockTakeAdapter

    private lateinit var rvStockTake: RecyclerView
    private lateinit var tvExpected:  TextView
    private lateinit var tvScanned:   TextView
    private lateinit var etProductCode: EditText
    private lateinit var etHiddenBarcode: EditText
    private lateinit var llTextInput:  LinearLayout
    private lateinit var btnSave:      Button
    private lateinit var btnSubmit:    Button
    private var loadingView:  View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stock_take_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[StockTakePageViewModel::class.java]

        initViews(view)
        setupRecyclerView()
        observeViewModel()
        setupListeners()
        setupMenu()

        viewModel.initReader(
            model     = BaseViewModel.rfidModel,
            batchMode = BaseViewModel.rfidModel.isBatchMode,
            connected = BaseViewModel.isConnected
        )
    }

    private fun initViews(view: View) {
        rvStockTake     = view.findViewById(R.id.rvStockList)
        tvExpected      = view.findViewById(R.id.tvExpectedQty)
        tvScanned       = view.findViewById(R.id.tvScannedQty)
        etProductCode   = view.findViewById(R.id.etProductCode)
        etHiddenBarcode = view.findViewById(R.id.etHiddenBarcode)
        llTextInput     = view.findViewById(R.id.layoutManualEntry)
        btnSave         = view.findViewById(R.id.btnSave)
        btnSubmit       = view.findViewById(R.id.btnSubmit)
        loadingView     = view.findViewById(R.id.loadingLayout)
    }

    private fun setupRecyclerView() {
        adapter = StockTakeAdapter { item -> confirmDelete(item) }
        rvStockTake.layoutManager = LinearLayoutManager(requireContext())
        rvStockTake.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.stockTakeScannedItems.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list.toMutableList())
        }

        viewModel.expectedQTYTotalCount.observe(viewLifecycleOwner) { count ->
            tvExpected.text = "Expected\nQTY [$count]"
        }

        viewModel.scannedQTYTotalCount.observe(viewLifecycleOwner) { count ->
            tvScanned.text = "Scanned\nQTY [$count]"
        }

        viewModel.isTextBoxVisible.observe(viewLifecycleOwner) { visible ->
            llTextInput.visibility = if (visible) View.VISIBLE else View.GONE
        }

        viewModel.showLoading.observe(viewLifecycleOwner) { loading ->
            loadingView?.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) { goBack ->
            if (goBack) {
                viewModel.clearNavigateBack()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            val code = etProductCode.text.toString().trim()
            viewModel.onAddManualItem(code)
            etProductCode.setText("")
        }

        btnSubmit.setOnClickListener {
            viewModel.submitStockTakeList()
        }

        // Barcode hidden field – fires when scanner sends Enter after barcode
        etHiddenBarcode.setOnEditorActionListener { v, _, _ ->
            val code = v.text.toString().trim()
            if (code.isNotEmpty()) {
                viewModel.onBarcodeScanned(code)
                v.setText("")
            }
            true
        }
    }

    private fun confirmDelete(item: StockTakeModel.StockTakeListModel) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Alert")
            .setMessage("Are you sure you want to Remove this from List?")
            .setPositiveButton("Yes") { _, _ -> viewModel.deleteTag(item) }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.home_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        // findNavController().navigate(R.id.action_stocktake_to_settings)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}
