package edu.brown.cs32.exceptions;

import edu.brown.cs32.message.MessageType;

/**
 * Custom exception for when an expected GameState does not exist.
 */
public class MissingGameStateException extends Exception {

    public final MessageType messageType; // the MessageType to be sent to the client in the failure response

    /**
     * Constructor for the MissingGameStateException class.
     *
     * @param messageType - a MessageType: the MessageType to be sent to the client in the failure response.
     */
    public MissingGameStateException(MessageType messageType) {
        this.messageType = messageType;
    }
}
