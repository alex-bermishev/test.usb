package parsec.ftdi

enum class FtdiParityType(val value: Int) {
    NONE(0),
    ODD(1),
    EVEN(2),
    MARK(3),
    SPACE(4),
}