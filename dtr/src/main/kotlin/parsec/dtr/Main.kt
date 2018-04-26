package parsec.dtr

import unsigned.*
import java.nio.ByteBuffer
import javax.usb.UsbDevice
import javax.usb.UsbHub
import javax.usb.UsbEndpoint
import javax.usb.UsbInterface
import javax.usb.UsbConfiguration
import kotlin.experimental.and


fun <T> nothrow(block: () -> T?) =
        try {
            block()
        } catch (ex: Exception) {
            null
        }

var counter = 1

fun Byte.toHexChar(): Char {
    return if (this < 0x0a)
        (this + '0'.toByte()).toChar()
    else
        ((this - 0x0a) + 'A'.toByte()).toChar()
}

fun Ubyte.toHexChar(): Char {
    return if (this < 0x0a)
        (this + '0'.toByte()).toChar()
    else
        ((this - 0x0a) + 'A'.toByte()).toChar()
}


fun Int.toByteArray() =
        ByteBuffer.allocate(4).putInt(this).array()

fun main(args: Array<String>) {

    /*
    val a = ftdiDevice.usbDevice.getUsbStringDescriptor(ftdiDevice.usbDevice.usbDeviceDescriptor.iManufacturer()).string
    val b = ftdiDevice.usbDevice.getUsbStringDescriptor(ftdiDevice.usbDevice.usbDeviceDescriptor.iProduct()).string
    val c = ftdiDevice.usbDevice.getUsbStringDescriptor(ftdiDevice.usbDevice.usbDeviceDescriptor.iSerialNumber())
            .bString()
            */

    //val dtrDevice = DtrDevice.openBySerial("001164") ?: return
    val dtrDevice = DtrDevice.openBySerial("001164") ?: return

    val pid = dtrDevice.pid
    val serial = dtrDevice.serial



    //dtrDevice.beep()

    dtrDevice.readCode()

    while (true) {
        val code = dtrDevice.readCode()
        if (code?.isNotEmpty() == true) {
            val cardCode = (code[2].toUint() shl 24) or
                    (code[3].toUint() shl 16) or
                    (code[4].toUint() shl 8) or
                    code[5].toUint()



            val test = cardCode.toInt().toByteArray()

            println(test.toHexString())
            dtrDevice.beep()
        }

        Thread.sleep(100)
    }

    /*
    buffer = byteArrayOf(Constants.CMD_GET_INFO.toByte())
    received = ftdiDevice.tranceive(buffer)
    println(received.toHexString())
    */


    /*
    while (true) {


        buffer = byteArrayOf(Constants.CMD_GETCODECARD.toByte())
        received = ftdiDevice.tranceive(buffer)
        if (received.isNotEmpty())
            println(received.toHexString())


        Thread.sleep(100)
    }*/


    /*
    val services = UsbHostManager.getUsbServices()

    println("USB Service Implementation: ${services.impDescription}")
    println("Implementation version:  ${services.impVersion}")
    println("Service API version: ${services.apiVersion}")
    println("${counter++}------------------------------------------------------------")

    // Dump the root USB hub

    dump(services.rootUsbHub)

    services.addUsbServicesListener(object : UsbServicesListener {
        override fun usbDeviceDetached(p0: UsbServicesEvent?) {
            println(p0)
        }

        override fun usbDeviceAttached(p0: UsbServicesEvent?) {
            if (p0 != null)
                dumpDeviceInfo(p0.usbDevice)
        }

    })

    val reader = services.rootUsbHub.findDevice(vendorId, productId)

    readLine()

    if (reader != null) {
        val config = reader.activeUsbConfiguration
        val iface = config.getUsbInterface(0)
        iface.claim()

        val endpoint1 = iface.getUsbEndpoint(0x81.toByte())
        val pipe1 = endpoint1.usbPipe
        pipe1.open()

        pipe1.addUsbPipeListener(object : UsbPipeListener {
            override fun errorEventOccurred(p0: UsbPipeErrorEvent?) {
                println("error")
            }

            override fun dataEventOccurred(p0: UsbPipeDataEvent?) {
                println("p = ${p0?.data?.toHexString()}")
            }

        })


        val endpoint = iface.getUsbEndpoint(0x02.toByte())
        val pipe = endpoint.usbPipe
        pipe.open()

        pipe.addUsbPipeListener(object : UsbPipeListener {
            override fun errorEventOccurred(p0: UsbPipeErrorEvent?) {
                println("error")
            }

            override fun dataEventOccurred(p0: UsbPipeDataEvent?) {
                println(p0?.data?.toHexString())
            }

        })

        pipe.close()
        iface.release()

    }
    */
}

fun ByteArray.toHexString(separator: CharSequence = "",
                          prefix: CharSequence = "",
                          postfix: CharSequence = "",
                          limit: Int = -1,
                          truncated: CharSequence = "..."
) =
        joinToString(separator, prefix, postfix, limit, truncated) {
            String.format("%02x", it and 0xff.toByte())
        }


fun UsbDevice.findDevice(vendorId: Int, productId: Int): UsbDevice? {
    if (isUsbHub) {
        for (device in (this as UsbHub).attachedUsbDevices.map { it as UsbDevice }) {
            val found = device.findDevice(vendorId, productId)
            if (found != null)
                return found
        }
    } else if (usbDeviceDescriptor.idVendor() == vendorId.toShort()
            && usbDeviceDescriptor.idProduct() == productId.toShort()) {
        return this
    }

    return null
}

val vendorId = 0x0403 //ftdi
val productId = 0xe3b4 //


fun dumpDeviceInfo(device: UsbDevice) {
    println(device)
    val port = device.parentUsbPort
    if (port != null) {
        println("Connected to port: " + port.portNumber)
        println("Parent: " + port.usbHub)
    }

    // Dump usbDevice descriptor
    println(device.usbDeviceDescriptor)

    // Process all configurations
    for (configuration in device
            .usbConfigurations as List<UsbConfiguration>) {
        // Dump configuration descriptor
        println(configuration.usbConfigurationDescriptor)

        // Process all interfaces
        for (iface in configuration
                .usbInterfaces as List<UsbInterface>) {
            // Dump the interface descriptor
            println(iface.usbInterfaceDescriptor)

            // Process all endpoints
            for (endpoint in iface
                    .usbEndpoints as List<UsbEndpoint>) {
                // Dump the endpoint descriptor
                println(endpoint.usbEndpointDescriptor)
            }
        }
    }
}

fun dump(device: UsbDevice) {

    dumpDeviceInfo(device)

    println("${counter++}------------------------------------------------------------")

    // Dump child devices if usbDevice is a hub
    if (device.isUsbHub) {
        val hub = device as UsbHub
        for (child in hub.attachedUsbDevices as List<UsbDevice>) {
            dump(child)
        }
    }
}