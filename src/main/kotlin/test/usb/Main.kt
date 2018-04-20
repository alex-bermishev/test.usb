package test.usb

import org.usb4java.*
import javax.usb.UsbDevice
import javax.usb.UsbHub
import javax.usb.UsbHostManager
import javax.usb.UsbServices
import javax.usb.event.*
import sun.audio.AudioDevice.device
import javax.usb.UsbEndpoint
import javax.usb.UsbInterface
import javax.usb.UsbConfiguration
import org.usb4java.LibUsb.getPortNumber
import javax.usb.UsbPort


fun <T> nothrow(block: () -> T?) =
        try {
            block()
        } catch (ex: Exception) {
            null
        }

/*
fun main(args: Array<String>) {
    // Create the libusb context
    val context = Context()

    // Initialize the libusb context
    var result = LibUsb.init(context)
    if (result < 0) {
        throw LibUsbException("Unable to initialize libusb", result)
    }

    // Read the USB device list
    val list = DeviceList()
    result = LibUsb.getDeviceList(context, list)
    if (result < 0) {
        throw LibUsbException("Unable to get device list", result)
    }

    try {
        // Iterate over all devices and list them
        for (device in list) {
            val address = LibUsb.getDeviceAddress(device)
            val busNumber = LibUsb.getBusNumber(device)
            val descriptor = DeviceDescriptor()
            result = LibUsb.getDeviceDescriptor(device, descriptor)
            if (result < 0) {
                throw LibUsbException(
                        "Unable to read device descriptor", result)
            }
            System.out.format(
                    "Bus %03d, Device %03d: Vendor %04x, Product %04x%n",
                    busNumber, address, descriptor.idVendor(),
                    descriptor.idProduct())
        }
    } finally {
        // Ensure the allocated device list is freed
        LibUsb.freeDeviceList(list, true)
    }

    // Deinitialize the libusb context
    LibUsb.exit(context)
}*/


var counter = 1

fun main(args: Array<String>) {
    val services = UsbHostManager.getUsbServices();
    println("USB Service Implementation: ${services.impDescription}")
    println("Implementation version:  ${services.impVersion}")
    println("Service API version: ${services.apiVersion}")
    println("${counter++}------------------------------------------------------------")

    // Dump the root USB hub

    dump(services.rootUsbHub, 0)

    services.addUsbServicesListener(object : UsbServicesListener {
        override fun usbDeviceDetached(p0: UsbServicesEvent?) {
            println(p0)
        }

        override fun usbDeviceAttached(p0: UsbServicesEvent?) {
            println(p0)
        }

    })

    readLine()

    val hub = services.rootUsbHub.findDevice(-32633, 0)
    val device = hub?.findDevice(5426, 2)
    if (device != null) {
        val config = device.activeUsbConfiguration
        val iface = config.getUsbInterface(1)
        iface.claim()
        val endpoint = iface.getUsbEndpoint(1)
        val pipe = endpoint.usbPipe
        pipe.open()

        pipe.addUsbPipeListener(object : UsbPipeListener {
            override fun errorEventOccurred(p0: UsbPipeErrorEvent?) {
                println("error")
            }

            override fun dataEventOccurred(p0: UsbPipeDataEvent?) {
                println(p0)
            }

        })

        pipe.close()

    }
}

fun UsbDevice.findDevice(vendorID: Int, productID: Int) =
        if (isUsbHub) {
            (this as UsbHub).attachedUsbDevices
                    .map { it as UsbDevice }
                    .find {
                        it.usbDeviceDescriptor.idVendor() == vendorID.toShort()
                                && it.usbDeviceDescriptor.iProduct() == productID.toByte()
                    }
        } else
            null

fun dump(device: UsbDevice, level: Int) {
    var i = 0
    while (i < level) {
        print("  ")
        i += 1
    }

    println(device)
    val port = device.parentUsbPort
    if (port != null) {
        println("Connected to port: " + port.portNumber)
        println("Parent: " + port.usbHub)
    }

    // Dump device descriptor
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

    println("${counter++}------------------------------------------------------------")

    // Dump child devices if device is a hub
    if (device.isUsbHub) {
        val hub = device as UsbHub
        for (child in hub.attachedUsbDevices as List<UsbDevice>) {
            dump(child, level + 1)
        }
    }
}