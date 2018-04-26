package parsec.dtr

enum class CardType(val value: Int) {
    CARD_UNKNOWN(0),
    CARD_ISO14443A(1),
    CARD_EM(2),
    CARD_HID(3),
    CARD_DALLS(4),
    CARD_CHEKPOINT(5),
    CARD_TEXT(6),
    CARD_COTAG(7),
    CARD_BOSCH(8),
    CARD_ISO14443B(9),
}