package net.hawkelele.discoords.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoordinatesMessageTest {
    @Test
    void coordinatesAreRoundedToWholeBlocks() {
        assertEquals("120, 64, -340", CoordinatesMessage.coordinates(120.4, 63.6, -340.4));
    }

    @Test
    void overworldIsNotWritten() {
        assertEquals("🧭 Base: `120, 64, -340`", CoordinatesMessage.of("Base", 120, 64, -340, "minecraft:overworld"));
    }

    @Test
    void otherDimensionsAreWritten() {
        assertEquals("🧭 Fortress: `10, 70, 20` (Nether)", CoordinatesMessage.of("Fortress", 10, 70, 20, "minecraft:the_nether"));
        assertEquals("🧭 Portal: `0, 50, 0` (The End)", CoordinatesMessage.of("Portal", 0, 50, 0, "minecraft:the_end"));
        assertEquals("🧭 Mine: `1, 2, 3` (mymod:mining)", CoordinatesMessage.of("Mine", 1, 2, 3, "mymod:mining"));
    }
}
