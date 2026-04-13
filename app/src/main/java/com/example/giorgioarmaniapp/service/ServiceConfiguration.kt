package com.example.giorgioarmaniapp.service

object ServiceConfiguration {

    // ================= URL =================

    // var URL = "https://rfid-au.giorgioarmani.tech/"

    // var URL = "http://bcdsdemo.com/"

    // var URL = "http://project-alb-1490102676.ap-southeast-2.elb.amazonaws.com/"

    // var URL = "http://bcdsdemo.com/"
    // "https://rfid-au.giorgioarmani.tech/"
    // "http://project-alb-1490102676.ap-southeast-2.elb.amazonaws.com/"
    // "http://bcdsdemo.com/"
    // "http://3.104.166.169/"
    // "http://54.206.126.172/"

    // var URL = "http://project-alb-1490102676.ap-southeast-2.elb.amazonaws.com/"
    // "http://bcdsdemo.com/"
    // "https://rfid-au.giorgioarmani.tech/"
    // "http://project-alb-1490102676.ap-southeast-2.elb.amazonaws.com/"
    // "http://bcdsdemo.com/"
    // "http://3.104.166.169/"
    // "http://54.206.126.172/"

    var URL: String =
        "http://project-alb-1490102676.ap-southeast-2.elb.amazonaws.com/"

    // ================= APIs =================

    // Login API
    const val postLoginURL =
        "api.php?c=user&m=login&uname=%s&password=%s&deviceType=%s"

    // Inbound APIs
    const val getInboundPendingListURL =
        "api.php?c=inbound&m=pending&storeCode=%s"

    const val postInboundSubmitListURL =
        "api.php?c=inbound&m=submit&userID=%s"

    // Outbound APIs
    const val getOutboundListURL =
        "api.php?c=stores&m=exceptList&storeCode=%s"

    // Main API for Outbound Submit
    const val postOutboundSubmitListURL =
        "api.php?c=outbound&m=submit&fromStoreCode=%s&toStoreCode=%s&userID=%s&transferType=%s&id=%s&toStoreLocation=%s"

    // Temp API for ConsolidatedST
    const val postOutboundConsolidatedSTSubmitListURL =""
//        "api.php?c=outbound&m=submit&fromStoreCode=%s&toStoreCode=%s&userID=%s&transferType=%s&id=%s"

    const val getOutboundPendingListURL =
        "api.php?c=outbound&m=pending&storeCode=%s"

    // StockTake APIs
    const val getStockTakePendingListURL =
        "api.php?c=soh&m=pending&storeCode=%s&category=%s&brand=%s&gender=%s"

    const val postSubmitStockTakeListURL =
        "api.php?c=soh&m=submit&userID=%s"

    const val postSubmitStockTakeFileURL =
        "api.php?c=soh&m=mail&fileName=%s"

    // GTIN Pattern API
    const val postGTINPatternListURL =
        "api.php?c=gis1&m=listAll"

    // Passcode API
    const val postPasscodeURL =
        "api.php?c=passcode&m=checkCode&code=%s"


    // public static string GetSAPStockTakeListURL =
    // "https://apiarmanidev.gateway.webmethodscloud.de/gateway/GA_TEMERA_FMS_RFID_CYCLE_COUNT_IN/1/ZA_RFID_IF004_CC?$format=json&$filter=Site eq '{0}' and({1})&sap-language=EN&sap-client=100";

    // Example:
    // "https://apiarmanidev.gateway.webmethodscloud.de/...Site eq 'AS02' and(GTINCode eq '8054524710824' ...)"

    // New Stock Take Selection List API
    const val postStockTakeSelectionListURL =
        "api.php?c=soh&m=getFilters&storeCode=%s"
}
