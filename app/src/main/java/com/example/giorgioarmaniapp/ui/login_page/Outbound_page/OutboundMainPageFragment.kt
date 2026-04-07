package com.example.giorgioarmaniapp.ui.login_page.Outbound_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.enums.OutboundMenuEnums
import com.google.android.material.appbar.MaterialToolbar

class OutboundMainPageFragment : Fragment() {

    private val viewModel: OutboundMainPageViewModel by viewModels()

    private lateinit var menuAdapter: OutboundMenuAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true) // enable toolbar settings icon
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

        setupToolbar()
        setupMenuList(view)
        observeViewModel()
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
        try {
            viewModel.stopReadingMode()
        } catch (ex: Exception) {
            // silent catch
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_outbound_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Originally: SettingCommand → NavigateToSettingsPage()
                viewModel.navigateToSettingsPage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupMenuList(view: View) {
        val rvMenu = view.findViewById<RecyclerView>(R.id.rvOutboundMenuItems)

        menuAdapter = OutboundMenuAdapter { model ->
            // Originally: OutboundMenuItemTapCommand with CommandParameter={Binding .}
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
                    // Originally: await App.Current.MainPage.Navigation.PushAsync(new ConsolidatedStockTransferPage())
                    // findNavController().navigate(R.id.action_outboundMainPage_to_consolidatedStockTransferPage)
                }
            }
        }

        viewModel.navigateToSettings.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                viewModel.onNavigateToSettingsHandled()
                findNavController().navigate(R.id.nav_passcode)
            }
        }
    }
}
