package parsec.ftdi

import unsigned.us
import javax.usb.UsbDevice

fun UsbDevice.ftdiReset() =
        syncSubmit(createUsbControlIrp(Ftdi.FTDI_DEVICE_OUT_REQTYPE,
                Ftdi.SIO_RESET_REQUEST.toByte(),
                0.toShort(),
                0.toShort()))

fun UsbDevice.ftdiSetBaudRate(requestedBaudRate: Int) =
        syncSubmit(createUsbControlIrp(Ftdi.FTDI_DEVICE_OUT_REQTYPE,
                Ftdi.SIO_SET_BAUDRATE_REQUEST.toByte(),
                Ftdi.calculateBaudRate(requestedBaudRate),
                Interface.A.value.toShort()))

fun UsbDevice.ftdiSetDTR(state: Boolean) =
        syncSubmit(createUsbControlIrp(Ftdi.FTDI_DEVICE_OUT_REQTYPE,
                Ftdi.SIO_SET_MODEM_CTRL_REQUEST.toByte(),
                (if (state) Ftdi.SIO_SET_DTR_HIGH else Ftdi.SIO_SET_DTR_LOW).toShort(),
                Interface.A.value.toShort()))

fun UsbDevice.ftdiSetRTS(state: Boolean) =
        syncSubmit(createUsbControlIrp(Ftdi.FTDI_DEVICE_OUT_REQTYPE,
                Ftdi.SIO_SET_MODEM_CTRL_REQUEST.toByte(),
                (if (state) Ftdi.SIO_SET_RTS_HIGH else Ftdi.SIO_SET_RTS_LOW).toShort(),
                Interface.A.value.toShort()))

fun UsbDevice.ftdiSetDTRRTS(dtrState: Boolean, rtsState: Boolean) {
    val dtrValue = if (dtrState) Ftdi.SIO_SET_DTR_HIGH else Ftdi.SIO_SET_DTR_LOW
    val rtsValue = if (rtsState) Ftdi.SIO_SET_RTS_HIGH else Ftdi.SIO_SET_RTS_LOW
    syncSubmit(createUsbControlIrp(Ftdi.FTDI_DEVICE_OUT_REQTYPE,
            Ftdi.SIO_SET_MODEM_CTRL_REQUEST.toByte(),
            (dtrValue or rtsValue).toShort(),
            Interface.A.value.toShort()))
}

fun UsbDevice.ftdiSetFlowControl(flowControl: FtdiFlowControlMask) =
        syncSubmit(createUsbControlIrp(Ftdi.FTDI_DEVICE_OUT_REQTYPE,
                Ftdi.SIO_SET_FLOW_CTRL_REQUEST.toByte(),
                flowControl.value,
                Interface.A.value.toShort()))

fun UsbDevice.ftdiSetLineProperties(bits: FtdiBitsType,
                                    stopBit: FtdiStopbitsType,
                                    parity: FtdiParityType,
                                    lineBreak: FtdiBreakType = FtdiBreakType.BREAK_OFF) {
    var value = bits.value.us
    when (parity) {
        FtdiFlowControlMask.NONE -> {
        }
        FtdiParityType.ODD -> value = (value or (0x01 shl 8))
        FtdiParityType.EVEN -> value = (value or (0x02 shl 8))
        FtdiParityType.MARK -> value = (value or (0x03 shl 8))
        FtdiParityType.SPACE -> value = (value or (0x04 shl 8))
        else -> throw IllegalArgumentException(parity.name)
    }
    when (stopBit) {
        FtdiStopbitsType.STOP_BIT_1 -> {
        }
        FtdiStopbitsType.STOP_BIT_15 -> value = (value or (0x01 shl 11))
        FtdiStopbitsType.STOP_BIT_2 -> value = (value or (0x01 shl 11))
        else -> throw IllegalArgumentException(stopBit.name)
    }
    when (lineBreak) {
        FtdiBreakType.BREAK_OFF -> {
        }
        FtdiBreakType.BREAK_ON -> value = (value or (0x01 shl 14))
        else -> throw IllegalArgumentException(lineBreak.name)
    }

    syncSubmit(createUsbControlIrp(Ftdi.FTDI_DEVICE_OUT_REQTYPE,
            Ftdi.SIO_SET_DATA_REQUEST.toByte(),
            value.toShort(),
            Interface.A.value.toShort()))
}

fun UsbDevice.ftdiPurgeTxBuffer() {
    syncSubmit(createUsbControlIrp(Ftdi.FTDI_DEVICE_OUT_REQTYPE,
            Ftdi.SIO_RESET_REQUEST.toByte(),
            Ftdi.SIO_RESET_PURGE_RX.toShort(),
            0))
}

fun UsbDevice.ftdiPurgeRxBuffer() {
    syncSubmit(createUsbControlIrp(Ftdi.FTDI_DEVICE_OUT_REQTYPE,
            Ftdi.SIO_RESET_REQUEST.toByte(),
            Ftdi.SIO_RESET_PURGE_RX.toShort(),
            0))
}