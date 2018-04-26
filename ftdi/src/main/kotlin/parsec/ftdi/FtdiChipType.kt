package parsec.ftdi

enum class FtdiChipType(val value: Int) {
    TYPE_AM(0),
    TYPE_BM(1),
    TYPE_2232C(2),
    TYPE_R(3),
    TYPE_2232H(4),
    TYPE_4232H(5),
    TYPE_232H(6),
    TYPE_230X(7),
}