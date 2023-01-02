package edu.brown.cs32.gameState;

import edu.brown.cs32.exceptions.InvalidRemoveCoordinateException;
import edu.brown.cs32.message.Message;
import edu.brown.cs32.message.MessageType;
import edu.brown.cs32.orb.OrbColor;
import edu.brown.cs32.position.Position;
import edu.brown.cs32.orb.Orb;
import edu.brown.cs32.orb.OrbGenerator;
import edu.brown.cs32.orb.OrbSize;
import edu.brown.cs32.server.SlitherServer;
import edu.brown.cs32.user.User;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.java_websocket.WebSocket;

/**
 * GameState class to contain all game data corresponding to this state
 */
public class GameState {

  private final SlitherServer slitherServer; // an instance of the SlitherServer (currently running server)
  private final String gameCode; // the game code corresponding to this GameState
  private final Set<Orb> orbs; // the set of all the orbs currently present in the game
  private int numDeathOrbs; // total count of the number of orbs formed as a result of players dying
  private final OrbGenerator orbGenerator = new OrbGenerator(); //  an OrbGenerator for this game
  private final int ORB_GENERATION_TIME_INTERVAL = 5; // time interval at which new orbs are generated
  private final Map<User, Set<Position>> userToOthersPositions; // maps each user to the positions of every other snake's body parts
  private final Map<User, Set<Position>> userToOwnPositions; // maps each user to their own snake's body parts
  private final Map<User, Deque<Position>> userToSnakeDeque; // maps each user to a double ended queue with their body parts (in order)
  private final int SNAKE_CIRCLE_RADIUS = 35; // radius of each body part of the snakes

  /**
   * GameState constructor to initialize all necessary variables, including
   * a corresponding server and game code unique to this state
   * 
   * Note: Uses a ScheduledThreadPoolExecutor to generate orbs up to the
   * maximum orb count every 5 seconds
   * 
   * @param slitherServer : the server to be used in correlation with this
   * GameState to synchronize all assigned users
   * @param gameCode : the unique game code to be assigned to this state
   */
  public GameState(SlitherServer slitherServer, String gameCode) {
    this.slitherServer = slitherServer;
    this.gameCode = gameCode;
    this.orbs = new HashSet<>();
    this.userToOthersPositions = new HashMap<>();
    this.userToOwnPositions = new HashMap<>();
    this.userToSnakeDeque = new HashMap<>();
    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
    exec.scheduleAtFixedRate(new Runnable() {
      public void run() {
        // code to execute repeatedly
        System.out.println("Try to generate orbs");
        GameState.this.generateOrb();
        GameState.this.sendOrbData();
      }
    }, 0, this.ORB_GENERATION_TIME_INTERVAL, TimeUnit.SECONDS);
  }

  /**
   * Adds a user to this game state
   * @param user : the user to be added to this GameState
   */
  public void addUser(User user) {
    this.userToOwnPositions.put(user, new HashSet<>());
    this.userToOthersPositions.put(user, new HashSet<>());
    this.userToSnakeDeque.put(user, new LinkedList<>());
  }

  /**
   * Fills this GameState's set of orbs up to the maximum orb count (plus death orbs)
   */
  public void generateOrb() {
    this.orbGenerator.generateOrbs(this.orbs, this.numDeathOrbs);
  }

  /**
   * Removes an orb within this GameState's set of orbs if that orb has
   * a position matching that of the input
   * @param position : the position of the orb to be removed
   * @return a boolean indicating whether an orb with a matching position was
   * found (and therefore removed)
   */
  public boolean removeOrb(Position position) {
    Orb removeOrb = new Orb(position, OrbSize.SMALL, "red"); // OrbSize/color irrelevant for hash equality comparison
    if (!this.orbs.contains(removeOrb))
      return false;
    while (this.orbs.contains(removeOrb)) {
      this.orbs.remove(removeOrb);
    }
    return true;
  }

  /**
   * Sends the updated orb data (including newly-generated orbs) to all clients
   * connected to this GameState
   */
  public void sendOrbData() {
    Map<String, Object> orbData = new HashMap<>();
    orbData.put("orbSet", this.orbs);
    String json = this.slitherServer.serialize(new Message(MessageType.SEND_ORBS, orbData));    
    this.slitherServer.sendToAllGameStateConnections(this, json);
  }

  /**
   * Updates the specified user's position based on its current position
   * @param thisUser : the user whose position is to change
   * @param toAdd : the position to add to the front of this user's snake
   * @param toRemove : the position to remove from the back of this user's snake
   * @throws InvalidRemoveCoordinateException if the coordinate attempting to be removed
   * is not the last in the Deque
   */
  public void updateOwnPositions(User thisUser, Position toAdd, Position toRemove) throws InvalidRemoveCoordinateException {
    if (!this.userToOwnPositions.containsKey((thisUser)))
      this.userToOwnPositions.put(thisUser, new HashSet<>());
    this.userToOwnPositions.get(thisUser).add(toAdd);
    this.userToOwnPositions.get(thisUser).remove(toRemove);

    this.userToSnakeDeque.get(thisUser).addFirst(toAdd);
    if (!this.userToSnakeDeque.get(thisUser).peekLast().equals(toRemove)) {
      System.out.println("To remove error");
      System.out.println(this.userToSnakeDeque.get(thisUser));
      throw new InvalidRemoveCoordinateException(MessageType.ERROR);
    }
    this.userToSnakeDeque.get(thisUser).removeLast();
  }

  /**
   * Sends a message to the corresponding client (via webSocket) that this
   * user's snake length has been increased
   * @param webSocket : the webSocket through which to send the increased length message
   * @param newBodyParts : the list of Positions that correspond to the increase in length
   * @param server : the server through which to serialize the message to be sent via webSocket
   */
  private void sendOwnIncreasedLengthBodyParts(WebSocket webSocket, List<Position> newBodyParts, SlitherServer server) {
    Map<String, Object> data = new HashMap<>();
    data.put("newBodyParts", newBodyParts);
    Message message = new Message(MessageType.INCREASE_OWN_LENGTH, data);
    webSocket.send(server.serialize(message));
  }

  /**
   * Sends a message to all other corresponding clients (to this GameState) (via webSocket) that this
   * user's snake length has been increased
   * @param webSocket : the webSocket through which to send the increased length message
   * @param newBodyParts : the list of Positions that correspond to the increase in length
   * @param gameStateSockets : the list of other clients' sockets to receieve the update in this client's snake length
   * @param server : the server through which to serialize the message to be sent via webSocket
   */
  private void sendOthersIncreasedLengthBodyParts(WebSocket webSocket, List<Position> newBodyParts, Set<WebSocket> gameStateSockets, SlitherServer server) {
    Map<String, Object> data = new HashMap<>();
    data.put("newBodyParts", newBodyParts);
    Message message = new Message(MessageType.INCREASE_OTHER_LENGTH, data);
    String jsonMessage = server.serialize(message);
    for (WebSocket socket : gameStateSockets) {
      if (socket.equals(webSocket))
        continue;
      socket.send(jsonMessage);
    }
  }

  /**
   * Creates a new snake for this user at a preset position and sends this data to all other users sharing this GameState
   * @param thisUser : the user for which this new snake is being generated
   * @param webSocket : the current user's socket through which to send data to the client
   * @param gameStateSockets : the list of other clients' sockets to receive the creation update of this client's snake
   * @param server : the server through which to serialize the message to be sent via webSocket
   */
  public void createNewSnake(User thisUser, WebSocket webSocket, Set<WebSocket> gameStateSockets, SlitherServer server) {
    List<Position> newSnake = new ArrayList<>();
    for (int i=0; i < 20; i++) {
      Position position = new Position(600, 100 + 5 * i);
      newSnake.add(position);
      this.userToSnakeDeque.get(thisUser).addLast(position);
    }
    this.sendOthersIncreasedLengthBodyParts(webSocket, newSnake, gameStateSockets, server);
  }

  /**
   * Updates the specified user's position based on its current position
   * @param thisUser : the user whose position is to change
   * @param toAdd : the position to add to the front of this user's snake
   * @param toRemove : the position to remove from the back of this user's snake
   * @param webSocket : 
   * @param gameStateSockets :
   * @param server : 
   */
  public void updateOtherUsersWithPosition(User thisUser, Position toAdd, Position toRemove, WebSocket webSocket, Set<WebSocket> gameStateSockets, SlitherServer server) {
    for (User user : this.userToOthersPositions.keySet()) {
      if (user.equals(thisUser))
        continue;
      this.userToOthersPositions.get(user).add(toAdd);
      this.userToOthersPositions.get(user).remove(toRemove);
    }

    Map<String, Object> data = new HashMap<>();
    data.put("add", toAdd);
    data.put("remove", toRemove);
    Message message = new Message(MessageType.UPDATE_POSITION, data);
    String jsonResponse = server.serialize(message);

    for (WebSocket socket : gameStateSockets) {
      if (socket.equals(webSocket))
        continue;
      socket.send(jsonResponse);
    }
  }

  /**
   * This function is called when a user's snake dies. It updates all the other clients in the
   * same game with the information on the latest positions at which the user's snake's body parts
   * were, and instructs the client to remove those body parts so that they are no longer rendered.
   *
   * @param thisUser - a User: the User whose snake body parts need to be removed from all other
   *                 clients in the same game
   * @param webSocket - a WebSocket: The WebSocket connections corresponding to thisUser
   * @param gameStateSockets - a Set of WebSockets: The set of all the websockets corresponding to
   *                         this GameState (i.e. the websockets of all the clients in the same
   *                         game).
   * @param server - a SlitherServer: an instance of the currently running server.
   */
  public void updateOtherUsersWithRemovedPositions(User thisUser, WebSocket webSocket, Set<WebSocket> gameStateSockets, SlitherServer server) {
    List<Position> removedPositions = new ArrayList<>();
    removedPositions.addAll(this.userToOwnPositions.get(thisUser));
    for (Position position : removedPositions) {
      for (User user : this.userToOthersPositions.keySet()) {
        if (user.equals(thisUser))
          continue;
        this.userToOthersPositions.get(user).remove(position);
      }
    }
    Map<String, Object> data = new HashMap<>();
    data.put("removePositions", removedPositions);
    String jsonMessage = server.serialize(new Message(MessageType.OTHER_USER_DIED, data));
    for (WebSocket socket : gameStateSockets) {
      if (socket.equals(webSocket))
        continue;
      socket.send(jsonMessage);
    }
  }

  /**
   * Computes and returns the Euclidean distance between two Positions (two coordinates on the
   * game map).
   *
   * @param firstCenter - a Position: the first position (coordinate) on the game map.
   * @param secondCenter - a Position: the second position (coordinate) on the game map.
   * @return a double: the Euclidean distance between firstCenter and secondCenter.
   */
  private double distance(Position firstCenter, Position secondCenter) {
    return Math.sqrt(Math.pow(firstCenter.x() - secondCenter.x(), 2) + Math.pow(firstCenter.y() - secondCenter.y(), 2));
  }

  /**
   * Takes a double-ended queue containing the positions of the body parts of some snake and returns
   * a List of positions containing the last two body parts of the snake (without modifying the
   * original double-ended queue).
   * @param bodyParts
   * @return
   */
  private List<Position> getLastTwoBodyParts(Deque<Position> bodyParts) {
    Position lastPosition = bodyParts.removeLast();
    Position secondLastPosition = bodyParts.peekLast();
    bodyParts.addLast(lastPosition);
    List<Position> lastTwoBodyParts = new ArrayList<>();
    lastTwoBodyParts.add(secondLastPosition);
    lastTwoBodyParts.add(lastPosition);
    return lastTwoBodyParts;
  }

  /**
   * Computes the coordinates (the Position) at which a new body part should be created for a user
   * (when they eat an orb) so that growth in the length of the snake looks natural and continuous.
   *
   * @param thisUser - a User: the user whose snake's new body part position needs to be computed.
   * @return a Position: the position at which the new body part for the user's snake will be
   * created.
   */
  private Position getNewBodyPartPosition(User thisUser) {
    Deque<Position> userBodyParts = this.userToSnakeDeque.get(thisUser);
    Position newPosition;
    if (userBodyParts.size() == 0)
      newPosition = new Position(600.0, 100.0);
    else if (userBodyParts.size() == 1)
      newPosition = new Position(Math.round(userBodyParts.peekFirst().x() * 100) / 100.0, Math.round((userBodyParts.peekFirst().y() + 5) * 100) / 100.0);
    else {
      List<Position> userLastTwoBodyParts = this.getLastTwoBodyParts(userBodyParts);
      double xDifference = userLastTwoBodyParts.get(0).x() - userLastTwoBodyParts.get(1).x();
      double yDifference = userLastTwoBodyParts.get(0).y() - userLastTwoBodyParts.get(1).y();
      double x = userLastTwoBodyParts.get(1).x() - xDifference;
      double y = userLastTwoBodyParts.get(1).y() - yDifference;
      newPosition = new Position(Math.round(x * 100) / 100.0, Math.round(y * 100) / 100.0);
    }
    return newPosition;
  }

  /**
   * Generates death orbs for a snake when it dies: a large orb is created for every fourth snake
   * body part, and all the clients in the same game are updated with the new orbs so that they can
   * be rendered.
   *
   * @param positions - a List of Positions: the positions of the body parts of the snake that has
   *                  died and needs to be converted ("dissolved") into death orbs.
   */
  private void generateDeathOrbs(List<Position> positions) {
    for (int i=0; i < positions.size(); i++) {
      if (i % 4 != 0)
        continue;
      this.orbs.add(new Orb(positions.get(i), OrbSize.LARGE, OrbColor.generate()));
      this.numDeathOrbs++;
    }
    this.sendOrbData();
  }

  /**
   * Runs a collision check when the position of a snake is updated to see if the snake has eaten an
   * orb, collided with another snake, or collided with the game boundary. If any of these have
   * occurred then the relevant computations, state updates, and client updates are performed.
   *
   * @param thisUser - a User: the user for whom we are conducting the collision check (the position
   *                 of the snake of this user has just been updated).
   * @param latestHeadPosition - a Position: the position (coordinate) to which the head of the
   *                           user's snake has just moved.
   * @param webSocket - a WebSocket: the WebSocket connection object associated with this user
   * @param gameStateSockets - a Set of WebSockets: the set of all the WebSockets for players
   *                         within the same game as this user.
   * @param server - a SlitherServer object: an instance of the server that is currently running.
   */
  public void collisionCheck(User thisUser, Position latestHeadPosition, WebSocket webSocket, Set<WebSocket> gameStateSockets, SlitherServer server) {
    System.out.println("Run collision check");
    Set<Position> otherBodies = this.userToOthersPositions.get(thisUser);
    Set<Orb> allOrbs = new HashSet<>(this.orbs);

    // check if the user's snake has collided with (gone beyond) the game map boundary -- kill
    // the snake if this happens
    if( latestHeadPosition.x() - this.SNAKE_CIRCLE_RADIUS <= -1500 ||
        latestHeadPosition.x() + this.SNAKE_CIRCLE_RADIUS >= 1500 ||
        latestHeadPosition.y() - this.SNAKE_CIRCLE_RADIUS <= -1500 ||
        latestHeadPosition.y() + this.SNAKE_CIRCLE_RADIUS >= 1500
      ) {
      Message userDiedMessage = new Message(MessageType.YOU_DIED, new HashMap<>());
      String jsonMessage = server.serialize(userDiedMessage);
      webSocket.send(jsonMessage);
      this.updateOtherUsersWithRemovedPositions(thisUser, webSocket, gameStateSockets, server);

      List<Position> deadSnakePositions = new ArrayList<>();
      deadSnakePositions.addAll(this.userToSnakeDeque.get(thisUser));
      this.userToOwnPositions.remove(thisUser);
      this.userToOthersPositions.remove(thisUser);
      this.userToSnakeDeque.remove(thisUser);
      server.handleUserDied(thisUser, webSocket, this);
      this.generateDeathOrbs(deadSnakePositions);
      return;
    }

    // check if the user's snake has collided with any other snakes in the same game -- kill the
    // user's snake if this happens
    for (Position otherBodyPosition : otherBodies) {
      if (this.distance(latestHeadPosition, otherBodyPosition) <= this.SNAKE_CIRCLE_RADIUS) {
        Message userDiedMessage = new Message(MessageType.YOU_DIED, new HashMap<>());
        String jsonMessage = server.serialize(userDiedMessage);
        webSocket.send(jsonMessage);

        List<Position> deadSnakePositions = new ArrayList<>();
        deadSnakePositions.addAll(this.userToSnakeDeque.get(thisUser));
        this.updateOtherUsersWithRemovedPositions(thisUser, webSocket, gameStateSockets, server);
        this.userToOwnPositions.remove(thisUser);
        this.userToOthersPositions.remove(thisUser);
        this.userToSnakeDeque.remove(thisUser);
        server.handleUserDied(thisUser, webSocket, this);
        this.generateDeathOrbs(deadSnakePositions);
        return;
      }
    }

    // Check if the user's snake has eaten any orbs -- remove the eaten orbs and increase the length
    // of the snake when this happens
    List<Position> newBodyParts = new ArrayList<>();
    boolean orbCollided = false;
    for (Orb orb : allOrbs) {
      Position orbPosition = orb.getPosition();
      if (this.distance(latestHeadPosition, orbPosition) <= this.SNAKE_CIRCLE_RADIUS) {
        this.removeOrb(orbPosition);
        orbCollided = true;
        Integer orbValue = switch(orb.getSize()) {
          case SMALL -> 1;
          case LARGE -> 5;
        };
        server.handleUpdateScore(thisUser, this, orbValue);

        for (int i=0; i < orbValue; i++) {
          Position newPosition = this.getNewBodyPartPosition(thisUser);
          newBodyParts.add(newPosition);
        }
      }
    }
    if (orbCollided)
      this.sendOrbData();

    if (newBodyParts.size() > 0) {
      // increase the length of the user's own snake with their client
      this.sendOwnIncreasedLengthBodyParts(webSocket, newBodyParts, server);
      // increase the length of the user's snake for every other client in the same game
      this.sendOthersIncreasedLengthBodyParts(webSocket, newBodyParts, gameStateSockets, server);
    }
  }

  /**
   * Provides this GameState's unique game code
   * @return this GameState's unique game code (type: String)
   */
  public String getGameCode() {
    return this.gameCode;
  }
}
