import MessageType from "./messageTypes";
import { Position } from "../game/GameState";
import { OrbData } from "../game/orb/Orb";

/**
 * The default generic interface for any message sent or received to
 * the Slither+ server.
 */
export default interface Message {
  /** The type (purpose) of the message sent or received */
  type: MessageType;
  /** The data sent as part of the message */
  data: any;
}

/**
 * An interface representing a message sent to the server registering
 * the client for a new game, with no specified game code.
 */
export interface NewClientNoCodeMessage {
  /** The type (purpose) of the message sent or received */
  type: MessageType.NEW_CLIENT_NO_CODE;
  /** The data sent with the message - the client's username */
  data: {
    username: string;
  };
}

/**
 * An interface representing a message sent to the server registering
 * the client for a presently running game, with a specified game code.
 */
export interface NewClientOldCodeMessage {
  /** The type (purpose) of the message sent or received */
  type: MessageType.NEW_CLIENT_WITH_CODE;
  /**
   * The data sent with the message - the client's username and
   * specified game code for the lobby to join
   */
  data: {
    username: string;
    gameCode: string;
  };
}

/**
 * An interface representing a message sent to the server to update
 * the position of the current client's snake across all clients.
 */
export interface UpdatePositionMessage {
  /** The type (purpose) of the message sent or received */
  type: MessageType.UPDATE_POSITION;
  /**
   * The data sent with the message - the position of segments in the
   * client's snake to be removed and added, to simulate movement
   */
  data: {
    add: Position;
    remove: Position;
  };
}

/**
 * Sends a message to the server via the given websocket to register
 * the client for a new game.
 * @param socket the client's websocket for communication with the server
 * @param username the client's username
 */
export function sendNewClientNoCodeMessage(
  socket: WebSocket,
  username: string
): void {
  const message: NewClientNoCodeMessage = {
    type: MessageType.NEW_CLIENT_NO_CODE,
    data: {
      username: username,
    },
  };
  socket.send(JSON.stringify(message));
}

/**
 * Sends a message to the server via the given websocket to register
 * the client for a joining a game with the given game code.
 * @param socket the client's websocket for communication with the server
 * @param username the client's username
 * @param gameCode the game code of the lobby to join
 */
export function sendNewClientWithCodeMessage(
  socket: WebSocket,
  username: string,
  gameCode: string
): void {
  const message: NewClientOldCodeMessage = {
    type: MessageType.NEW_CLIENT_WITH_CODE,
    data: {
      username: username,
      gameCode: gameCode,
    },
  };
  socket.send(JSON.stringify(message));
}

/**
 * Sends a message to the server via the given websocket to update the
 * current client's position across all other clients.
 * @param socket the client's websocket for communication with the server
 * @param add the position of the segment of the snake to add
 * @param remove the position of the segment of the snake to remove
 */
export function sendUpdatePositionMessage(
  socket: WebSocket,
  add: Position,
  remove: Position
): void {
  const message: UpdatePositionMessage = {
    type: MessageType.UPDATE_POSITION,
    data: {
      add: add,
      remove: remove,
    },
  };
  socket.send(JSON.stringify(message));
}

// TYPES FOR MESSAGES RECEIVED FROM THE SERVER

/**
 * An interface representing a message received from the server to update
 * the current lobby's leaderboard for the client.
 */
export interface leaderboardData {
  /** The type (purpose) of the message sent or received */
  type: MessageType.UPDATE_LEADERBOARD;
  /**
   * The data sent with the message - a list of leaderboard
   * entries, containing each user and their score
   */
  data: {
    leaderboard: leaderboardEntry[];
  };
}

/**
 * An interface representing an entry on the leaderboard, with
 * a user and their score.
 */
export interface leaderboardEntry {
  /** A client's username */
  username: string;
  /** The respective client's score */
  score: number;
}

/**
 * An interface representing a message received from the server to pass along
 * the current lobby's game code.
 */
export interface gameCode {
  /** The type (purpose) of the message sent or received */
  type: MessageType.SET_GAME_CODE;
  /** The data sent with the message - the current lobby's game code */
  data: {
    gameCode: string;
  };
}

/**
 * An interface representing a message received from the server to update
 * the current lobby's orbs.
 */
export interface orbsData {
  /** The type (purpose) of the message sent or received */
  type: MessageType.SEND_ORBS;
  /** The data sent with the message - the current lobby's orbs */
  data: {
    orbSet: Set<OrbData>;
  };
}

/**
 * An interface representing a message received from the server to notify
 * the client that they have died, from a collision.
 */
export interface YouDiedMessage {
  /** The type (purpose) of the message sent or received */
  type: MessageType.YOU_DIED;
  /** The data sent with the message - none */
  data: {};
}

/**
 * An interface representing a message received from the server to notify
 * that another client has died.
 */
export interface OtherUserDiedMessage {
  /** The type (purpose) of the message sent or received */
  type: MessageType.OTHER_USED_DIED;
  /**
   * The data sent with the message - the positions of the other
   * client to be removed, from rendering
   */
  data: {
    removePositions: Position[];
  };
}

/**
 * An interface representing a message received from the server to notify
 * the client that their snake has increased in length.
 */
export interface IncreaseOwnLengthMessage {
  /** The type (purpose) of the message sent or received */
  type: MessageType.INCREASE_OWN_LENGTH;
  /**
   * The data sent with the message - the positions of the segments
   * to be added to the client's snake
   */
  data: {
    newBodyParts: Position[];
  };
}

/**
 * An interface representing a message received from the server to notify
 * the client that other client's snake has increased in length.
 */
export interface IncreaseOtherLengthMessage {
  /** The type (purpose) of the message sent or received */
  type: MessageType.INCREASE_OTHER_LENGTH;
  /**
   * The data sent with the message - the positions of the other
   * client's snake's segments to be added
   */
  data: {
    newBodyParts: Position[];
  };
}
