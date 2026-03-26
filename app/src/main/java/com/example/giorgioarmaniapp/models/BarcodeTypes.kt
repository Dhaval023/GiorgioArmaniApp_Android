package com.example.giorgioarmaniapp.models

class BarcodeTypes {

    companion object {

        private const val ST_NOT_APP = 0x00
        private const val ST_CODE_39 = 0x01
        private const val ST_CODABAR = 0x02
        private const val ST_CODE_128 = 0x03
        private const val ST_D2OF5 = 0x04
        private const val ST_IATA = 0x05
        private const val ST_I2OF5 = 0x06
        private const val ST_CODE93 = 0x07
        private const val ST_UPCA = 0x08
        private const val ST_UPCE0 = 0x09
        private const val ST_EAN8 = 0x0a
        private const val ST_EAN13 = 0x0b
        private const val ST_CODE11 = 0x0c
        private const val ST_CODE49 = 0x0d
        private const val ST_MSI = 0x0e
        private const val ST_EAN128 = 0x0f
        private const val ST_UPCE1 = 0x10
        private const val ST_PDF417 = 0x11
        private const val ST_CODE16K = 0x12
        private const val ST_C39FULL = 0x13
        private const val ST_UPCD = 0x14
        private const val ST_TRIOPTIC = 0x15
        private const val ST_BOOKLAND = 0x16
        private const val ST_COUPON = 0x17
        private const val ST_NW7 = 0x18
        private const val ST_ISBT128 = 0x19
        private const val ST_MICRO_PDF = 0x1a
        private const val ST_DATAMATRIX = 0x1b
        private const val ST_QR_CODE = 0x1c
        private const val ST_MICRO_PDF_CCA = 0x1d
        private const val ST_POSTNET_US = 0x1e
        private const val ST_PLANET_CODE = 0x1f
        private const val ST_CODE_32 = 0x20
        private const val ST_ISBT128_CON = 0x21
        private const val ST_JAPAN_POSTAL = 0x22
        private const val ST_AUS_POSTAL = 0x23
        private const val ST_DUTCH_POSTAL = 0x24
        private const val ST_MAXICODE = 0x25
        private const val ST_CANADIN_POSTAL = 0x26
        private const val ST_UK_POSTAL = 0x27
        private const val ST_MACRO_PDF = 0x28
        private const val ST_MACRO_QR_CODE = 0x29
        private const val ST_MICRO_QR_CODE = 0x2c
        private const val ST_AZTEC = 0x2d
        private const val ST_AZTEC_RUNE_CODE = 0x2E
        private const val ST_FRENCH_LOTTERY = 0x2F
        private const val ST_RSS14 = 0x30
        private const val ST_RSS_LIMITET = 0x31
        private const val ST_RSS_EXPANDED = 0x32
        private const val ST_PARAMETER_FNC3 = 0x33
        private const val ST_4STATE_US = 0x34
        private const val ST_4STATE_US4 = 0x35
        private const val ST_ISSN = 0x36
        private const val ST_SCANLET = 0x37
        private const val ST_CUE_CAT_CODE = 0x38
        private const val ST_MATRIX_2_OF_5 = 0x39
        private const val ST_UPCA_2 = 0x48
        private const val ST_UPCE0_2 = 0x49
        private const val ST_EAN8_2 = 0x4a
        private const val ST_EAN13_2 = 0x4b
        private const val ST_UPCE1_2 = 0x50
        private const val ST_CCA_EAN128 = 0x51
        private const val ST_CCA_EAN13 = 0x52
        private const val ST_CCA_EAN8 = 0x53
        private const val ST_CCA_RSS_EXPANDED = 0x54
        private const val ST_CCA_RSS_LIMITED = 0x55
        private const val ST_CCA_RSS14 = 0x56
        private const val ST_CCA_UPCA = 0x57
        private const val ST_CCA_UPCE = 0x58
        private const val ST_CCC_EAN128 = 0x59
        private const val ST_TLC39 = 0x5A
        private const val ST_CCB_EAN128 = 0x61
        private const val ST_CCB_EAN13 = 0x62
        private const val ST_CCB_EAN8 = 0x63
        private const val ST_CCB_RSS_EXPANDED = 0x64
        private const val ST_CCB_RSS_LIMITED = 0x65
        private const val ST_CCB_RSS14 = 0x66
        private const val ST_CCB_UPCA = 0x67
        private const val ST_CCB_UPCE = 0x68
        private const val ST_SIGNATURE_CAPTURE = 0x69
        private const val ST_MATRIX2OF5_OLD = 0x71
        private const val ST_CHINESE2OF5 = 0x72
        private const val ST_KOREAN2OF5 = 0x73
        private const val ST_UPCA_5 = 0x88
        private const val ST_UPCE0_5 = 0x89
        private const val ST_EAN8_5 = 0x8a
        private const val ST_EAN13_5 = 0x8b
        private const val ST_UPCE1_5 = 0x90
        private const val ST_MACRO_MICRO_PDF = 0x9A
        private const val ST_OCRB = 0xA0
        private const val ST_NEW_COUPON = 0xB4
        private const val ST_HAN_XIN = 0xB7
        private const val ST_GS1_DATAMATRIX = 0xC1
        private const val ST_RFID_RAW = 0xE0
        private const val ST_RFID_URI = 0xE1

        fun getBarcodeTypeName(barcodeType: Int): String {
            return when (barcodeType) {
                ST_NOT_APP -> "Unknown"
                ST_CODE_39 -> "Code 39"
                ST_CODABAR -> "Codabar"
                ST_CODE_128 -> "Code 128"
                ST_D2OF5 -> "Discrete 2 of 5"
                ST_IATA -> "IATA"
                ST_I2OF5 -> "Interleaved 2 of 5"
                ST_CODE93 -> "Code 93"
                ST_UPCA -> "UPCA"
                ST_UPCE0 -> "UPCE 0"
                ST_EAN8 -> "EAN 8"
                ST_EAN13 -> "EAN 13"
                ST_CODE11 -> "Code 11"
                ST_CODE49 -> "Code 49"
                ST_MSI -> "MSI"
                ST_EAN128 -> "EAN 128"
                ST_UPCE1 -> "UPCE 1"
                ST_PDF417 -> "PDF 417"
                ST_CODE16K -> "Code 16K"
                ST_C39FULL -> "Code 39 Full ASCII"
                ST_UPCD -> "UPCD"
                ST_TRIOPTIC -> "Trioptic"
                ST_BOOKLAND -> "Bookland"
                ST_COUPON -> "Coupon Code"
                ST_NW7 -> "NW7"
                ST_ISBT128 -> "ISBT-128"
                ST_MICRO_PDF -> "Micro PDF"
                ST_DATAMATRIX -> "Data Matrix"
                ST_QR_CODE -> "QR Code"
                ST_MICRO_PDF_CCA -> "Micro PDF CCA"
                ST_POSTNET_US -> "Postnet US"
                ST_PLANET_CODE -> "Planet Code"
                ST_CODE_32 -> "Code 32"
                ST_ISBT128_CON -> "ISBT-128 Concat"
                ST_JAPAN_POSTAL -> "Japan Postal"
                ST_AUS_POSTAL -> "Aus Postal"
                ST_DUTCH_POSTAL -> "Dutch Postal"
                ST_MAXICODE -> "Maxicode"
                ST_CANADIN_POSTAL -> "Canada Postal"
                ST_UK_POSTAL -> "UK Postal"
                ST_MACRO_PDF -> "Macro PDF-417"
                ST_MACRO_QR_CODE -> "Macro QR Code"
                ST_RSS14 -> "GS1 Databar"
                ST_RSS_LIMITET -> "GS1 Databar Limited"
                ST_RSS_EXPANDED -> "GS1 Databar Expanded"
                ST_SCANLET -> "Scanlet Webcode"
                ST_UPCA_2 -> "UPCA + 2"
                ST_UPCE0_2 -> "UPCE0 + 2"
                ST_EAN8_2 -> "EAN8 + 2"
                ST_EAN13_2 -> "EAN13 + 2"
                ST_UPCE1_2 -> "UPCE1 + 2"
                ST_CCA_EAN128 -> "CC-A + EAN-128"
                ST_CCA_EAN13 -> "CC-A + EAN-13"
                ST_CCA_EAN8 -> "CC-A + EAN-8"
                ST_CCA_RSS_EXPANDED -> "CC-A + GS1 Databar Expanded"
                ST_CCA_RSS_LIMITED -> "CC-A + GS1 Databar Limited"
                ST_CCA_RSS14 -> "CC-A + GS1 Databar"
                ST_CCA_UPCA -> "CC-A + UPCA"
                ST_CCA_UPCE -> "CC-A + UPC-E"
                ST_CCC_EAN128 -> "CC-C + EAN-128"
                ST_TLC39 -> "TLC-39"
                ST_CCB_EAN128 -> "CC-B + EAN-128"
                ST_CCB_EAN13 -> "CC-B + EAN-13"
                ST_CCB_EAN8 -> "CC-B + EAN-8"
                ST_CCB_RSS_EXPANDED -> "CC-B + GS1 Databar Expanded"
                ST_CCB_RSS_LIMITED -> "CC-B + GS1 Databar Limited"
                ST_CCB_RSS14 -> "CC-B + GS1 Databar"
                ST_CCB_UPCA -> "CC-B + UPC-A"
                ST_CCB_UPCE -> "CC-B + UPC-E"
                ST_SIGNATURE_CAPTURE -> "Signature"
                ST_MATRIX2OF5_OLD, ST_MATRIX_2_OF_5 -> "Matrix 2 Of 5"
                ST_CHINESE2OF5 -> "Chinese 2 Of 5"
                ST_UPCA_5 -> "UPCA 5"
                ST_UPCE0_5 -> "UPCE0 5"
                ST_EAN8_5 -> "EAN8 5"
                ST_EAN13_5 -> "EAN13 5"
                ST_UPCE1_5 -> "UPCE1 5"
                ST_MACRO_MICRO_PDF -> "Macro Micro PDF"
                ST_MICRO_QR_CODE -> "Micro QR Code"
                ST_AZTEC -> "Aztec Code"
                ST_AZTEC_RUNE_CODE -> "Aztec Rune Code"
                ST_FRENCH_LOTTERY -> "French Lottery"
                ST_PARAMETER_FNC3 -> "Parameter (FNC3)"
                ST_4STATE_US -> "4 State US"
                ST_4STATE_US4 -> "4 State US4"
                ST_CUE_CAT_CODE -> "Cue CAT Code"
                ST_KOREAN2OF5 -> "Korean 3 Of 5"
                ST_OCRB -> "OCRB"
                ST_RFID_RAW -> "RFID Raw"
                ST_RFID_URI -> "RFID URI"
                ST_ISSN -> "ISSN"
                ST_HAN_XIN -> "Han Xin"
                ST_NEW_COUPON -> "GS1 Databar Expanded Coupon"
                ST_GS1_DATAMATRIX -> "GS1 Datamatrix"
                else -> ""
            }
        }
    }
}