package net.hawkelele.discoords.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DimensionNamesTest {
    @Test
    void vanillaDimensionsHaveTheirNames() {
        assertEquals("Overworld", DimensionNames.of("minecraft:overworld"));
        assertEquals("Nether", DimensionNames.of("minecraft:the_nether"));
        assertEquals("The End", DimensionNames.of("minecraft:the_end"));
    }

    @Test
    void otherMinecraftDimensionsDropTheNamespace() {
        assertEquals("mining", DimensionNames.of("minecraft:mining"));
    }

    @Test
    void modDimensionsKeepTheirId() {
        assertEquals("mymod:deep/caves", DimensionNames.of("mymod:deep/caves"));
    }
}
