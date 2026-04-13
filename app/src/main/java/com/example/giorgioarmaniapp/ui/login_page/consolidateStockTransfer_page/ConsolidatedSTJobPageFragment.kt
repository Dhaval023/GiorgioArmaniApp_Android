package com.example.giorgioarmaniapp.ui.login_page.consolidateStockTransfer_page

import android.app.AlertDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.OutBoundStockModel
import com.example.giorgioarmaniapp.models.statics.ScanOptionModel
import com.example.giorgioarmaniapp.ui.login_page.inbound_page.ScanOptionAdapter
import com.example.giorgioarmaniapp.service.RestService
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.example.giorgioarmaniapp.ui.login_page.popup.PasscodeFragment
import com.google.android.material.appbar.MaterialToolbar

class ConsolidatedSTJobPageFragment : Fragment() {

    private val viewModel: ConsolidatedSTJobPageViewModel by viewModels()

    private var pendingOutboundArg: OutBoundStockModel.PendingOutboundResult? = null

    private lateinit var rvScanOptions:        RecyclerView
    private lateinit var llTextInputRow:       LinearLayout
    private lateinit var etProductIDCode:      EditText
    private lateinit var btnSave:              Button
    private lateinit var tvHeaderExpectedQTY:  TextView
    private lateinit var tvHeaderScannedQTY:   TextView
    private lateinit var rvInboundItems:       RecyclerView
    private lateinit var etBarcodeEntry:       EditText
    private lateinit var btnSubmit:            Button
    private lateinit var loadingOverlay:       View

    private lateinit var adapter: ConsolidatedSTAdapter
    private lateinit var scanOptionAdapter: ScanOptionAdapter

    private val restService = RestService()

    companion object {
        private const val ARG_PENDING_DATA = "pending_outbound_data"

        fun newInstance(data: OutBoundStockModel.PendingOutboundResult): ConsolidatedSTJobPageFragment {
            return ConsolidatedSTJobPageFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PENDING_DATA, data)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        @Suppress("DEPRECATION")
        pendingOutboundArg = arguments?.getSerializable(ARG_PENDING_DATA) as? OutBoundStockModel.PendingOutboundResult
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_consolidated_st_job_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupRecyclerViews()

        pendingOutboundArg?.let { data ->
            viewModel.setPendingOutBoundData(data)
            val title = data.toStore ?: "Consolidated ST"
            viewModel.setPageTitle(title)
            (activity as? AppCompatActivity)?.supportActionBar?.title = title
        }

        observeViewModel()
        setupClickListeners()
        setupBarcodeFocus()
        setupMenu()
    }

    private fun setupBarcodeFocus() {
        viewModel.requestBarcodeFocus.observe(viewLifecycleOwner) { request ->
            if (request) {
                focusBarcodeEntry()
                viewModel.clearBarcodeFocus()
            }
        }
    }

    private fun focusBarcodeEntry() {
        if (etBarcodeEntry.isFocused == false) {
            etBarcodeEntry.requestFocus()
        }
        hideKeyboard()
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
        viewModel.onResume()
        hideKeyboard()
        
        // Wire RFID callbacks
        BaseViewModel.updateIn(
            onTagRead = { tags -> viewModel.onTagRead(tags) },
            onTrigger = { pressed -> if (pressed) viewModel.onTriggerPressed() else viewModel.onTriggerReleased() },
            onStatus  = { event -> viewModel.onStatusEvent(event.statusEventType.toString()) }
        )
        
        viewModel.initReader(BaseViewModel.rfidModel.rfidReader)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
        BaseViewModel.updateOut()
    }

    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
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
                        viewModel.navigateToSettingsPage()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

//    @Deprecated("Deprecated in Java")
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.action_settings) {
//            viewModel.()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private fun bindViews(view: View) {
        rvScanOptions        = view.findViewById(R.id.rvScanOptions)
        llTextInputRow       = view.findViewById(R.id.llTextInputRow)
        etProductIDCode      = view.findViewById(R.id.etProductIDCode)
        btnSave              = view.findViewById(R.id.btnSave)
        tvHeaderExpectedQTY  = view.findViewById(R.id.tvHeaderExpectedQTY)
        tvHeaderScannedQTY   = view.findViewById(R.id.tvHeaderScannedQTY)
        rvInboundItems       = view.findViewById(R.id.rvInboundItems)
        etBarcodeEntry       = view.findViewById(R.id.etBarcodeEntry)
        btnSubmit            = view.findViewById(R.id.btnSubmit)
        loadingOverlay       = view.findViewById(R.id.loadingLayout)
    }

    private fun setupRecyclerViews() {
        // Scan Options
        scanOptionAdapter = ScanOptionAdapter { model ->
            viewModel.onScanOptionSelected(model)
        }
        rvScanOptions.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvScanOptions.adapter = scanOptionAdapter

        // Job List
        adapter = ConsolidatedSTAdapter(
            onDeleteClick = { item -> confirmDelete(item) }
        )
        rvInboundItems.layoutManager = LinearLayoutManager(requireContext())
        rvInboundItems.adapter       = adapter
        adapter.attachSwipeToDelete(rvInboundItems)
    }

    private fun observeViewModel() {
        viewModel.scanOptions.observe(viewLifecycleOwner) { options ->
            scanOptionAdapter.submitList(options?.toList())
        }

        viewModel.consolidatedSTItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items?.toList())
        }

        viewModel.consolidatedSTExpectedQTYTotalCount.observe(viewLifecycleOwner) { count ->
            tvHeaderExpectedQTY.text = "Expected\nQTY\n[$count]"
        }
        viewModel.consolidatedSTScannedQTYTotalCount.observe(viewLifecycleOwner) { count ->
            tvHeaderScannedQTY.text = "Scanned\nQTY\n[$count]"
        }

        viewModel.isTextBoxVisible.observe(viewLifecycleOwner) { visible ->
            llTextInputRow.visibility = if (visible) View.VISIBLE else View.GONE
            if (visible) etProductIDCode.requestFocus()
        }

        viewModel.isBarcodeViewVisible.observe(viewLifecycleOwner) { visible ->
            if (visible) {
                focusBarcodeEntry()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            showAlert("Alert", msg)
            viewModel.clearError()
        }
        viewModel.successMessage.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            showAlert("Alert", msg)
            viewModel.clearSuccess()
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) { go ->
            if (go) {
                findNavController().popBackStack()
                viewModel.clearNavigateBack()
            }
        }

        viewModel.navigateToSettings.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                viewModel.onNavigateToSettingsHandled()
                PasscodeFragment().show(parentFragmentManager, "PasscodePopup")
            }
        }

        viewModel.showLoading.observe(viewLifecycleOwner) { loading ->
            loadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            val code = etProductIDCode.text?.toString()?.trim() ?: ""
            viewModel.setProductIDcode(code)
            viewModel.onAddManualItem()
            etProductIDCode.setText("")
        }

        etBarcodeEntry.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideKeyboard()
            } else {
                if (viewModel.isBarcodeViewVisible.value == true) {
                    etBarcodeEntry.post { focusBarcodeEntry() }
                }
            }
        }

        etBarcodeEntry.setOnEditorActionListener { v, actionId, event ->
            val isDone  = actionId == EditorInfo.IME_ACTION_DONE
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.action   == KeyEvent.ACTION_DOWN
            if (isDone || isEnter) {
                val code = v.text.toString().trim()
                if (code.isNotEmpty()) {
                    viewModel.onBarcodeScanned(code)
                    v.setText("")
                    focusBarcodeEntry()
                }
                true
            } else false
        }

        btnSubmit.setOnClickListener {
            viewModel.submitConsolidatedST(restService, requireContext())
        }
    }

    private fun confirmDelete(item: OutBoundStockModel.OutBoundStockListModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Alert")
            .setMessage("Are you sure you want to Remove this from List?")
            .setPositiveButton("Yes") { _, _ -> viewModel.deleteTag(item) }
            .setNegativeButton("No",  null)
            .show()
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok", null)
            .show()
    }

    private fun hideKeyboard() {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
