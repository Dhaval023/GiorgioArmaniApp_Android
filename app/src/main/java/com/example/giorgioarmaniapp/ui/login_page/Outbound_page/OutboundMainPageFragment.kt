package com.example.giorgioarmaniapp.ui.login_page.Outbound_page

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.enums.OutboundMenuEnums
import com.google.android.material.appbar.MaterialToolbar

class OutboundMainPageFragment : Fragment() {

    private val viewModel: OutboundMainPageViewModel by viewModels()

    private lateinit var menuAdapter: OutboundMenuAdapter
    private lateinit var loadingOverlay: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_outbound_main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingOverlay = view.findViewById(R.id.loadingLayout)
        setupMenuList(view)
        observeViewModel()

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

    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
        try {
            viewModel.stopReadingMode()
        } catch (ex: Exception) {
            // silent catch
        }
    }

    private fun setupMenuList(view: View) {
        val rvMenu = view.findViewById<RecyclerView>(R.id.rvOutboundMenuItems)

        menuAdapter = OutboundMenuAdapter { model ->
            viewModel.outboundMenuItemTap(model)
        }

        rvMenu.layoutManager = LinearLayoutManager(requireContext())
        rvMenu.adapter = menuAdapter

        viewModel.outboundMenuItems.observe(viewLifecycleOwner) { items ->
            menuAdapter.submitList(items)
        }
    }


    private fun observeViewModel() {


        viewModel.navigateTo.observe(viewLifecycleOwner) { destination ->
            destination ?: return@observe
            viewModel.onNavigateToHandled()

            when (destination) {
                OutboundMenuEnums.STOCKTRANSFER -> {
                    findNavController().navigate(R.id.action_outboundMainPage_to_stockTransfer)
                }
                OutboundMenuEnums.CONSOLIDATEDSTOCKTRANSFER -> {
                }
            }
        }

        viewModel.navigateToSettings.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                viewModel.onNavigateToSettingsHandled()
                findNavController().navigate(R.id.nav_passcode)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}