package com.example.giorgioarmaniapp.models

import com.google.gson.annotations.SerializedName


class GTINPatternModel {

    data class GTINPattern(
        @SerializedName("id")
        val id: Int,

        @SerializedName("name")
        val name: String
    )

    data class GTINPatternResponse(
        @SerializedName("results")
        val results: List<GTINPattern>?,

        @SerializedName("ERROR")
        val error: String?,

        @SerializedName("SUCCESS")
        val success: String?
    )
}