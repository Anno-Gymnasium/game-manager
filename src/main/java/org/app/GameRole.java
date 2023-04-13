package org.app;

public enum GameRole {
    SPECTATOR((byte) 0, "Zuschauer"),
    PLAYER((byte) 1, "Spieler"),
    ADMIN((byte) 2, "Admin");

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
            case 0 -> SPECTATOR;
            case 1 -> PLAYER;
            case 2 -> ADMIN;
            default -> null;
        };
    }
}