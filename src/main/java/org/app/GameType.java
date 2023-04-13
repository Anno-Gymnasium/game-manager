package org.app;

public enum GameType {
    MATCHLESS(0, "All vs All"),
    MATCHING(1, "1v1-Matches"),
    TREE(2, "Baum-Spiel");

    private final int value;
    private final String name;

    GameType(int value, String name) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    public String getName() {
        return name;
    }

    public static GameType getType(int value) {
        return switch (value) {
            case 0 -> MATCHLESS;
            case 1 -> MATCHING;
            case 2 -> TREE;
            default -> null;
        };
    }
}