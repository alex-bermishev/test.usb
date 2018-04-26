package parsec.ftdi

import parsec.ftdi.Ftdi.FTDI_MAX_EEPROM_SIZE
import parsec.ftdi.Ftdi.MODEM_STATUS_HEADER_LENGTH
import java.io.Closeable
import java.util.*
import javax.usb.*
import javax.usb.event.UsbPipeDataEvent
import javax.usb.event.UsbPipeErrorEvent
import javax.usb.event.UsbPipeListener


class FtdiDevice(val usbDevice: UsbDevice)
    :Closeable {
    data class LineProperties(val bits: FtdiBitsType,
                              val stopBit: FtdiStopbitsType,
                              val parity: FtdiParityType,
                              val lineBreak: FtdiBreakType = FtdiBreakType.BREAK_OFF)

    private val configuration: UsbConfiguration = usbDevice.activeUsbConfiguration
    private val iface: UsbInterface
    private val pipeRead: UsbPipe
    private val readBufferSize: Int
    private val pipeWrite: UsbPipe

    private var _baudRate: Int = Ftdi.DEFAULT_BAUD_RATE
    private var _dtr: Boolean = false
    private var _rts: Boolean = false
    private var _flowControl: FtdiFlowControlMask = FtdiFlowControlMask.NONE
    private var _lineProperties: LineProperties = LineProperties(FtdiBitsType.BITS_8,
            FtdiStopbitsType.STOP_BIT_1,
            FtdiParityType.NONE)

    var readDataListener: ((UsbPipeDataEvent) -> Unit)? = null
    var readErrorListener: ((UsbPipeErrorEvent) -> Unit)? = null

    init {
        iface = configuration.getUsbInterface(0)
        iface.claim()

        pipeRead = iface.usbEndpoints
                .map { it as UsbEndpoint }
                .find { it.direction == UsbConst.ENDPOINT_DIRECTION_IN }!!
                .usbPipe

        readBufferSize = pipeRead.usbEndpoint.usbEndpointDescriptor.wMaxPacketSize().toInt()

        pipeWrite = iface.usbEndpoints
                .map { it as UsbEndpoint }
                .find { it.direction == UsbConst.ENDPOINT_DIRECTION_OUT }!!
                .usbPipe

        pipeRead.addUsbPipeListener(object : UsbPipeListener {
            override fun errorEventOccurred(event: UsbPipeErrorEvent?) {
                if (event != null)
                    readErrorListener?.invoke(event)
            }

            override fun dataEventOccurred(event: UsbPipeDataEvent?) {
                if (event != null)
                    readDataListener?.invoke(event)
            }
        })

        setBaudRate(_baudRate)
                .setLinePropeties(FtdiBitsType.BITS_8, FtdiStopbitsType.STOP_BIT_1, FtdiParityType.NONE)
                .setFlowControl(FtdiFlowControlMask.NONE)
                .setDtr(false)
                .setRts(true)
    }

    override fun close() {
        iface.release()
    }

    fun reset() =
            usbDevice.ftdiReset()

    var baudRate: Int
        get() = _baudRate
        set(value) {
            usbDevice.ftdiSetBaudRate(value)
            _baudRate = value
        }

    fun setBaudRate(value: Int) = apply { baudRate = value }

    var dtr: Boolean
        get() = _dtr
        set(value) {
            usbDevice.ftdiSetDTR(value)
            _dtr = value
        }

    fun setDtr(value: Boolean) = apply { dtr = value }

    var rts: Boolean
        get() = _rts
        set(value) {
            usbDevice.ftdiSetRTS(value)
            _rts = value
        }

    fun setRts(value: Boolean) = apply { rts = value }

    var flowControl: FtdiFlowControlMask
        get() = _flowControl
        set(value) {
            usbDevice.ftdiSetFlowControl(value)
            _flowControl = value
        }

    fun setFlowControl(value: FtdiFlowControlMask) = apply { flowControl = value }

    var lineProperties: LineProperties
        get() = _lineProperties
        set(value) {
            usbDevice.ftdiSetLineProperties(value.bits, value.stopBit, value.parity, value.lineBreak)
            _lineProperties = value
        }

    fun setLinePropeties(bits: FtdiBitsType,
                         stopBit: FtdiStopbitsType,
                         parity: FtdiParityType,
                         lineBreak: FtdiBreakType = FtdiBreakType.BREAK_OFF) = apply {
        lineProperties = LineProperties(bits, stopBit, parity, lineBreak)
    }

    fun purgeBuffers() = apply {
        usbDevice.ftdiPurgeTxBuffer()
        usbDevice.ftdiPurgeRxBuffer()
    }

    fun write(data: ByteArray): Int {
        if (!pipeWrite.isOpen)
            pipeWrite.open()

        return pipeWrite.syncSubmit(data)
    }

    fun writeAsync(data: ByteArray) {
        if (!pipeWrite.isOpen)
            pipeWrite.open()

        val usbIrp = pipeWrite.createUsbIrp()
        usbIrp.data = data
        pipeWrite.asyncSubmit(usbIrp)
    }

    fun read(): ByteArray {
        if (!pipeRead.isOpen)
            pipeRead.open()

        val usbFrame = ByteArray(readBufferSize)
        val bytesRead = pipeRead.syncSubmit(usbFrame)

        return if (bytesRead == MODEM_STATUS_HEADER_LENGTH)
            ByteArray(0)
        else
            Arrays.copyOfRange(usbFrame, MODEM_STATUS_HEADER_LENGTH, bytesRead)
    }

    fun readEEPROM(): ByteArray {
        val result = mutableListOf<Byte>()

        val buffer = ByteArray(2)
        for (index in 0 until FTDI_MAX_EEPROM_SIZE / 2) {
            val request = usbDevice.createUsbControlIrp(Ftdi.FTDI_DEVICE_IN_REQTYPE,
                    Ftdi.SIO_READ_EEPROM_REQUEST.toByte(),
                    0,
                    index.toShort())

            request.data = buffer

            usbDevice.syncSubmit(request)

            result.add(buffer[0])
            result.add(buffer[1])
        }

        return result.toByteArray()
    }

    fun readEEPROMLocation(offset: Int, size: Int): ByteArray {
        require(size > 0)

        val readOffset = ((if ((offset % 2) == 1) offset - 1 else offset) / 2)
        val readCount = (if ((size % 2) == 1) size + 1 else size) / 2

        val buffer = mutableListOf<Byte>()
        val readBuffer = ByteArray(2)
        for (index in 0 until readCount) {
            val request = usbDevice.createUsbControlIrp(Ftdi.FTDI_DEVICE_IN_REQTYPE,
                    Ftdi.SIO_READ_EEPROM_REQUEST.toByte(),
                    0,
                    (readOffset + index).toShort())

            request.data = readBuffer

            usbDevice.syncSubmit(request)

            buffer.add(readBuffer[0])
            buffer.add(readBuffer[1])
        }

        return buffer.toByteArray().copyOfRange(if ((offset % 2) == 1) 1 else 0, size)
    }

    val serial: ByteArray
        get() {
            val offsetAndSize = readEEPROMLocation(0x12, 2)
            val offset = offsetAndSize[0].toInt() and 0x7f
            val size = offsetAndSize[1] / 2

            val serialBuffer = readEEPROMLocation(offset + 2, size * 2)
            return serialBuffer.asIterable()
                    .withIndex()
                    .filter { it.index % 2 == 0 }
                    .map { it.value }
                    .toByteArray()
        }
}