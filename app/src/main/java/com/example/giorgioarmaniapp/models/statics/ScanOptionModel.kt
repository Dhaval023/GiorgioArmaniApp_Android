package com.example.giorgioarmaniapp.models.statics

data class ScanOptionModel(
    var id: Int = 0,
    var title: String = "",
    var isSelected: Boolean = false,
    var checkImage: String = "unselected"
) {
    fun updateSelected(selected: Boolean) {
        isSelected = selected
        checkImage = if (selected) "selected" else "unselected"
    }
}