package org.app;

public enum GameRoles {
    VIEWER(0, "Zuschauer"),
    PLAYER(1, "Spieler"),
    ADMIN(2, "Admin");

    private final int value;
    private final String name;
    GameRoles(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }
    public String getName() {
        return name;
    }

    public static GameRoles getRole(int value) {
        return switch (value) {
            case 0 -> VIEWER;
            case 1 -> PLAYER;
            case 2 -> ADMIN;
            default -> null;
        };
    }
}