package edu.brown.cs32.message;
import java.util.Map;

/**
 * Message record for transferring data between the server and client
 * 
 * Contains MessageType in order to denote the type of message to be 
 * dealt with (and thus the procedure that this message should be processed by)
 * 
 * Contains Map<String, Object> to store all relevant data with regards to
 * the operations that will be performed on a message of its MessageType
 */
public record Message(MessageType type, Map<String, Object> data) {}
