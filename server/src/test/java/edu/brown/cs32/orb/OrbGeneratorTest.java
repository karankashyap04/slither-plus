package edu.brown.cs32.orb;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.brown.cs32.position.Position;

/**
 * Testing class for OrbGenerator.java in 'orb' directory
 */
public class OrbGeneratorTest {
    OrbGenerator orbGenerator;

    /**
     * Setup method to instantiate OrbGenerator for use
     */
    @BeforeEach
    public void setup() {
        this.orbGenerator = new OrbGenerator();
    }

    /**
     * Test for orb generation within orb set (starting as empty) up to 150 natural orbs
     */
    @Test
    public void testEmptyOrbSetSize() {
        Set<Orb> gameOrbs = new HashSet<>();
        orbGenerator.generateOrbs(gameOrbs, 0);
        assertEquals(gameOrbs.size(), 150);
    }

    /**
     * Test for orb generation within orb set (starting as non-empty) up to 150 natural orbs
     */
    @Test
    public void testNonEmptyOrbSetSize() {
        Set<Orb> gameOrbs = new HashSet<>();
        Position position = new Position(3.5, 4.5);
        Orb orb = new Orb(position, OrbSize.LARGE, "red");
        gameOrbs.add(orb);
        orbGenerator.generateOrbs(gameOrbs, 0);
        assertEquals(gameOrbs.size(), 150);
    }

    /**
     * Test for orb generation within orb set (starting as empty) up to 150 natural orbs,
     * with the addition of existing death orbs, for a total of 250 orbs (150 natural, 100 death)
     */
    @Test
    public void testDeathOrbSetSize() {
        Set<Orb> gameOrbs = new HashSet<>();
        orbGenerator.generateOrbs(gameOrbs, 100);
        assertEquals(gameOrbs.size(), 250);
    }
}
