package parsec.ftdi

enum class FtdiModuleDetachMode(val value: Int) {
    AUTO_DETACH_SIO_MODULE(0),
    DONT_DETACH_SIO_MODULE(1),
}