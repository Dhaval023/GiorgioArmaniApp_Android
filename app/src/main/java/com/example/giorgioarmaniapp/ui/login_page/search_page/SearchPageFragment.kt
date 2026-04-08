package com.example.giorgioarmaniapp.ui.login_page.search_page

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.giorgioarmaniapp.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class SearchPageFragment : Fragment() {

    private val viewModel: SearchPageViewModel by viewModels()

    private lateinit var tagInput: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var tvDistance: TextView
    private lateinit var fillBar: View
    private lateinit var loadingOverlay: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_search_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tagInput = view.findViewById(R.id.tagPatternEntry)
        btnSave = view.findViewById(R.id.btnSave)
        tvDistance = view.findViewById(R.id.tvRelativeDistance)
        fillBar = view.findViewById(R.id.blackFillBar)
        loadingOverlay = view.findViewById(R.id.loadingLayout)

        viewModel.isEnabledTextGTIN.observe(viewLifecycleOwner) {
            tagInput.isEnabled = it
            btnSave.isEnabled = it
        }

        viewModel.relativeDistance.observe(viewLifecycleOwner) {
            tvDistance.text = it
        }

        viewModel.distanceBoxHeight.observe(viewLifecycleOwner) {
            val px = (it * resources.displayMetrics.density).toInt()
            fillBar.layoutParams.height = px
            fillBar.requestLayout()
        }

        viewModel.alertEvent.observe(viewLifecycleOwner) {
            it ?: return@observe
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(it)
                .setPositiveButton("OK", null)
                .show()
            viewModel.onAlertHandled()
        }

        viewModel.confirmEvent.observe(viewLifecycleOwner) {
            it ?: return@observe
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(it.first)
                .setPositiveButton("Yes") { _, _ -> viewModel.onSearchConfirmed() }
                .setNegativeButton("No", null)
                .show()
            viewModel.onConfirmHandled()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.navigateToSettings.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                viewModel.onNavigateToSettingsHandled()
                findNavController().navigate(R.id.nav_passcode)
            }
        }

        tagInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.textGTINValue = s.toString()
            }
        })

        btnSave.setOnClickListener {
            viewModel.searchTag()
        }
    }

    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

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

    override fun onResume() {
        super.onResume()
        setupToolbar()
        if (viewModel.isEnabledTextGTIN.value == false) {
            viewModel.updateIn()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateOut()
    }
}