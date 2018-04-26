package parsec.dtr

object Uart {
    const val STX: Byte = 0x02        // Начало пакета
    const val TSID: Byte = 0x00       // Target station ID
    const val SSID: Byte = 0x00       // Source station ID
    const val POC: Byte = 0x00        // Protocol option code
    const val ETX: Byte = 0x03        // Конец пакета
}