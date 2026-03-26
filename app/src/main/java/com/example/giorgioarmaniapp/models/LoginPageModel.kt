package com.example.giorgioarmaniapp.models

data class LoginData(
    val user_id: Int = 0,
    val user_type: String? = null,
    val empName: String? = null,
    val store_id: String? = null,
    val store_name: String? = null
)

data class GetLoginDetailsResponse(
    val success: Int = 0,
    val msg: String? = null,
    val data: List<LoginData>? = null
)