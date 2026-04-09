package com.example.giorgioarmaniapp.ui.login_page.stocktake_page

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.service.RestService
import kotlinx.coroutines.launch

class StockTakeSelectionViewModel : ViewModel() {

    val genderList = MutableLiveData<List<String>>()
    val categoryList = MutableLiveData<List<String>>()
    val brandList = MutableLiveData<List<String>>()

    val selectedGender = MutableLiveData<String>()
    val selectedCategory = MutableLiveData<String>()
    val selectedBrand = MutableLiveData<String>()

    private val restService = RestService()

    fun loadData() {
        viewModelScope.launch {
            try {
                val response = restService.stockTakeSelectionList(Settings.storeId ?: "")

                response?.let {
                    genderList.value = listOf("None") + it.results.gender.filter { item -> item != "None" }
                    categoryList.value = listOf("None") + it.results.category.filter { item -> item != "None" }
                    brandList.value = listOf("None") + it.results.brands.filter { item -> item != "None" }

                    loadDefaultSelection()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadDefaultSelection() {
        selectedGender.value = genderList.value?.firstOrNull()
        selectedCategory.value = categoryList.value?.firstOrNull()
        selectedBrand.value = brandList.value?.firstOrNull()
    }
}