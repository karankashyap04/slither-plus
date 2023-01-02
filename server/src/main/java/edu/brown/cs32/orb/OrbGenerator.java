package edu.brown.cs32.orb;

import edu.brown.cs32.position.Position;
import java.util.Random;
import java.util.Set;

/**
 * OrbGenerator class used for 'randomly' creating orbs to be generated in-game
 */
public class OrbGenerator {

  final int MAX_ORB_COUNT = 150; // 750
  final float MAP_MIN_COORDINATE = -1500.0f + 100.0f;
  final float MAP_MAX_COORDINATE = 1500.0f - 100.0f;

  /**
   * Rounds the inputted float to two decimal places
   * @param value : the float to be rounded to two decimal places
   * @return the inputted float rounded to two decimal places
   */
  private float round(float value) {
    return Math.round(value * 100) / 100.0f;
  }

  /**
   * Fills its input Set<Orb> with orbs up to MAX_ORB_COUNT (plus the amount of death orbs present)
   * Note: Orbs are generated with random Position coordinates, a random OrbSize, and a random hexidecimal color assignment
   * 
   * @param orbs : the set of orbs to which to be filled up to the orb maximum (every 5 seconds)
   * @param numDeathOrbs : the number of death orbs currently present on screen (also to be rendered)
   */
  public void generateOrbs(Set<Orb> orbs, int numDeathOrbs) {
    Random random = new Random();
    int size = orbs.size();
    for (int i = 0; i < this.MAX_ORB_COUNT - size + numDeathOrbs; i++) {
      Orb orb = new Orb(new Position(this.round(random.nextFloat(this.MAP_MIN_COORDINATE, this.MAP_MAX_COORDINATE)),
                                       this.round(random.nextFloat(this.MAP_MIN_COORDINATE, this.MAP_MAX_COORDINATE))),
                        this.generateOrbSize(), OrbColor.generate());
      orbs.add(orb);
    }
  }

  /**
   * Generates a random OrbSize such that 75% of those generated are SMALL
   * and rest are LARGE
   * @return the pseudo-randomly generated OrbSize
   */
  private OrbSize generateOrbSize() {
    // 75% -- small; 25% -- large
    Random random = new Random();
    if (random.nextFloat() <= 0.75)
      return OrbSize.SMALL;
    return OrbSize.LARGE;
  }

}
