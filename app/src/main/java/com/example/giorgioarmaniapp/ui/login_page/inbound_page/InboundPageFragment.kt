package com.example.giorgioarmaniapp.ui.login_page.inbound_page

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
import com.example.giorgioarmaniapp.models.InboundPendingListModel
import com.example.giorgioarmaniapp.models.statics.ScanOptionModel
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zebra.rfid.api3.Antennas

class InboundPageFragment : Fragment() {

    private val viewModel: InboundPageViewModel by viewModels()

    // Adapters — declared as lateinit; assigned in setup functions
    private lateinit var scanOptionAdapter: ScanOptionAdapter
    private lateinit var inboundItemAdapter: InboundItemAdapter
    private lateinit var loadingOverlay: View

    // Holds the delivery data passed to this screen.
    // Originally: InboundPage(InboundPendingListResult arg)
    private var pendingInboundData: InboundPendingListModel.InboundPendingListResult? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve argument — equivalent of constructor parameter `InboundPendingListResult arg`
        arguments?.let { args ->
            pendingInboundData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                args.getParcelable(
                    ARG_PENDING_INBOUND_DATA,
                    InboundPendingListModel.InboundPendingListResult::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                args.getParcelable(ARG_PENDING_INBOUND_DATA)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inbound_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingOverlay = view.findViewById(R.id.loadingLayout)
        viewModel.updateBarcodeOut()
        hideKeyboard()

        if (pendingInboundData != null) {
            viewModel.pendingInboundData = pendingInboundData
        }

        setupScanOptions(view)
        setupInboundList(view)
        setupTextEntry(view)
        setupBarcodeEntry(view)
        setupSubmitButton(view)
        observeViewModel(view)

        // Originally: bentry.Focus()
        focusBarcodeEntry(view)

        // Originally: viewmodel.ActiveOnFocus = ((obj) => { bentry.Focus(); });
        viewModel.activeOnFocus = { focusBarcodeEntry(view) }

        hideKeyboard()
    }

    /**
     * Converted from: protected override void OnAppearing()
     */
    override fun onResume() {
        super.onResume()
        try {
            setupToolbar()
            viewModel.updateIn()
            viewModel.updateBarcodeIn()
            viewModel.inboundScanTotalCount()
            hideKeyboard()

            val barcodeEntry = view?.findViewById<EditText>(R.id.etBarcodeEntry)
            if (barcodeEntry?.isFocused == false) {
                barcodeEntry.requestFocus()
            }
            hideKeyboard()

            // Set RFID antenna transmit power from Settings
            val antennaRfConfig: Antennas.AntennaRfConfig =
                BaseViewModel.rfidModel.rfidReader!!.Config.Antennas.getAntennaRfConfig(1)
            antennaRfConfig.transmitPowerIndex = Settings.inboundRFIDPower
            BaseViewModel.rfidModel.rfidReader!!.Config.Antennas.setAntennaRfConfig(1, antennaRfConfig)
        } catch (ex: Exception) {
            // silent catch
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateOut()
        viewModel.updateBarcodeOut()

        // Changes done by Mitesh on 16/05/2023 to fix bug DB00011
        // viewModel.newAllItems.value?.clear()
        hideKeyboard()
    }

    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        toolbar.title = viewModel.pendingInboundData?.deliveryNumber ?: "Inbound Detail"

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

    private fun setupScanOptions(view: View) {
        val rvScanOptions = view.findViewById<RecyclerView>(R.id.rvScanOptions)
        scanOptionAdapter = ScanOptionAdapter { model: ScanOptionModel ->
            // Originally: Command="{Binding ChangeScanOptionsCommand}" + TapGestureRecognizer
            viewModel.scanOptionSetting(model)
        }
        rvScanOptions.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvScanOptions.adapter = scanOptionAdapter

        viewModel.scanOptions.observe(viewLifecycleOwner) { options ->
            scanOptionAdapter.submitList(options?.toList())
        }
    }

    private fun setupInboundList(view: View) {
        val rvItems = view.findViewById<RecyclerView>(R.id.rvInboundItems)
        inboundItemAdapter = InboundItemAdapter(
            onDelete = { item: InboundPendingListModel.InboundPendingModel ->
                // Originally: DeleteTagCommand → shows confirmation dialog, then deletes
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Warning")
                    .setMessage("Are you sure you want to Remove this from List?")
                    .setPositiveButton("Yes") { _, _ -> viewModel.deleteTag(item) }
                    .setNegativeButton("No", null)
                    .show()
            },
            onMakeEqual = { item: InboundPendingListModel.InboundPendingModel ->
                // Originally: MakeEqualCountTagCommand
                viewModel.makeEqualQTYTag(item)
            }
        )
        rvItems.layoutManager = LinearLayoutManager(requireContext())
        rvItems.adapter = inboundItemAdapter

        viewModel.newAllItems.observe(viewLifecycleOwner) { items ->
            inboundItemAdapter.submitList(items?.toList())
        }
    }

    private fun setupTextEntry(view: View) {
        val llTextInputRow = view.findViewById<LinearLayout>(R.id.llTextInputRow)
        val etProductIDCode = view.findViewById<EditText>(R.id.etProductIDCode)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        viewModel.isTextBoxVisible.observe(viewLifecycleOwner) { visible ->
            llTextInputRow.visibility = if (visible) View.VISIBLE else View.GONE
        }

        etProductIDCode.addTextChangedListener {
            viewModel.productIDCode = it?.toString() ?: ""
        }

        // Originally: Command="{Binding AddInboundItemCommand}"
        btnSave.setOnClickListener {
            viewModel.addInboundItem()
        }
    }

    /**
     * Sets up the hidden barcode entry field.
     * Equivalent of:
     *   XBarcodeEntry x:Name="bentry" IsVisible="false"
     *   Text="{Binding BarcodeOrProductcode}"
     *   Unfocused="bentry_Unfocused"
     *   Focused="bentry_Focused"
     */
    private fun setupBarcodeEntry(view: View) {
        val barcodeEntry = view.findViewById<EditText>(R.id.etBarcodeEntry)

        barcodeEntry.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Originally: void bentry_Focused(...)
                // Hides keyboard, keeps focus, clears keyboard type
                hideKeyboard()
                barcodeEntry.requestFocus()
            } else {
                // Originally: void bentry_Unfocused(...)
                // If barcode mode is active, re-focus to keep scanner ready
                hideKeyboard()
                if (viewModel.isBarcodeViewVisible.value == true) {
                    barcodeEntry.requestFocus()
                }
                hideKeyboard()
            }
        }

        barcodeEntry.addTextChangedListener { text ->
            val value = text?.toString() ?: ""
            if (value.isNotEmpty()) {
                // Triggers addBarcodes() via the barcodeOrProductcode property setter
                viewModel.barcodeOrProductcode = value
            }
        }
    }

    /**
     * Sets up the Submit button at the bottom.
     * Equivalent of: Button Text="Submit" Command="{Binding SaveCommand}"
     */
    private fun setupSubmitButton(view: View) {
        view.findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            viewModel.saveListData()
        }
    }

    // endregion

    // region --- Observers ---

    private fun observeViewModel(view: View) {
        viewModel.inboundExpectedQTYTotalCount.observe(viewLifecycleOwner) { count ->
            view.findViewById<TextView>(R.id.tvHeaderExpectedQTY).text =
                "Expected\nQTY\n[$count]"
        }

        viewModel.inboundScannedQTYTotalCount.observe(viewLifecycleOwner) { count ->
            view.findViewById<TextView>(R.id.tvHeaderScannedQTY).text =
                "Scanned\nQTY\n[$count]"
        }

        // Alert dialogs — replaces Xamarin DisplayAlert("Warning", ..., "Cancel")
        viewModel.alertMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Warning")
                    .setMessage(message)
                    .setPositiveButton("Cancel", null)
                    .show()
            }
        }

        viewModel.submitSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                viewModel.onSubmitSuccessHandled()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Warning")
                    .setMessage("Data are Successfully Submitted")
                    .setPositiveButton("Ok") { _, _ ->
                        findNavController().popBackStack()
                    }
                    .show()
            }
        }

        // Navigate to settings (PasscodePopup)
        // Originally: await App.Current.MainPage.Navigation.PushAsync(new PasscodePopup())
        viewModel.navigateToSettings.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                viewModel.onNavigateToSettingsHandled()
                findNavController().navigate(R.id.action_inboundPage_to_passcodePopup)
            }
        }

        viewModel.isBarcodeViewVisible.observe(viewLifecycleOwner) { visible ->
            val barcodeEntry = view.findViewById<EditText>(R.id.etBarcodeEntry)
            barcodeEntry.visibility = if (visible) View.VISIBLE else View.GONE
            if (visible) barcodeEntry.requestFocus()
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

    // endregion

    companion object {
        const val ARG_PENDING_INBOUND_DATA = "pendingInboundData"

        fun newInstance(
            arg: InboundPendingListModel.InboundPendingListResult
        ): InboundPageFragment {
            return InboundPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PENDING_INBOUND_DATA, arg)
                }
            }
        }
    }
}
