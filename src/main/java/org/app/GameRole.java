package org.app;

public enum GameRole {
    EXCLUDED((byte) 0, "Ausgeschlossen"),
    SPECTATOR((byte) 1, "Zuschauer"),
    PLAYER((byte) 2, "Spieler"),
    ADMIN((byte) 3, "Admin"),
    REJECTED((byte) 4, "Abgelehnt");

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
        return switch (value) {
            case 0 -> EXCLUDED;
            case 1 -> SPECTATOR;
            case 2 -> PLAYER;
            case 3 -> ADMIN;
            case 4 -> REJECTED;
            default -> null;
        };
    }
}