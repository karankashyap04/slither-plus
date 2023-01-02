package edu.brown.cs32.actionHandlers;

import edu.brown.cs32.exceptions.InvalidRemoveCoordinateException;
import edu.brown.cs32.exceptions.MissingFieldException;
import edu.brown.cs32.gameState.GameState;
import edu.brown.cs32.message.Message;
import edu.brown.cs32.message.MessageType;
import edu.brown.cs32.position.Position;
import edu.brown.cs32.server.SlitherServer;
import edu.brown.cs32.user.User;
import java.util.Map;
import java.util.Set;
import org.java_websocket.WebSocket;

/**
 * UpdatePositionHandler class to allow for snake movement support
 */
public class UpdatePositionHandler {

  /**
   * Activated when SlitherServer receives UPDATE_POSITION method to
   * update for all users (sharing the inputted gameState) where the
   * newly moved snake connected to the inputted webSocket is located
   * 
   * @param thisUser : the user whose snake's position is being updated
   * @param message : the deserialized message from the client containing
   * information about the snake's new position (to be added) and old 
   * position (to be removed)
   * @param gameState : the GameState corresponding to the game in which
   * this UPDATE_POSITION message is being processed
   * @param webSocket : the WebSocket corresponding to the user whose snake's
   * position is being updated
   * @param gameStateSockets : the set of WebSockets for this game so other
   * users can be updated with this user's snake position update
   * @param server : the server through which GameState updates are sent live
   * to all users for synchronicity
   * @throws MissingFieldException if both addData and removeData messages do not each contain an 'x' and 'y' field
   * @throws InvalidRemoveCoordinateException (via updateOwnPositions method call) if the last body part of the snake (which is being attempted to be removed) is not actually the last body part
   */
  public void handlePositionUpdate(User thisUser, Message message, GameState gameState, WebSocket webSocket, Set<WebSocket> gameStateSockets, SlitherServer server) throws MissingFieldException, InvalidRemoveCoordinateException {
    if (!(message.data().containsKey("add") && message.data().containsKey("remove")))
      throw new MissingFieldException(message, MessageType.ERROR);
    Map<String, Double> addData = (Map<String, Double>) message.data().get("add");
    Map<String, Double> removeData = (Map<String, Double>) message.data().get("remove");
    if ((!(addData.containsKey("x") && addData.containsKey("y"))) || (!(removeData.containsKey("x") && removeData.containsKey("y"))))
      throw new MissingFieldException(message, MessageType.ERROR);
    Position toAdd = new Position(addData.get("x"), addData.get("y"));
    Position toRemove = new Position(removeData.get("x"), removeData.get("y"));
    gameState.updateOwnPositions(thisUser, toAdd, toRemove);
    gameState.updateOtherUsersWithPosition(thisUser, toAdd, toRemove, webSocket, gameStateSockets, server);

//    Thread t = new Thread(() -> gameState.collisionCheck(thisUser, toAdd, webSocket, gameStateSockets, server));
    gameState.collisionCheck(thisUser, toAdd, webSocket, gameStateSockets, server);
//    t.start();
  }

}

