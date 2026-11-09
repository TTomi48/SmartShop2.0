package hu.bme.backend.entities.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum Unit {
    PIECE("db"),
    KILOGRAM("kg"),
    DEKAGRAM("dkg"),
    GRAM("g"),
    LITER("l"),
    DECILITER("dl"),
    MILLILITER("ml"),
    PACKAGE("csomag"),
    BOTTLE("üveg"),
    BOX("doboz"),
    TRAY("tálca");

    private final String value;
    Unit(String value) {
        this.value = value;
    }
}
