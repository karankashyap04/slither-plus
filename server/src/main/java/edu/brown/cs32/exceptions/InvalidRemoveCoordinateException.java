package edu.brown.cs32.exceptions;

import edu.brown.cs32.message.MessageType;

/**
 * Custom exception for when we try and remove a body part of a snake from a position at which
 * it's last current body part is not located.
 */
public class InvalidRemoveCoordinateException extends Exception {

  public final MessageType messageType; // the MessageType to be sent to the client in the failure response

  /**
   * Constructor for the InvalidRemoveCoordinateException class.
   *
   * @param messageType - a MessageType: the MessageType to be sent to the client in the failure response.
   */
  public InvalidRemoveCoordinateException(MessageType messageType) {
    this.messageType = messageType;
  }

}
