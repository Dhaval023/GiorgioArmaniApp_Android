package com.example.giorgioarmaniapp.models

import com.google.gson.annotations.SerializedName

class PasscodeModel {

    class ResponsePasscodeModel {
        @SerializedName("success")
        var success: Int = 0

        @SerializedName("msg")
        var msg: String? = null
    }
}