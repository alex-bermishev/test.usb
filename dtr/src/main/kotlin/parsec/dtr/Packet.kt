package parsec.dtr

import parsec.ftdi.crc8
import java.io.DataInputStream

class Packet(val command: Int,
             val data: ByteArray? = null) {
    companion object {
        const val HEADER_SIZE = 4
        const val CRC_SIZE = 1
        const val FOOTER_SIZE = 1

        const val CRC8_SEED = 0

        fun parse(value: ByteArray): Packet {
            require(value.size >= 8)
            require(value.first() == Uart.STX)
            require(value.last() == Uart.ETX)

            value.inputStream().use {
                val input = DataInputStream(it)
                input.skipBytes(HEADER_SIZE)
                val command = input.readUbyte().toInt()
                val size = input.readUbyte().toInt()
                val data = if (size != 0) input.readBytesExact(size) else null
                val crc = input.readUbyte()

                val expectedCrc = value.copyOfRange(0, value.size - CRC_SIZE - FOOTER_SIZE)
                        .crc8(CRC8_SEED)
                require(crc.toInt() == expectedCrc)

                return Packet(command, data)
            }
        }
    }

    fun toByteArray(): ByteArray {
        val result = mutableListOf(
                Uart.STX,
                Uart.TSID,
                Uart.SSID,
                Uart.POC,
                command.toByte(),
                data?.size?.toByte() ?: 0)

        if (data?.isNotEmpty() == true)
            result.addAll(data.asIterable())

        result.add(result.crc8(CRC8_SEED).toByte())
        result.add(Uart.ETX)

        return result.toByteArray()
    }
}