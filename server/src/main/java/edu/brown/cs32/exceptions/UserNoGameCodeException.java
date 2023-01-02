package edu.brown.cs32.exceptions;

import edu.brown.cs32.message.MessageType;

/**
 * Custom exception for when a User exists but (incorrectly) has no Game Code associated with them.
 */
public class UserNoGameCodeException extends Exception {

    public final MessageType messageType; // the MessageType to be sent to the client in the failure response

    /**
     * Constructor for the UserNoGameCodeException class.
     *
     * @param messageType - a MessageType: the MessageType to be sent to the client in the failure response.
     */
    public UserNoGameCodeException(MessageType messageType) {
        this.messageType = messageType;
    }
}
