package parsec.ftdi

infix fun Byte.or(value: Int) =
        (toInt() or value).toByte()