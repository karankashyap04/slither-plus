package edu.brown.cs32.orb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Testing class for OrbColor.java in 'orb' directory
 */
public class OrbColorTest {
    
    /**
     * Verifies creation of pseudo-random orb color (in hexidecimal) 
     * by affirming a length of 7 ('# + 6 hex-characters')
     */
    @Test
    public void testRandomOrbColor() {
        assertEquals(OrbColor.generate().length(), 7);
    }

    /**
     * Verifies leading hashtag in generated orb color (to confirm hexidecimal form)
     */
    @Test
    public void testOrbColorLeadingHashtag() {
        assertEquals(OrbColor.generate().substring(0,1), "#");
    }
}
