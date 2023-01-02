package edu.brown.cs32.exceptions;

import edu.brown.cs32.message.MessageType;

/**
 * Custom Exception for when there is no GameState associated with the provided Game Code.
 */
public class GameCodeNoGameStateException extends Exception {

    public final MessageType messageType; // the MessageType to be sent to the client in the failure response

    /**
     * Constructor for the GameCodeNoGameStateException class.
     *
     * @param messageType - a MessageType: the MessageType to be sent to the client in the failure response.
     */
    public GameCodeNoGameStateException(MessageType messageType) {
        this.messageType = messageType;
    }
}
