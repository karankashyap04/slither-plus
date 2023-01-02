package edu.brown.cs32.orb;

import java.util.Random;

/**
 * OrbColor abstract class to allow for pseudo-random orb-color generation
 */
public abstract class OrbColor {

    /**
     * Pseudo-randomly generates an orb color from a list of pre-determined hexidecimal color options
     * @return the hexidecimal string representing the color value to which an orb should be assigned
     */
    public static String generate() {
        final String[] colors = { "ff0000", "24f51e", "221fdc", "811fdc", "1fd9dc", "ff6d00", "fdff00", "ff00b2" };
        Random random = new Random();

        return "#" + colors[random.nextInt(0, colors.length)];
    }
}
