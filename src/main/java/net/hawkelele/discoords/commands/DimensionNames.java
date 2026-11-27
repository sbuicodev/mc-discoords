package net.hawkelele.discoords.commands;

// How /dc writes a dimension: Overworld, Nether and The End by name; others by their id, without "minecraft:"
public final class DimensionNames {
    private static final String MINECRAFT = "minecraft:";

    private DimensionNames() {
    }

    public static String of(String dimension) {
        return switch (dimension) {
            case MINECRAFT + "overworld" -> "Overworld";
            case MINECRAFT + "the_nether" -> "Nether";
            case MINECRAFT + "the_end" -> "The End";
            default -> dimension.startsWith(MINECRAFT) ? dimension.substring(MINECRAFT.length()) : dimension;
        };
    }
}
