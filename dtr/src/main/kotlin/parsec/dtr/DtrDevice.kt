package parsec.dtr

import parsec.ftdi.*
import unsigned.toUbyte
import unsigned.toUint
import unsigned.ub
import java.io.Closeable
import javax.usb.UsbDevice
import javax.usb.UsbHostManager

class DtrDevice(val ftdiDevice: FtdiDevice)
    : Closeable {
    companion object {
        const val SERIAL_OFFSET = 5
        const val SERIAL_SIZE = 3

        const val CMD_INIT = 0x3B
        const val CMD_ACTIVATEA = 0x49
        const val CMD_HALTA = 0x68
        const val CMD_BEEP_CONFIG = 0x30
        const val CMD_DO_BEEP = 0x31
        const val CMD_GETCODECARD = 0x32
        const val CMD_GET_INFO = 0x3F

        const val CARD_TYPE_NOCHANGE = 0x00
        const val CARD_TYPE_MIFARE = 0x05
        const val RF_N0_CHANGE = 0x00
        const val RF_OFF = 0x01
        const val RF_LOW = 0x02
        const val RF_HIGH = 0x03
    }

    constructor(usbDevice: UsbDevice)
            : this(FtdiDevice(usbDevice))

    init {
        ftdiDevice.setBaudRate(9600)
                .setLinePropeties(FtdiBitsType.BITS_8, FtdiStopbitsType.STOP_BIT_1, FtdiParityType.NONE)
                .setFlowControl(FtdiFlowControlMask.NONE)
                .purgeBuffers()
    }

    override fun close() {
        ftdiDevice.close()
    }

    fun ByteArray.toDecodedString(): String {
        val res = StringBuilder()
        for (byte in asIterable().map { it.toUbyte() }) {
            res.append((((byte and 0xf0.ub) shr 4) - 1).toByte().toString(16))
            res.append(((byte and 0x0f.ub) - 1).toByte().toString(16))
        }
        return res.toString()
    }

    val pid: Pid
        get() {
            val ftdiSerial = ftdiDevice.serial
            val pidValue = ((ftdiSerial[0].toUint() shl 8) or ftdiSerial[1].toUint()).toInt()

            return Pid.values().find { it.value == pidValue } ?: Pid.PID_UNKNOWN
        }

    val serial: String
        get() = ftdiDevice.serial
                .copyOfRange(SERIAL_OFFSET, SERIAL_OFFSET + SERIAL_SIZE)
                .toDecodedString()

    fun beep() {
        val read = ftdiDevice.tranceive(CMD_DO_BEEP)
    }

    fun readCode() =
            ftdiDevice.tranceive(CMD_GETCODECARD)
                    .data

    fun FtdiDevice.tranceive(command: Int, vararg data: Byte): Packet {
        val bytesWritten = write(Packet(command, if (data.isNotEmpty()) data else null).toByteArray())
        return Packet.parse(readPacket())
    }

    fun FtdiDevice.readPacket(): ByteArray {
        val result = mutableListOf<Byte>()
        do {
            for (byte in read()) {
                if (result.isEmpty()) {
                    if (byte == Uart.STX)
                        result.add(byte)
                } else {
                    result.add(byte)

                    if (byte == Uart.ETX)
                        break
                    else if (byte == Uart.STX)
                        return ByteArray(0)
                }
            }

        } while (result.isEmpty() || (result.last() != Uart.ETX))

        return result.toByteArray()
    }
}

fun DtrDevice.Companion.openBySerial(value: String): DtrDevice? {
    for (usbDevice in UsbHostManager.getUsbServices().rootUsbHub.ftdiDevices()) {
        try {
            val dtrDevice = DtrDevice(usbDevice)
            if (dtrDevice.serial == value)
                return dtrDevice
            dtrDevice.close()
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    return null
}