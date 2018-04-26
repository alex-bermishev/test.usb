package parsec.ftdi

import javax.usb.UsbConst
import kotlin.experimental.or

object Ftdi {
    const val VENDOR_ID = 0x0403

    /* Write TDI/DO on negative TCK/SK edge*/
    val MPSSE_WRITE_NEG = 0x01

    /* Write bits, not bytes */
    val MPSSE_BITMODE = 0x02

    /* Sample TDO/DI on negative TCK/SK edge */
    val MPSSE_READ_NEG = 0x04

    /* LSB first */
    val MPSSE_LSB = 0x08

    /* Write TDI/DO */
    val MPSSE_DO_WRITE = 0x10

    /* Read TDO/DI */
    val MPSSE_DO_READ = 0x20

    /* Write TMS/CS */
    val MPSSE_WRITE_TMS = 0x40

    val SET_BITS_LOW = 0x80
    val SET_BITS_HIGH = 0x82
    val GET_BITS_LOW = 0x81
    val GET_BITS_HIGH = 0x83
    val LOOPBACK_START = 0x84
    val LOOPBACK_END = 0x85
    val TCK_DIVISOR = 0x86
    val DIS_DIV_5 = 0x8a
    val EN_DIV_5 = 0x8b
    val EN_3_PHASE = 0x8c
    val DIS_3_PHASE = 0x8d
    val CLK_BITS = 0x8e
    val CLK_BYTES = 0x8f
    val CLK_WAIT_HIGH = 0x94
    val CLK_WAIT_LOW = 0x95
    val EN_ADAPTIVE = 0x96
    val DIS_ADAPTIVE = 0x97
    val CLK_BYTES_OR_HIGH = 0x9c
    val CLK_BYTES_OR_LOW = 0x0d

    val DRIVE_OPEN_COLLECTOR = 0x9e

    val SEND_IMMEDIATE = 0x87
    val WAIT_ON_HIGH = 0x88
    val WAIT_ON_LOW = 0x89

    val READ_SHORT = 0x90
    val READ_EXTENDED = 0x91
    val WRITE_SHORT = 0x92
    val WRITE_EXTENDED = 0x93

    /* Reset the port */
    val SIO_RESET = 0

    /* Set the modem control register */
    val SIO_MODEM_CTRL = 1

    /* Set flow control register */
    val SIO_SET_FLOW_CTRL = 2

    /* Set baud rate */
    val SIO_SET_BAUD_RATE = 3

    /* Set the data characteristics of the port */
    val SIO_SET_DATA = 4

    val FTDI_DEVICE_OUT_REQTYPE = UsbConst.REQUESTTYPE_TYPE_VENDOR or
            UsbConst.REQUESTTYPE_RECIPIENT_DEVICE or
            UsbConst.ENDPOINT_DIRECTION_OUT

    val FTDI_DEVICE_IN_REQTYPE = UsbConst.REQUESTTYPE_TYPE_VENDOR or
            UsbConst.REQUESTTYPE_RECIPIENT_DEVICE or
            UsbConst.ENDPOINT_DIRECTION_IN

    /* Requests */
    val SIO_RESET_REQUEST = SIO_RESET
    val SIO_SET_BAUDRATE_REQUEST = SIO_SET_BAUD_RATE
    val SIO_SET_DATA_REQUEST = SIO_SET_DATA
    val SIO_SET_FLOW_CTRL_REQUEST = SIO_SET_FLOW_CTRL
    val SIO_SET_MODEM_CTRL_REQUEST = SIO_MODEM_CTRL
    val SIO_POLL_MODEM_STATUS_REQUEST = 0x05
    val SIO_SET_EVENT_CHAR_REQUEST = 0x06
    val SIO_SET_ERROR_CHAR_REQUEST = 0x07
    val SIO_SET_LATENCY_TIMER_REQUEST = 0x09
    val SIO_GET_LATENCY_TIMER_REQUEST = 0x0A
    val SIO_SET_BITMODE_REQUEST = 0x0B
    val SIO_READ_PINS_REQUEST = 0x0C
    val SIO_READ_EEPROM_REQUEST = 0x90
    val SIO_WRITE_EEPROM_REQUEST = 0x91
    val SIO_ERASE_EEPROM_REQUEST = 0x92


    val SIO_RESET_SIO = 0
    val SIO_RESET_PURGE_RX = 1
    val SIO_RESET_PURGE_TX = 2

    val SIO_DISABLE_FLOW_CTRL = 0x0
    val SIO_RTS_CTS_HS = (0x1 shl 8)
    val SIO_DTR_DSR_HS = (0x2 shl 8)
    val SIO_XON_XOFF_HS = (0x4 shl 8)

    val SIO_SET_DTR_MASK = 0x1
    val SIO_SET_DTR_HIGH = (1 or (SIO_SET_DTR_MASK shl 8))
    val SIO_SET_DTR_LOW = (0 or (SIO_SET_DTR_MASK shl 8))
    val SIO_SET_RTS_MASK = 0x2
    val SIO_SET_RTS_HIGH = (2 or (SIO_SET_RTS_MASK shl 8))
    val SIO_SET_RTS_LOW = (0 or (SIO_SET_RTS_MASK shl 8))

    const val DEFAULT_BAUD_RATE = 115200
    const val MODEM_STATUS_HEADER_LENGTH = 2
    const val FTDI_MAX_EEPROM_SIZE = 256


    /**
     * FTDI Baud Rate Calculation.
     *
     *
     * A Baud rate for the FT232R, FT2232 (UART mode) or FT232B is generated using
     * the chips internal 48MHz clock. This is input to Baud rate generator
     * circuitry where it is then divided by 16 and fed into a prescaler as a 3MHz
     * reference clock. This 3MHz reference clock is then divided down to provide
     * the required Baud rate for the usbDevice's on chip UART. The value of the Baud
     * rate divisor is an integer plus a sub-integer prescaler. The original
     * FT8U232AM only allowed 3 sub- integer prescalers - 0.125, 0.25 or 0.5. The
     * FT232R, FT2232 (UART mode) and FT232B support a further 4 additional
     * sub-integer prescalers - 0.375, 0.625, 0.75, and 0.875. Thus, allowed
     * values for the Baud rate divisor are:
     *
     *
     * Divisor = n + 0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875; where n is an
     * integer between 2 and 16384 (214).
     *
     *
     * Note: Divisor = 1 and Divisor = 0 are special cases. A divisor of 0 will
     * give 3 MBaud, and a divisor of 1 will give 2 MBaud. Sub-integer divisors
     * between 0 and 2 are not allowed. Therefore the value of the divisor needed
     * for a given Baud rate is found by dividing 3000000 by the required Baud
     * rate.
     *
     *
     * The exact Baud rate may not be achievable - however as long as the actual
     * Baud rate used is within +/-3% of the required Baud rate then the link
     * should function without errors. When a Baud rate is passed to the driver
     * where the exact divisor required is not achievable the closest possible
     * Baud rate divisor will be used as long as that divisor gives a Baud rate
     * which is within +/- 3% of the Baud rate originally set.
     *
     *
     * For example: A non-standard Baud rate of 490000 Baud is required.
     *
     *
     * Required divisor = 3000000 / 490000 = 6.122
     *
     *
     * The closest achievable divisor is 6.125, which gives a baud rate of
     * 489795.9, which is well within the allowed +/- 3% margin of error.
     * Therefore 490000 can be passed to the driver and the usbDevice will
     * communicate without errors.
     *
     * @see See AN232B-05_BaudRates.pdf, page 7.
     *
     *
     * @param requestedBaudRate the (possibly non-standard) requested baud rate
     * (bits per second)
     * @return the nearest supported baud rate (bits per second)
     */
    fun calculateBaudRate(requestedBaudRate: Int): Short {
        /**
         * Developer note: The maximum Baud rate achievable with FTDI's current
         * devices is 3M Baud. the Baud rate divisor must be calculated using the
         * following formula:
         *
         *
         * `Integer Divisor + Sub-Integer Divisor = 3000000/Baud Rate`
         *
         *
         * where the Integer Divisor is any integer between 2 and 16384 and the
         * Sub-Integer Divisor can be any one of 0, 0.125, 0.25, 0.375, 0.5, 0.625,
         * 0.75 or 0.875. Note that the FT8U232AM usbDevice will only support
         * Sub-Integer Divisors of 0, 0.125, 0.25 and 0.5.
         *
         *
         * This 3MHz reference clock is then divided down to provide the required
         * Baud rate for the usbDevice's on chip UART. The value of the Baud rate
         * divisor is an integer plus a sub-integer pre-scaler.
         *
         *
         * Allowed values for the Baud rate divisor are: Divisor = n + 0, 0.125,
         * 0.25, 0.375, 0.5, 0.625, 0.75, 0.875; where n is an integer between 2 and
         * 16384 (2^14). n > 2.
         *
         *
         * The value of the divisor needed for a given Baud rate is found by
         * dividing 3000000 (= 3 MHz) by the required Baud rate.
         */
        val divisor = 3000000 / requestedBaudRate
        /**
         * The exact Baud rate may not be achievable - however as long as the actual
         * Baud rate used is within +/-3% of the required Baud rate then the link
         * should function without errors. When a Baud rate is passed to the driver
         * where the exact divisor required is not achievable the closest possible
         * Baud rate divisor will be used as long as that divisor gives a Baud rate
         * which is within +/- 3% of the Baud rate originally set.
         */
        return (divisor shl 16 shr 16).toShort()
    }


}