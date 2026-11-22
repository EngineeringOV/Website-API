package io.github.engineeringov.website.api.minecraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftNamesTest {

    @Test
    void validNames() {
        assertTrue(MinecraftNames.valid("Bob"));
        assertTrue(MinecraftNames.valid("Some_Name_123456"));
        assertFalse(MinecraftNames.valid("Bo"));
        assertFalse(MinecraftNames.valid("SeventeenCharName"));
        assertFalse(MinecraftNames.valid("john.doe"));
        assertFalse(MinecraftNames.valid("Björn"));
        assertFalse(MinecraftNames.valid("Bob(1)"));
        assertFalse(MinecraftNames.valid(null));
    }

    @Test
    void suffixStaysWithin16() {
        assertEquals("Bob(2)", MinecraftNames.withSuffix("Bob", 2));
        assertEquals("SixteenCharsN(1)", MinecraftNames.withSuffix("SixteenCharsName", 1));
        assertEquals("SixteenChars(12)", MinecraftNames.withSuffix("SixteenCharsName", 12));
        assertEquals(16, MinecraftNames.withSuffix("SixteenCharsName", 1).length());
    }

    @Test
    void parseSuffixed() {
        MinecraftNames.Suffixed s = MinecraftNames.parse("Bob(2)");
        assertEquals("Bob", s.base());
        assertEquals(2, s.n());
        assertNull(MinecraftNames.parse("Bob"));
        assertTrue(MinecraftNames.parse("SixteenCharsN(1)").is("SixteenCharsName"));
        assertTrue(MinecraftNames.parse("bob(2)").is("Bob"));
        assertFalse(MinecraftNames.parse("Bob(2)").is("Bobby"));
    }
}
