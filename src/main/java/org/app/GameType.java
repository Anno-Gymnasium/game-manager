package org.app;

public enum GameType {
    MATCHLESS((byte) 0, "All vs All"),
    MATCHING((byte) 1, "1v1-Matches"),
    TREE((byte) 2, "Baum-Spiel");

    private final byte value;
    private final String name;

    GameType(byte value, String name) {
        this.name = name;
        this.value = value;
    }
    public static GameType fromClass(Class<?> gameClass) {
        if (gameClass == org.app.game_classes.MatchlessGame.class) {
            return MATCHLESS;
        } else if (gameClass == org.app.game_classes.MatchingGame.class) {
            return MATCHING;
        } else if (gameClass == org.app.game_classes.TreeGame.class) {
            return TREE;
        } else {
            throw new IllegalArgumentException("Ungültige Spielklasse: " + gameClass);
        }
    }

    public byte getValue() {
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