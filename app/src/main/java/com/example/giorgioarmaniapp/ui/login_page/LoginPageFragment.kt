package com.example.giorgioarmaniapp.ui.login_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.giorgioarmaniapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class LoginPageFragment : Fragment() {

    private val viewModel: LoginPageViewModel by viewModels()

    private lateinit var usernameEntry: TextInputEditText
    private lateinit var passwordEntry: TextInputEditText
    private lateinit var loginButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameEntry = view.findViewById(R.id.UsernameEntry)
        passwordEntry = view.findViewById(R.id.PasswordEntry)
        loginButton = view.findViewById(R.id.LoginButton)

        loginButton.setOnClickListener {
            viewModel.usernameText = usernameEntry.text?.toString() ?: ""
            viewModel.passwordText = passwordEntry.text?.toString() ?: ""

            val hasInternet = isInternetAvailable()
            viewModel.login(hasInternet)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginUiState.Loading -> showLoading(true)

                is LoginUiState.Success -> {
                    showLoading(false)
                    // Navigate to HomePage
                    findNavController().navigate(R.id.action_loginPage_to_homePage)
                }

                is LoginUiState.Error -> {
                    showLoading(false)
                    showAlert(state.message)
                }

                is LoginUiState.NoInternet -> {
                    showLoading(false)
                    showAlert("No internet connection. Please check your network.")
                }

                else -> showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        loginButton.isEnabled = !show
    }

    private fun showAlert(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage(message)
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isInternetAvailable(): Boolean {
        val cm = requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager
        val network = cm.activeNetworkInfo
        @Suppress("DEPRECATION")
        return network != null && network.isConnected
    }
}