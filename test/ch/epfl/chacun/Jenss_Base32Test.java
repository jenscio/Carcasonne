package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Jenss_Base32Test {


    @Test
    void isValidWorksCorrectly() {
        assertFalse(Base32.isValid(null));
        assertFalse(Base32.isValid(""));
        assertFalse(Base32.isValid("0"));
        assertFalse(Base32.isValid("1"));
        assertFalse(Base32.isValid("a"));
        assertFalse(Base32.isValid("8"));

        assertTrue(Base32.isValid(Base32.ALPHABET));
    }

    @Test
    void encode5BitsWorksCorrectly() {
        assertEquals("A",Base32.encodeBits5(0));
        assertEquals("7",Base32.encodeBits5(31));
        assertEquals("C",Base32.encodeBits5(2));
    }

    @Test
    void encode10BitsWorksCorrectly() {
        assertEquals("CH",Base32.encodeBits10(71));
        assertEquals("BA",Base32.encodeBits10(32));
    }

    @Test
    void decodeWorksCorrectly(){
        assertEquals(0,Base32.decode("A"));
        assertEquals(31,Base32.decode("7"));
        assertEquals(32,Base32.decode("BA"));
        assertEquals(2,Base32.decode("C"));
        assertEquals(71,Base32.decode("CH"));
    }


}
