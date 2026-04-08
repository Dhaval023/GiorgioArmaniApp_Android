package com.example.giorgioarmaniapp.ui.login_page.popup

import com.example.giorgioarmaniapp.ui.login_page.home_page.HomePageFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.giorgioarmaniapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class PasscodeFragment : DialogFragment() {

    private val viewModel: PasscodeViewModel by viewModels()

    private lateinit var passcodeEntry: TextInputEditText
    private lateinit var btnOk: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var loadingOverlay: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_passcode_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passcodeEntry = view.findViewById(R.id.PasscodeEntry)
        btnOk         = view.findViewById(R.id.btnOk)
        btnCancel     = view.findViewById(R.id.btnCancel)
        loadingOverlay = view.findViewById(R.id.loadingLayout)

        // Mirrors CloseCommand
        btnCancel.setOnClickListener {
            dismiss()
        }

        btnOk.setOnClickListener {
            val passcode = passcodeEntry.text?.toString() ?: ""
            viewModel.submitPasscode(passcode)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnOk.isEnabled     = !isLoading
            btnCancel.isEnabled = !isLoading
        }

        viewModel.alertEvent.observe(viewLifecycleOwner) { event ->
            event ?: return@observe
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage(event)
                .setNegativeButton("Cancel", null)
                .show()
            viewModel.onAlertHandled()
        }

        viewModel.navigateToSettings.observe(viewLifecycleOwner) { success ->
            if (success) {
                dismiss()
                parentFragmentManager.let {
                    (parentFragment as? HomePageFragment)
                        ?.findNavController()
                        ?.navigate(R.id.action_homePage_to_settingPage)
                }
                viewModel.onNavigateHandled()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}