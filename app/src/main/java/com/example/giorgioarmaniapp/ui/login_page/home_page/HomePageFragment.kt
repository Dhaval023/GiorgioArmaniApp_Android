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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giorgioarmaniapp.R
import com.google.android.material.appbar.MaterialToolbar

class HomePageFragment : Fragment() {

    private val viewModel: HomePageViewModel by viewModels()
    private lateinit var adapter: HomePageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        val txtStoreName = view.findViewById<TextView>(R.id.txtStoreName)
        val txtEmployeeName = view.findViewById<TextView>(R.id.txtEmployeeName)
        val recyclerView = view.findViewById<RecyclerView>(R.id.menuRecyclerView)

        // Setup RecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        
        // Observe ViewModel data
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

        // Setup Menu (Settings Icon)
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

        val originalDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.log_off)
        if (originalDrawable != null) {
            val iconSizePx = (30 * resources.displayMetrics.density).toInt() // slightly smaller than 24dp
            val horizontalInset = (4 * resources.displayMetrics.density).toInt()
            val insetDrawable = InsetDrawable(originalDrawable, horizontalInset, horizontalInset, horizontalInset, horizontalInset)
            insetDrawable.setTint(ContextCompat.getColor(requireContext(), android.R.color.white))

            val layerDrawable = android.graphics.drawable.LayerDrawable(arrayOf(insetDrawable))
            layerDrawable.setLayerSize(0, iconSizePx, iconSizePx)

            toolbar.navigationIcon = layerDrawable
            toolbar.navigationIcon?.setTint(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
        toolbar.setNavigationOnClickListener {
            viewModel.logout { success ->
                if (success) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        
        toolbar.contentInsetStartWithNavigation = 0
    }
}