package edu.brown.cs32.orb;

import edu.brown.cs32.position.Position;
import java.util.Objects;

/**
 * Orb class to represent orb objects (with a position, size, and color)
 */
public class Orb {

  private final Position position;
  private final OrbSize orbSize;
  private final String color;

  /**
   * Orb constructor to assign orb's an inputted Position, OrbSize, and color
   * @param position : the position to which this orb should be assigned
   * @param orbSize : the size to which this orb should be assigned
   * @param color : the color to which this orb should be assigned (hexidecimal string)
   */
  public Orb(Position position, OrbSize orbSize, String color) {
    this.position = position;
    this.orbSize = orbSize;
    this.color = color;
  }

  /**
   * Accessor method to retrive orb Position
   * @return this orb's Position 
   */
  public Position getPosition() {
    return this.position;
  }

  /**
   * Accessor method to retrive orb OrbSize
   * @return this orb's OrbSize
   */
  public OrbSize getSize() {
    return this.orbSize;
  }

  /**
   * Custom Orb object equals method to determine equality based on
   * whether the 'other Orb' has an equivalent Position
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    Orb orb = (Orb) o;
    return this.position.equals(orb.position);
  }

  /**
   * Custom Orb object hash method that creates hashes based exclusively
   * on an orb's Position
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.position);
  }
}
