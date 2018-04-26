package parsec.dtr

enum class Pid(val value: Int) {
    PID_UNKNOWN(0),
    PID_APERIO(0xE377),
    PID_RFIDEAS(0xE378),
    PID_PR_T08(0xE379),
    PID_COTAG(0xE37A),
    PID_PR_P08_OLD(0xE37C),
    PID_NI_A01_OLD(0xE37D),
    PID_PR_A08(0xE37E),
    PID_PR_H08(0xE37F),
    PID_PR_P08(0xE3B0),
    PID_NIP_A01(0xE3B1),
    PID_NI_A01(0xE3B2),
    PID_PR_C08(0xE3B3),
    PID_PR_EH08(0xE3B4),
}