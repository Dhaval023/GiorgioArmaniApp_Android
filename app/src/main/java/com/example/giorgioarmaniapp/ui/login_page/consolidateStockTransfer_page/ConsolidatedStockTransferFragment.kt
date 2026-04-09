package com.example.giorgioarmaniapp.ui.login_page.consolidateStockTransfer_page

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.example.giorgioarmaniapp.models.OutBoundStockModel
import com.example.giorgioarmaniapp.ui.login_page.popup.PasscodeFragment
import com.google.android.material.appbar.MaterialToolbar

class ConsolidatedStockTransferFragment : Fragment() {

    private lateinit var viewModel: ConsolidatedStockTransferViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoData: TextView
    private lateinit var adapter: PendingOutboundAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_consolidated_stock_transfer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ConsolidatedStockTransferViewModel::class.java]

        recyclerView = view.findViewById(R.id.rvPendingList)
        tvNoData = view.findViewById(R.id.tvNoData)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.navigateToSettings.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                viewModel.onNavigateToSettingsHandled()
                PasscodeFragment().show(parentFragmentManager, "PasscodePopup")
            }
        }
        observeData()
        setupMenu()
        viewModel.loadData()
    }

    private fun observeData() {

        adapter = PendingOutboundAdapter(emptyList()) { item ->
            navigateToNext(item)
        }
        recyclerView.adapter = adapter
        
        viewModel.pendingList.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                recyclerView.visibility = View.GONE
                tvNoData.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                tvNoData.visibility = View.GONE
                adapter = PendingOutboundAdapter(list) { item ->
                    navigateToNext(item)
                }
                recyclerView.adapter = adapter
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                showErrorPopup(message)
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun showErrorPopup(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToNext(item: OutBoundStockModel.PendingOutboundResult) {
        val bundle = Bundle().apply {
            putSerializable("data", item)
        }
        try {
//            findNavController().navigate(
//                R.id.action_consolidated_to_job,
//                bundle
//            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun onResume() {
        super.onResume()
        setupToolbar()
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
}