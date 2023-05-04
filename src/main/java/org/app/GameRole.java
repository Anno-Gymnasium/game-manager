package org.app;

import javafx.util.StringConverter;
import java.lang.Enum;

public enum GameRole implements Comparable<GameRole> {
    EXCLUDED((byte) 0, "Ausgeschlossen"),
    SPECTATOR((byte) 1, "Zuschauer"),
    PLAYER((byte) 2, "Spieler"),
    ADMIN((byte) 3, "Admin"),
    REJECTED((byte) 4, "Abgelehnt");

    public final static StringConverter<GameRole> STRING_CONVERTER = new StringConverter<>() {
        @Override public String toString(GameRole gameRole) {
            return gameRole == null ? null : gameRole.getName();
        }
        @Override public GameRole fromString(String s) {
            return null;
        }
    };
    private final byte value;
    private final String name;
    GameRole(byte value, String name) {
        this.value = value;
        this.name = name;
    }

    public byte getValue() {
        return value;
    }
    public String getName() {
        return name;
    }

    public static GameRole getRole(int value) {
        return values()[value];
    }
}