package edu.brown.cs32.exceptions;

import edu.brown.cs32.message.MessageType;

/**
 * Custom exception for when new client creation is attempted for a user that already exists.
 */
public class ClientAlreadyExistsException extends Exception {

  public final MessageType messageType; // the MessageType to be sent to the client in the failure response

  /**
   * Constructor for the ClientAlreadyExistsException class.
   *
   * @param messageType - a MessageType: the MessageType to be sent to the client in the failure response.
   */
  public ClientAlreadyExistsException(MessageType messageType){
    this.messageType = messageType;
  };

}
