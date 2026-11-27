package net.hawkelele.discoords.commands;

// The text /dc posts: a message without a dimension is in the Overworld
public final class CoordinatesMessage {
    private static final String ICON = "🧭";
    private static final String OVERWORLD = "minecraft:overworld";

    private CoordinatesMessage() {
    }

    public static String coordinates(double x, double y, double z) {
        return Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z);
    }

    public static String of(String name, double x, double y, double z, String dimension) {
        String text = ICON + " " + name + ": `" + coordinates(x, y, z) + "`";

        if (dimension.equals(OVERWORLD)) {
            return text;
        }

        return text + " (" + DimensionNames.of(dimension) + ")";
    }
}
