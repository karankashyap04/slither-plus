import { OrbData } from "./orb/Orb";
import { SnakeData } from "./snake/Snake";

/**
 * An interface representing a position on the Slither+ game map
 */
export interface Position {
  /** The x-coordinate of the position (horizontally) */
  x: number;
  /** The y-coordinate of the position (vertically) */
  y: number;
}

/**
 * An interface representing the state of the client's game
 */
export default interface GameState {
  /** A metadata representation of the client's snake */
  snake: SnakeData;
  // otherBodies: Set<Position>;
  /**
   * All other snake positions, all represented by a JSON serialization of
   * a position
   */
  otherBodies: Set<string>;
  /** A metadata representation of all current in-game orbs */
  orbs: Set<OrbData>;

  /** A map of each user to their score */
  scores: Map<String, Number>;
  /** The game code of the current lobby being played */
  gameCode: String;
}
