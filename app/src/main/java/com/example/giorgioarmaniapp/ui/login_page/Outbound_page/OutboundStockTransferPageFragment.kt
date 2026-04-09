package com.example.giorgioarmaniapp.ui.login_page.Outbound_page

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.models.statics.ScanOptionModel
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.example.giorgioarmaniapp.ui.login_page.inbound_page.ScanOptionAdapter
import com.example.giorgioarmaniapp.ui.login_page.popup.PasscodeFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zebra.rfid.api3.Antennas

class OutboundStockTransferPageFragment : Fragment() {

    private val viewModel: OutboundStockTransferPageViewModel by viewModels()

    private lateinit var scanOptionAdapter: ScanOptionAdapter
    private lateinit var outboundItemAdapter: StockTransferItemAdapter
    private lateinit var loadingOverlay: View

    // Guard flags: suppress text watchers while programmatically clearing fields
    private var isClearingBarcode = false
    private var isClearingProductCode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_outbound_stocktransfer_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingOverlay = view.findViewById(R.id.loadingLayout)
        viewModel.updateBarcodeOut()

        setupStoreCodeSpinner(view)
        setupLocationSpinner(view)
        setupLocationSaveButton(view)
        setupScanOptions(view)
        setupTextEntry(view)
        setupOutboundList(view)
        setupBarcodeEntry(view)
        setupNextButton(view)
        observeViewModel(view)

        focusBarcodeEntry(view)
        viewModel.activeOnFocus = { focusBarcodeEntry(view) }

        setupMenu()
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
                menuInflater.inflate(R.menu.menu_outbound_main, menu)
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

    override fun onResume() {
        super.onResume()
        try {
            setupToolbar()
            viewModel.updateIn()
            viewModel.stockTransferScanTotalCount()

            val barcodeEntry = view?.findViewById<EditText>(R.id.etBarcodeEntry)
            if (barcodeEntry?.isFocused == false) barcodeEntry.requestFocus()
            hideKeyboard()

            val antennaRfConfig: Antennas.AntennaRfConfig =
                BaseViewModel.rfidModel.rfidReader!!.Config.Antennas.getAntennaRfConfig(1)
            antennaRfConfig.transmitPowerIndex = Settings.outboundRFIDPower
            BaseViewModel.rfidModel.rfidReader!!.Config.Antennas.setAntennaRfConfig(1, antennaRfConfig)
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "onResume error", ex)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateOut()
    }

    private fun setupStoreCodeSpinner(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.spinnerStoreCode)

        viewModel.storeCodeList.observe(viewLifecycleOwner) { list ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                list.map { it.storeFullName ?: it.storeCode ?: "" }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            val selectedIndex = list.indexOf(viewModel.selectedStoreCode)
            if (selectedIndex >= 0) spinner.setSelection(selectedIndex)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                val list = viewModel.storeCodeList.value ?: return
                if (pos < list.size) viewModel.selectedStoreCode = list[pos]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewModel.isLocationPickerVisible.observe(viewLifecycleOwner) { enabled ->
            spinner.isEnabled = enabled
            view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardStoreCode)
                .alpha = if (enabled) 1f else 0.5f
        }
    }

    private fun setupLocationSpinner(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.spinnerLocation)

        viewModel.locationList.observe(viewLifecycleOwner) { list ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                list.map { it.locationName ?: "" }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            val selectedIndex = list.indexOf(viewModel.selectedLocation)
            if (selectedIndex >= 0) spinner.setSelection(selectedIndex)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                val list = viewModel.locationList.value ?: return
                if (pos < list.size) viewModel.selectedLocation = list[pos]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewModel.isLocationPickerVisible.observe(viewLifecycleOwner) { enabled ->
            spinner.isEnabled = enabled
            view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardLocation)
                .alpha = if (enabled) 1f else 0.5f
        }
    }

    private fun setupLocationSaveButton(view: View) {
        view.findViewById<Button>(R.id.btnLocationSave).setOnClickListener {
            viewModel.outboundLocationSave()
        }
        viewModel.isLocationPickerVisible.observe(viewLifecycleOwner) { enabled ->
            view.findViewById<Button>(R.id.btnLocationSave).isEnabled = enabled
        }
    }

    private fun setupScanOptions(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvScanOptions)
        scanOptionAdapter = ScanOptionAdapter { model: ScanOptionModel ->
            viewModel.scanOptionSetting(model)
        }
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = scanOptionAdapter

        viewModel.scanOptions.observe(viewLifecycleOwner) { options ->
            scanOptionAdapter.submitList(options?.toList())
        }
    }

    private fun setupTextEntry(view: View) {
        val llRow  = view.findViewById<LinearLayout>(R.id.llTextInputRow)
        val etCode = view.findViewById<EditText>(R.id.etProductIDCode)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        viewModel.isTextBoxVisible.observe(viewLifecycleOwner) { visible ->
            llRow.visibility = if (visible) View.VISIBLE else View.GONE
        }

        etCode.addTextChangedListener { text ->
            if (isClearingProductCode) return@addTextChangedListener
            viewModel.productIDCode = text?.toString() ?: ""
        }

        // Observe LiveData to clear EditText when ViewModel resets productIDCode
        viewModel.productIDCodeLive.observe(viewLifecycleOwner) { code ->
            if (code.isEmpty() && etCode.text.isNotEmpty()) {
                isClearingProductCode = true
                etCode.setText("")
                isClearingProductCode = false
            }
        }

        btnSave.setOnClickListener { viewModel.addInboundItem() }
    }

    private fun setupOutboundList(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvOutboundItems)
        outboundItemAdapter = StockTransferItemAdapter(
            onDelete = { item, position ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Are you sure you want to Remove this from List?")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.confirmDeleteTag(item)
                    }
                    .setNegativeButton("No") { _, _ ->
                        outboundItemAdapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        outboundItemAdapter.notifyItemChanged(position)
                    }
                    .show()
            }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = outboundItemAdapter
        outboundItemAdapter.attachSwipeTo(rv)

        viewModel.allOutboundItems.observe(viewLifecycleOwner) { items ->
            Log.d("OUTBOUND", "allOutboundItems observer: ${items?.size} items")
            outboundItemAdapter.submitList(items?.toList())
        }
    }

    private fun setupBarcodeEntry(view: View) {
        val barcodeEntry = view.findViewById<EditText>(R.id.etBarcodeEntry)

        barcodeEntry.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && viewModel.isBarcodeViewVisible.value == true) {
                barcodeEntry.requestFocus()
            }
        }

        barcodeEntry.addTextChangedListener { text ->
            if (isClearingBarcode) return@addTextChangedListener
            val value = text?.toString() ?: ""
            if (value.isNotEmpty()) {
                Log.d("OUTBOUND", "barcodeEntry changed: $value")
                viewModel.addBarcodes(value)
            }
        }

        viewModel.isBarcodeViewVisible.observe(viewLifecycleOwner) { visible ->
            barcodeEntry.visibility = if (visible) View.VISIBLE else View.GONE
            if (visible) barcodeEntry.requestFocus()
        }

        viewModel.clearBarcodeField.observe(viewLifecycleOwner) { shouldClear ->
            if (shouldClear == true) {
                isClearingBarcode = true
                barcodeEntry.setText("")
                isClearingBarcode = false
                viewModel.onBarcodeFieldCleared()
            }
        }
    }

    private fun setupNextButton(view: View) {
        view.findViewById<Button>(R.id.btnNext).setOnClickListener {
            viewModel.nextOutboundPreviewList()
        }
    }

    private fun observeViewModel(view: View) {
        viewModel.stockTransferScannedQTYTotalCount.observe(viewLifecycleOwner) { count ->
            view.findViewById<TextView>(R.id.tvHeaderScannedQTY).text = "Scanned\nQTY\n[$count]"
        }

        viewModel.alertMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                viewModel.onAlertShown()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage(message)
                    .setPositiveButton("Cancel", null)
                    .show()
            }
        }

        viewModel.navigateToSettings.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                viewModel.onNavigateToSettingsHandled()
                PasscodeFragment().show(parentFragmentManager, "PasscodePopup")
            }
        }

        viewModel.navigateToPreview.observe(viewLifecycleOwner) { args ->
            args ?: return@observe
            viewModel.onNavigateToPreviewHandled()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun hideKeyboard() {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun focusBarcodeEntry(view: View) {
        view.findViewById<EditText>(R.id.etBarcodeEntry)?.requestFocus()
        hideKeyboard()
    }
}