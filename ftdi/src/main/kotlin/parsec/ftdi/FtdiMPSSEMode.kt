package parsec.ftdi

/** MPSSE bitbang modes */
enum class FtdiMPSSEMode(val value: Int) {
    /**< switch off bitbang mode), back to regular serial/FIFO */
    BITMODE_RESET(0x00),

    /**< classical asynchronous bitbang mode), introduced with B-type chips */
    BITMODE_BITBANG(0x01),

    /**< MPSSE mode), available on 2232x chips */
    BITMODE_MPSSE(0x02),

    /**< synchronous bitbang mode), available on 2232x and R-type chips  */
    BITMODE_SYNCBB(0x04),

    /**< MCU Host Bus Emulation mode), available on 2232x chips */
    BITMODE_MCU(0x08),


    /**< Fast Opto-Isolated Serial Interface Mode), available on 2232x chips  */
    BITMODE_OPTO(0x10),

    /**< Bitbang on CBUS pins of R-type chips), configure in EEPROM before */
    BITMODE_CBUS(0x20),

    /**< Single Channel Synchronous FIFO mode), available on 2232H chips */
    BITMODE_SYNCFF(0x40),

    /**< FT1284 mode, available on 232H chips */
    BITMODE_FT1284(0x80),
}