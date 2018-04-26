package parsec.ftdi

import javax.usb.UsbDevice
import javax.usb.UsbHostManager
import javax.usb.UsbHub

fun UsbHub.allDevices(): List<UsbDevice> {
    val result = mutableListOf<UsbDevice>()
    for (device in attachedUsbDevices.map { it as UsbDevice }) {
        if (device.isUsbHub)
            result.addAll((device as UsbHub).allDevices())
        else
            result.add(device)
    }

    return result
}

fun UsbHub.filterDevices(block: (UsbDevice) -> Boolean) =
        allDevices().filter(block)

fun UsbHub.filter(vendorId: Short) =
        filterDevices { it.usbDeviceDescriptor.idVendor() == vendorId }

fun UsbHub.filter(vendorId: Short,
                  productId: Short) =
        filterDevices {
            it.usbDeviceDescriptor.idVendor() == vendorId
                    && it.usbDeviceDescriptor.idProduct() == productId
        }

fun UsbHub.ftdiDevices() =
        filter(Ftdi.VENDOR_ID.toShort())