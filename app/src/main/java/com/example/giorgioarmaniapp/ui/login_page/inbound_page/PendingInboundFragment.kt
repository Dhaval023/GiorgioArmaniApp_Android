package com.example.giorgioarmaniapp.ui.login_page.inbound_page

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
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
import com.google.android.material.appbar.MaterialToolbar

class PendingInboundFragment : Fragment(R.layout.fragment_pending_inbound) {

    private val viewModel: PendingInboundViewModel by viewModels()
    private lateinit var adapter: PendingInboundAdapter
    private lateinit var loadingOverlay: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingOverlay = view.findViewById(R.id.loadingLayout)
        setupRecyclerView(view)
        setupSearch(view)
        observeData(view)

        loadData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.inboundSearchText.value = ""
        setupToolbar()
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
                        findNavController().navigate(R.id.action_pendingInbound_to_passcode)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PendingInboundAdapter {
            val bundle = Bundle().apply {
                putParcelable(InboundPageFragment.ARG_PENDING_INBOUND_DATA, it)
                putString("deliveryNumber", it.deliveryNumber)
            }
            findNavController().navigate(R.id.action_pendingInbound_to_inboundPage, bundle)
        }
        recyclerView.adapter = adapter
    }

    private fun setupSearch(view: View) {
        val search = view.findViewById<EditText>(R.id.searchBar)

        search.addTextChangedListener {
            viewModel.filterList(it.toString())
        }
    }

    private fun observeData(view: View) {

        viewModel.pendingInboundList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.isNotFound.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.txtNoData).visibility =
                if (it) View.VISIBLE else View.GONE
        }

        viewModel.isVisibleInboundList.observe(viewLifecycleOwner) {
            view.findViewById<RecyclerView>(R.id.recyclerView).visibility =
                if (it) View.VISIBLE else View.GONE
        }

        // 🔥 LOADER
        viewModel.isLoading.observe(viewLifecycleOwner) {
            showLoading(it)
        }
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun loadData() {
        val storeCode = Settings.storeId
        viewModel.loadInboundList(storeCode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // No need to clear adapter list here if we want to preserve state on back navigation
    }
}
