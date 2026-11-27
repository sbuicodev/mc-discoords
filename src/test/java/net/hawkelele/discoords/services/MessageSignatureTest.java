package net.hawkelele.discoords.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageSignatureTest {
    @Test
    void playerIsAddedAtTheEnd() {
        assertEquals("🧭 Base: `1, 2, 3` ~ *[Steve]*", MessageSignature.of("🧭 Base: `1, 2, 3`", "Steve"));
    }
}
