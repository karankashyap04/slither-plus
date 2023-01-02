package edu.brown.cs32.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Class representing a User object within the game (with a UUID & username)
 */
public class User {

  private final String id;
  private final String username;

  /**
   * User object constructor -- assigning it a random universally unique
   * identifier (in string form) and a username (as per the input)
   * @param username : the name to which this user is publicly assigned in-game
   */
  public User(String username) {
    this.id = UUID.randomUUID().toString();
    this.username = username;
  }

  /**
   * Accessor method to retrive the UUID in string form
   * @return this user's UUID in string form
   */
  public String getId() {
    return this.id;
  }

  /**
   * Accessor method to retrive the user's username
   * @return this user's username (type: String)
   */
  public String getUsername() {
    return this.username;
  }

  /**
   * Custom User object equals method to determine equality based on
   * whether the 'other User' has an equivalent UUID (in string form) and 
   * username
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return this.id.equals(user.id) && this.username.equals(user.username);
  }

  /**
   * Custom User object hash method that creates hashes based exclusively
   * on a user's UUID (in string form) and username
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.id, this.username);
  }
}
