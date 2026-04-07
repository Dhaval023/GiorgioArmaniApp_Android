package com.example.giorgioarmaniapp.models.statics

import com.example.giorgioarmaniapp.models.enums.OutboundMenuEnums

data class OutboundMainPageMenuModel(
    var title: String = "",
    var outboundMenuNavType: OutboundMenuEnums = OutboundMenuEnums.STOCKTRANSFER,
    var onTap: (() -> Unit)? = null
)