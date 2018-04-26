package parsec.ftdi

enum class FtdiInterface(val value: Int) {
    INTERFACE_ANY(0),
    INTERFACE_A(1),
    INTERFACE_B(2),
    INTERFACE_C(3),
    INTERFACE_D(4),
}