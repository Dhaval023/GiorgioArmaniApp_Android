package com.example.giorgioarmaniapp.models.statics

import com.example.giorgioarmaniapp.models.enums.HomeMenuEnums

class HomePageMenuModel {
    var title: String? = null
    var targetType: Class<*>? = null
    var homeMenuNavType: HomeMenuEnums? = null
    var itemTapCommand: (() -> Unit)? = null
}