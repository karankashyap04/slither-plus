package edu.brown.cs32.gamecode;

import java.util.Random;
import java.util.Set;

/**
 * GameCodeGenerator class to pseudo-randomly generate new game codes
 */
public class GameCodeGenerator {

  /**
   * Randomly generates a 6-uppercase-letter game code
   * @return a 6-uppercase-letter game code
   */
  private String createGameCode() {
    Random random = new Random();
    String gameCode = "";
    for (int i = 0; i < 6; i++)
      gameCode += (char) (random.nextInt(65, 91));
    return gameCode;
  }

  /**
   * Given a set of existingGameCodes, continuously generates new game codes
   * until one not present in the set is created (in order to create a new,
   * non-overlapping game)
   * @param existingGameCodes : the set of existingGameCodes already within
   * use in other games
   * @return the new (not previously in existence) game code to be used
   * within a game being generated (type: String)
   */
  public String generateGameCode(Set<String> existingGameCodes) {
    String gameCode = this.createGameCode();
    while (existingGameCodes.contains(gameCode)) {
      gameCode = this.createGameCode();
    }
    return gameCode;
  }

}
