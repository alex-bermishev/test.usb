package parsec.ftdi

enum class FtdiStopbitsType(val value: Int) {
    STOP_BIT_1(0),
    STOP_BIT_15(1),
    STOP_BIT_2(2),
}