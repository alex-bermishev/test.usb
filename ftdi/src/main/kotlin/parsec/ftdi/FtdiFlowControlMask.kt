package parsec.ftdi

enum class FtdiFlowControlMask(val value: Short) {
    NONE(0),
    RTS_CTS_HS(1 shl 8),
    DTR_DSR_HS(2 shl 8),
    XON_XOFF_HS(4 shl 8)
}