package com.example.giorgioarmaniapp.ui.login_page.home_page

import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.ui.login_page.popup.PasscodeFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomePageFragment : Fragment() {

    private val viewModel: HomePageViewModel by viewModels()
    private lateinit var adapter: HomePageAdapter

    private lateinit var loadingOverlay: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtStoreName = view.findViewById<TextView>(R.id.txtStoreName)
        val txtEmployeeName = view.findViewById<TextView>(R.id.txtEmployeeName)
        val recyclerView = view.findViewById<RecyclerView>(R.id.menuRecyclerView)
        loadingOverlay = view.findViewById(R.id.loadingLayout)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        viewModel.menuItems.observe(viewLifecycleOwner) { items ->
            adapter = HomePageAdapter(items) { model ->
                viewModel.menuItemTap(model)
            }
            recyclerView.adapter = adapter
        }

        viewModel.storeName.observe(viewLifecycleOwner) { name ->
            txtStoreName.text = name
        }

        viewModel.employeeName.observe(viewLifecycleOwner) { name ->
            txtEmployeeName.text = name
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.navigateTo.observe(viewLifecycleOwner) { destination ->
            destination ?: return@observe
            when (destination) {
                "PasscodePopup" -> {
                    PasscodeFragment().show(parentFragmentManager, "PasscodePopup")
                    viewModel.onNavigationHandled()
                }
                "SearchPage" -> {
                    findNavController().navigate(R.id.action_homePage_to_searchPage)
                    viewModel.onNavigationHandled()
                }
                "PendingInboundPage" -> {
                    findNavController().navigate(R.id.action_homePage_to_inboundPage)
                    viewModel.onNavigationHandled()
                }
                "OutboundMainPage" -> {
                    findNavController().navigate(R.id.action_homePage_to_outboundMainPage)
                    viewModel.onNavigationHandled()
                }
                "StockTakeSelectionPage" -> {
                    viewModel.onNavigationHandled()
                }
            }
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

    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        val originalDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.log_off)
        if (originalDrawable != null) {
            val iconSizePx = (40 * resources.displayMetrics.density).toInt()
            val horizontalInset = (4 * resources.displayMetrics.density).toInt()
            val insetDrawable = InsetDrawable(
                originalDrawable,
                horizontalInset, horizontalInset,
                horizontalInset, horizontalInset
            )
            insetDrawable.setTint(ContextCompat.getColor(requireContext(), android.R.color.white))

            val layerDrawable = android.graphics.drawable.LayerDrawable(arrayOf(insetDrawable))
            layerDrawable.setLayerSize(0, iconSizePx, iconSizePx)

            toolbar.navigationIcon = layerDrawable
            toolbar.navigationIcon?.setTint(
                ContextCompat.getColor(requireContext(), android.R.color.white)
            )
        }

        toolbar.setNavigationOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.logout { success ->
                        if (success) {
                            findNavController().navigate(
                                R.id.action_homePage_to_loginPage,
                                null,
                                androidx.navigation.NavOptions.Builder()
                                    .setPopUpTo(R.id.nav_login, true)
                                    .build()
                            )
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }
}
