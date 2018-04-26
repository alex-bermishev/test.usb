package parsec.dtr

import unsigned.toUbyte
import java.io.DataInput

fun DataInput.readBytesExact(size: Int): ByteArray {
    val result = ByteArray(size)
    readFully(result)
    return result
}

fun DataInput.readUbyte() =
        readByte().toUbyte()