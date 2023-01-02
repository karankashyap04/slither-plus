package edu.brown.cs32.exceptions;

import edu.brown.cs32.message.Message;
import edu.brown.cs32.message.MessageType;

/**
 * Custom exception for when a message received from the client (via its websocket connection)
 * does not contain data for all of the expected fields and subfields.
 */
public class MissingFieldException extends Exception{
  final Message incompleteMessage; // the received deserialized message with the missing field(s)
  public final MessageType messageType; // the MessageType to be sent to the client in the failure response

  /**
   * Constructor for the MissingFieldException class.
   *
   * @param incompleteMessage - a Message: the deserialized message with the missing fields(s).
   * @param messageType - a MessageType: the MessageType to be sent to the client in the failure response.
   */
  public MissingFieldException(Message incompleteMessage, MessageType messageType) {
    this.incompleteMessage = incompleteMessage;
    this.messageType = messageType;
  }
}
