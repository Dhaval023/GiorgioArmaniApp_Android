package com.example.giorgioarmaniapp.ui.login_page.home_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.giorgioarmaniapp.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class SettingPageFragment : Fragment() {

    private val viewModel: SettingPageViewModel by viewModels()

    private lateinit var inboundEntry: TextInputEditText
    private lateinit var outboundEntry: TextInputEditText
    private lateinit var stocktakeEntry: TextInputEditText
    private lateinit var btnSetInbound: MaterialButton
    private lateinit var btnSetOutbound: MaterialButton
    private lateinit var btnSetStocktake: MaterialButton
    private lateinit var tvVersion: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_setting_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        inboundEntry    = view.findViewById(R.id.InboundEntry)
        outboundEntry   = view.findViewById(R.id.OutboundEntry)
        stocktakeEntry  = view.findViewById(R.id.StocktakeEntry)
        btnSetInbound   = view.findViewById(R.id.btnSetInbound)
        btnSetOutbound  = view.findViewById(R.id.btnSetOutbound)
        btnSetStocktake = view.findViewById(R.id.btnSetStocktake)
        tvVersion       = view.findViewById(R.id.tvVersion)

        viewModel.inboundPower.observe(viewLifecycleOwner) { value ->
            inboundEntry.setText(value)
        }
        viewModel.outboundPower.observe(viewLifecycleOwner) { value ->
            outboundEntry.setText(value)
        }
        viewModel.stocktakePower.observe(viewLifecycleOwner) { value ->
            stocktakeEntry.setText(value)
        }
        viewModel.appVersion.observe(viewLifecycleOwner) { version ->
            tvVersion.text = version
        }

        viewModel.alertEvent.observe(viewLifecycleOwner) { event ->
            event ?: return@observe
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(event.first)
                .setMessage(event.second)
                .setPositiveButton("Ok") { _, _ -> viewModel.onAlertHandled() }
                .show()
        }

        viewModel.confirmEvent.observe(viewLifecycleOwner) { event ->
            event ?: return@observe
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmation")
                .setMessage(event.first)
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.onConfirmed(event.second)
                    viewModel.onConfirmHandled()
                }
                .setNegativeButton("No") { _, _ ->
                    viewModel.onConfirmHandled()
                }
                .show()
        }

        btnSetInbound.setOnClickListener {
            viewModel.onSetInboundClicked(inboundEntry.text?.toString() ?: "")
        }
        btnSetOutbound.setOnClickListener {
            viewModel.onSetOutboundClicked(outboundEntry.text?.toString() ?: "")
        }
        btnSetStocktake.setOnClickListener {
            viewModel.onSetStockTakeClicked(stocktakeEntry.text?.toString() ?: "")
        }
    }
}