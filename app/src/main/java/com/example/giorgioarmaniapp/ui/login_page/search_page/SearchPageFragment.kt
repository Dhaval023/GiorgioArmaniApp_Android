package com.example.giorgioarmaniapp.ui.login_page.search_page

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.giorgioarmaniapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class SearchPageFragment : Fragment() {

    private val viewModel: SearchPageViewModel by viewModels()

    private lateinit var tagPatternEntry: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var tvRelativeDistance: TextView
    private lateinit var blackFillBar: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tagPatternEntry    = view.findViewById(R.id.tagPatternEntry)
        btnSave            = view.findViewById(R.id.btnSave)
        tvRelativeDistance = view.findViewById(R.id.tvRelativeDistance)
        blackFillBar       = view.findViewById(R.id.blackFillBar)

        // Mirrors IsEnabled binding
        viewModel.isEnabledTextGTIN.observe(viewLifecycleOwner) { isEnabled ->
            tagPatternEntry.isEnabled = isEnabled
        }

        // Mirrors RelativeDistance label
        viewModel.relativeDistance.observe(viewLifecycleOwner) { distance ->
            tvRelativeDistance.text = distance
        }

        // Mirrors DistanceBox binding
        viewModel.distanceBoxHeight.observe(viewLifecycleOwner) { heightDp ->
            val heightPx = (heightDp * resources.displayMetrics.density).toInt()
            val params = blackFillBar.layoutParams
            params.height = heightPx
            blackFillBar.layoutParams = params
        }

        // Mirrors DisplayAlert("Alert", ...)
        viewModel.alertEvent.observe(viewLifecycleOwner) { message ->
            message ?: return@observe
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage(message)
                .setNegativeButton("Cancel", null)
                .show()
            viewModel.onAlertHandled()
        }

        // Mirrors DisplayAlert("Search", "Are you sure...")
        viewModel.confirmEvent.observe(viewLifecycleOwner) { event ->
            event ?: return@observe
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Search")
                .setMessage(event.first)
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.onSearchConfirmed() // wires updateIn + disables entry
                    viewModel.onConfirmHandled()
                }
                .setNegativeButton("No") { _, _ ->
                    viewModel.onConfirmHandled()
                }
                .show()
        }

        // Mirrors TextChanged="Handle_TextChanged"
        tagPatternEntry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.textGTINValue = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Mirrors SaveCommand
        btnSave.setOnClickListener {
            viewModel.searchTag()
        }
    }

    // Mirrors OnAppearing() — NOT calling updateIn here, only after search confirmed
    override fun onResume() {
        super.onResume()
        // Ensure we are listening if a search was already in progress
        if (!viewModel.isEnabledTextGTIN.value!!) {
            viewModel.updateIn()
        }
    }

    // Mirrors OnDisappearing()
    override fun onPause() {
        super.onPause()
        viewModel.updateOut()
    }
}