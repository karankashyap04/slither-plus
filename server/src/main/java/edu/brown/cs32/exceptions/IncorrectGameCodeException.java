package edu.brown.cs32.exceptions;

import edu.brown.cs32.message.MessageType;

/**
 * Custom exception for when the user tries to join a game with a Game Code that deos not exist.
 */
public class IncorrectGameCodeException extends Exception{

  public final MessageType messageType; // the MessageType to be sent to the client in the failure response

  /**
   * Constructor for the IncorrectGameCodeException class.
   *
   * @param messageType - a MessageType: the MessageType to be sent to the client in the failure response.
   */
  public IncorrectGameCodeException(MessageType messageType) {
    this.messageType = messageType;
  }

}
