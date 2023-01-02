package edu.brown.cs32.server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs32.actionHandlers.NewClientHandler;
import edu.brown.cs32.actionHandlers.UpdatePositionHandler;
import edu.brown.cs32.exceptions.ClientAlreadyExistsException;
import edu.brown.cs32.exceptions.IncorrectGameCodeException;
import edu.brown.cs32.exceptions.InvalidRemoveCoordinateException;
import edu.brown.cs32.exceptions.MissingFieldException;
import edu.brown.cs32.exceptions.MissingGameStateException;
import edu.brown.cs32.exceptions.SocketAlreadyExistsException;
import edu.brown.cs32.exceptions.UserNoGameCodeException;
import edu.brown.cs32.exceptions.GameCodeNoGameStateException;
import edu.brown.cs32.exceptions.GameCodeNoLeaderboardException;
import edu.brown.cs32.gameState.GameState;
import edu.brown.cs32.gamecode.GameCode;
import edu.brown.cs32.gamecode.GameCodeGenerator;
import edu.brown.cs32.leaderboard.Leaderboard;
import edu.brown.cs32.message.Message;
import edu.brown.cs32.message.MessageType;
import edu.brown.cs32.user.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * This class is used to define the server for the Slither+ game. It creates an interface through
 * which the server listens for websocket connections on the port 9000, and can then receive
 * message through those connections and perform the required game updates on the server-side and
 * for all the clients, based on the type and data of the received message.
 */
public class SlitherServer extends WebSocketServer {

  private final Set<WebSocket> allConnections; // stores all connections
  private final Set<WebSocket> inactiveConnections; // stores connections for clients whose users are not actively playing
  private final Map<User, String> userToGameCode; // maps users to the game code for the game they are in
  private final Map<String, Leaderboard> gameCodeToLeaderboard; // maps game codes to the leaderboard for the game
  private final Map<WebSocket, User> socketToUser; // maps websockets to the user associated with that connections
  private final Map<String, GameState> gameCodeToGameState; // maps game codes to game states (for the same game)
  private final Map<GameState, Set<WebSocket>> gameStateToSockets; // maps game states to all the websockets for users in that game

  /**
   * Constructor for the SlitherServer class. Calls the code in the WebSocketServer constructor
   * to begin listening for connections on the specified port, and instantiates all of the
   * instance variables of the class.
   *
   * @param port - an int: the port on which we want the server to listen for websocket connections.
   */
  public SlitherServer(int port) {
    super(new InetSocketAddress(port));
    this.allConnections = new HashSet<>();
    this.inactiveConnections = new HashSet<>();
    this.userToGameCode = new HashMap<>();
    this.gameCodeToLeaderboard = new HashMap<>();
    this.socketToUser = new HashMap<>();
    this.gameCodeToGameState = new HashMap<>();
    this.gameStateToSockets = new HashMap<>();
  }

  /**
   * Generates and returns a set of all of the currently valid game codes (game codes for all of the
   * ongoing games).
   *
   * @return a set of strings, containing all of the currently valid game codes.
   */
  public Set<String> getExistingGameCodes() { return this.gameCodeToLeaderboard.keySet(); }

  /**
   * Sends a json String message (messageJson) to all of the clients (via their websockets) within
   * the provided gameState (i.e. all of the clients playing together in some game).
   *
   * @param gameState - a GameState object: the GameState whose associated clients need to be sent
   *                  the message.
   * @param messageJson a String: the json message to be sent to all of the clients (via their
   *                    respective websocket connections) associated with the provided GameState.
   */
  public void sendToAllGameStateConnections(GameState gameState, String messageJson) {
    Set<WebSocket> gameSockets = this.gameStateToSockets.get(gameState);
    for (WebSocket webSocket : gameSockets) {
      webSocket.send(messageJson);
    }
  }

  /**
   * Adds a mapping from a provided User to a provided game code.
   *
   * @param gameCode - a String: The game code that the user needs to be mapped to.
   * @param user - a User: The user that needs to be mapped to the provided game code.
   * @return a boolean: false if the user was already present in the userToGameCode map (in which
   * case this method was called erraneously since the user already exists); else, the desired
   * mapping is added into userToGameCode and true is returned.
   */
  public boolean addGameCodeToUser(String gameCode, User user) {
    if (this.userToGameCode.containsKey(user))
      return false;
    this.userToGameCode.put(user, gameCode);
    return true;
  }

  /**
   * Adds a mapping from a provided WebSocket to a provided User.
   *
   * @param webSocket - a WebSocket: The WebSocket that needs to be mapped to the provided user.
   * @param user - a User: The User that the provided WebSocket needs to be mapped to.
   * @return a boolean: false if the websocket was already present in the socketToUser map (in which
   * case this method was called erraneously since the websocket already exists, and must already
   * be associated with some user); else, the desired mapping is added into socketToUser and true
   * is returned.
   */
  public boolean addWebsocketUser(WebSocket webSocket, User user) {
    if (this.socketToUser.containsKey(webSocket))
      return false;
    this.socketToUser.put(webSocket, user);
    return true;
  }

  /**
   * Adds a provided WebSocket to the Set of WebSockets associated with (mapped to by) the relevant
   * GameState.
   * If the GameState associated with the provided game code does not exist, then a
   * MissingGameStateException is thrown.
   *
   * @param gameCode - a String: The gameCode associated with the relevant game. This is used to
   *                 access the GameState associated with the relevant game.
   * @param webSocket - a WebSocket: The WebSocket that needs to be added to the Set of WebSockets
   *                  mapped to by the relevant GameState.
   * @return a boolean: false if the provided WebSocket is already present in the set mapped to by
   * the relevant GameState (in which case this method was called erraneously since the socket has
   * already been added); else, the WebSocket is added to the desired set, and true is returned.
   * @throws MissingGameStateException - this exception is thrown if no GameState associated with
   * the provided game code can be found (the game code provided is incorrect).
   */
  public boolean addSocketToGameState(String gameCode, WebSocket webSocket) throws MissingGameStateException {
    GameState gameState = this.gameCodeToGameState.get(gameCode);
    if (gameState == null || this.gameStateToSockets.get(gameState) == null)
      throw new MissingGameStateException(MessageType.JOIN_ERROR);
    if (this.gameStateToSockets.get(gameState).contains(webSocket))
      return false;
    this.gameStateToSockets.get(gameState).add(webSocket);
    return true;
  }

  /**
   * Defines the code to be run when a new WebSocket connection is opened by the client (on the
   * specified port). The provided WebSocket is added to the Set of all sockets, and a json message
   * is sent to the client (associated with the newly opened connection) stating that a new
   * socket has been opened successfully.
   *
   * @param webSocket - a WebSocket: The WebSocket connection object associated with the client
   *                  who opened the new connection.
   * @param clientHandshake - a ClientHandshake: The ClientHandshake object associated with the new
   *                        opened connection (unused in this method).
   */
  @Override
  public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
    System.out.println("server: onOpen called");
    this.allConnections.add(webSocket);
    this.inactiveConnections.add(webSocket);
    System.out.println("server: New client joined - Connection from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    String jsonResponse = this.serialize(this.generateMessage("New socket opened", MessageType.SUCCESS));
    webSocket.send(jsonResponse);
  }

  /**
   * Defines the code to be run when an existing WebSocket connection is closed. If the snake
   * associated with the provided WebSocket connection is currently live in a game, then it is
   * killed and removed from the game. If the snake is already dead, then no changes are made.
   *
   * @param webSocket - a WebSocket: The WebSocket connection that is currently being closed.
   * @param code - an int: The code number associated with the connection closing message (not being
   *             used in this function).
   * @param reason - a String: The reason why the connection is being closed (not being used in this
   *               function).
   * @param remote - a boolean: Indicating whethere the closed connection was remote or not (not
   *               being used in this function).
   */
  @Override
  public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
    System.out.println("server: onClose called");
    User user = this.socketToUser.get(webSocket);
    if (user == null)
      return;
    String gameCode = this.userToGameCode.get(user);
    if (gameCode == null)
      return;
    GameState gameState = this.gameCodeToGameState.get(gameCode);
    if (gameState == null)
      return;
    gameState.updateOtherUsersWithRemovedPositions(user, webSocket, this.gameStateToSockets.get(gameState), this);
    this.handleUserDied(user, webSocket, gameState);
  }

  /**
   * Defines the code to be run when a message is received by an existing websocket on the
   * server-side. The json messae is deserialized, and then processed in a new thread.
   *
   * @param webSocket - a WebSocket: The WebSocket connection object corresponding to the socket
   *                  that has received the message.
   * @param jsonMessage - a String: The JSON string message received by the provided WebSocket.
   */
  @Override
  public void onMessage(WebSocket webSocket, String jsonMessage) {
    System.out.println("server: Message received from client: " + jsonMessage);
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Message> jsonAdapter = moshi.adapter(Message.class);
    String jsonResponse;
    try {
      Message deserializedMessage = jsonAdapter.fromJson(jsonMessage);
      Thread t = new Thread(() -> handleOnMessage(webSocket, deserializedMessage));
      t.start();
    } catch (IOException e) {
      MessageType messageType =
          this.socketToUser.containsKey(webSocket) ? MessageType.ERROR : MessageType.JOIN_ERROR;
      jsonResponse = this.serialize(
          this.generateMessage("The server could not deserialize the client's message",
              messageType));
      webSocket.send(jsonResponse);
    }
  }

  /**
   * Defines the code to be run when an error occurs while receiving or processing a WebSocket
   * message.
   *
   * @param connection - a WebSocket: The WebSocket which experienced an error while receiving,
   *                   processing, or sending a message.
   * @param e - an Exception: The Exception object that was created as a result of the error that
   *          occurred.
   */
  @Override
  public void onError(WebSocket connection, Exception e) {
    if (connection != null) {
      this.allConnections.remove(connection);
      System.out.println("server: An error occurred from: " + connection.getRemoteSocketAddress().getAddress().getHostAddress());
    }
  }

  /**
   * Defines the code to be run when the Server starts listening for new websocket connections
   * (and messages on the opened connections) on the desired port.
   */
  @Override
  public void onStart() {
    System.out.println("server: Server started!");
  }

  /**
   * Serializes and returns a provided Message object into a JSON String.
   *
   * @param message - a Message: the Message object that needs to be serialized into a JSON String.
   * @return a String - the serialized Message object.
   */
  public String serialize(Message message) {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Message> jsonAdapter = moshi.adapter(Message.class);
    return jsonAdapter.toJson(message);
  }

  /**
   * Takes a String message (msg) and a MessageType, and generates a Message object with this data.
   *
   * @param msg - a String: The String message that is to be included within the data field of the
   *            created Message object.
   * @param messageType - a MessageType: The type of the Message being created (the enum value for
   *                    the type field of the created Message object).
   * @return a Message - created using the provided msg String and MessageType.
   */
  private Message generateMessage(String msg, MessageType messageType) {
    Map<String, Object> data = new HashMap<>();
    data.put("msg", msg);
    return new Message(messageType, data);
  }

  /**
   * Defines the code to be run when a user's snake has died so that the all associations with the
   * user and their WebSocket are removed from the leaderboard, the gameState and also all game
   * code relations.
   * Furthermore, if the user was the last user in the associated game, the Leaderboard and
   * GameState for that game are nullified, in order to close those games and prevent the redundant
   * generation and sending of new orbs and leaderboard data to clients that do not exist.
   *
   * @param user - a User: The User whose snake death needs to be processed.
   * @param webSocket - a WebSocket: The WebSocket connection associated with the user whose snake
   *                  death needs to be processed.
   * @param gameState - a GameState: The GameState corresponding to the game in which the user
   *                  was playing.
   */
  public void handleUserDied(User user, WebSocket webSocket, GameState gameState) {
    System.out.println("handleUserDied");
    this.allConnections.remove(webSocket);
    this.inactiveConnections.remove(webSocket);
    if (user == null)
      return;
    this.socketToUser.remove(webSocket);
    String gameCode = this.userToGameCode.get(user);
    if (gameCode == null)
      return;
    this.userToGameCode.remove(user);
    Leaderboard leaderboard = this.gameCodeToLeaderboard.get(gameCode);
    leaderboard.removeUser(user);

    System.out.println(this.gameStateToSockets.get(gameState).size());

    if (this.gameStateToSockets.get(gameState).size() == 1) {
      this.gameStateToSockets.get(gameState).remove(webSocket);
      this.gameStateToSockets.remove(gameState);
      gameState = null; // so that it gets garbage collected eventually
      leaderboard = null; // if there is nobody in that game, then we delete that leaderboard
      this.gameCodeToGameState.remove(gameCode);
      this.gameCodeToLeaderboard.remove(gameCode);
    } else {
      this.gameStateToSockets.get(gameState).remove(webSocket);
    }
  }

  /**
   * Defines the code that needs to be run when a User's score needs to be updated - accesses the
   * Leaderboard associated with the game in which the user is playing, and updates the user's
   * score based on the size of the orb they consumed.
   *
   * @param user - a User: The User whose score needs to be updated.
   * @param gamestate - a GameState: The GameState associated with the game in which the user is
   *                  playing.
   * @param orbValue - an Integer: The value by which the provided User's score has to be updated.
   */
  public void handleUpdateScore(User user, GameState gamestate, Integer orbValue) {
    Leaderboard leaderboard = this.gameCodeToLeaderboard.get(gamestate.getGameCode());
    Integer currUserScore = leaderboard.getCurrentScore(user);
    leaderboard.updateScore(user, currUserScore + orbValue);
  }

  /**
   * This function is called from within the overriden onMessage function and is executed in a new
   * thread. It takes the WebSocket from which the message was received, along with the deserialized
   * message, and processes it.
   * Received messages are processed differently based on their type. There are specific processing
   * instructions defined for the following MessageTypes: NEW_CLIENT_WITH_CODE, NEW_CLIENT_NO_CODE,
   * UPDATE_POSITION.
   *
   * @param webSocket - a WebSocket: The WebSocket connection on which the JSON message (which has
   *                  since been deserialized) was received.
   * @param deserializedMessage - a Message: The deserialized JSON Message that was received from
   *                            some client.
   */
  public void handleOnMessage(WebSocket webSocket, Message deserializedMessage) {
    String jsonResponse;
    try {
      switch (deserializedMessage.type()) {
        case NEW_CLIENT_WITH_CODE -> { // create a new user and add them to the provided game code if it is valid.
          this.inactiveConnections.remove(webSocket);
          User newUser = new NewClientHandler().handleNewClientWithCode(deserializedMessage, webSocket, this);
          // throw errors if the desired game code, Leaderboard, or GameState do not already exist
          String existingGameCode = this.userToGameCode.get(newUser);
          if (existingGameCode == null) {
            throw new UserNoGameCodeException(MessageType.JOIN_ERROR);
          }
          if (this.gameCodeToLeaderboard.get(existingGameCode) == null) {
            throw new GameCodeNoLeaderboardException(MessageType.JOIN_ERROR);
          }
          this.gameCodeToLeaderboard.get(existingGameCode).addNewUser(newUser);
          if (!this.gameCodeToGameState.containsKey(this.userToGameCode.get(newUser)))
            throw new GameCodeNoGameStateException(MessageType.JOIN_ERROR);

          this.addSocketToGameState(existingGameCode, webSocket);
          this.gameCodeToGameState.get(existingGameCode).addUser(newUser);
          GameState gameState = this.gameCodeToGameState.get(this.userToGameCode.get(newUser));
          gameState.createNewSnake(newUser, webSocket, this.gameStateToSockets.get(gameState), this);

          GameCode.sendGameCode(existingGameCode, this.gameCodeToGameState.get(existingGameCode), this);

          Message message = this.generateMessage("New client added to existing game code", MessageType.JOIN_SUCCESS);
          message.data().put("gameCode", this.userToGameCode.get(newUser));
          jsonResponse = this.serialize(message);
          webSocket.send(jsonResponse);
          break;
        }
        case NEW_CLIENT_NO_CODE -> { // create a new user and also make a new game code, GameState, and Leaderboard for their new game.
          this.inactiveConnections.remove(webSocket);
          User newUser = new NewClientHandler().handleNewClientNoCode(deserializedMessage, webSocket, this);
          String gameCode = new GameCodeGenerator().generateGameCode(this.getExistingGameCodes());
          this.gameCodeToGameState.put(gameCode, new GameState(this, gameCode));
          this.gameCodeToGameState.get(gameCode).addUser(newUser);
          this.gameStateToSockets.put(this.gameCodeToGameState.get(gameCode), new HashSet<>());
          Leaderboard leaderboard = new Leaderboard(this.gameCodeToGameState.get(gameCode), this);
          leaderboard.addNewUser(newUser);
          this.userToGameCode.put(newUser, gameCode);
          this.gameCodeToLeaderboard.put(gameCode, leaderboard);

          boolean result = this.addSocketToGameState(gameCode, webSocket);

          if (!result)
            throw new SocketAlreadyExistsException(MessageType.JOIN_ERROR);

          GameCode.sendGameCode(gameCode, this.gameCodeToGameState.get(gameCode), this);

          GameState gameState = this.gameCodeToGameState.get(gameCode);
          gameState.createNewSnake(newUser, webSocket, this.gameStateToSockets.get(gameState), this);

          Message message = this.generateMessage("New client added to new game", MessageType.JOIN_SUCCESS);
          message.data().put("gameCode", gameCode);
          jsonResponse = this.serialize(message);
          webSocket.send(jsonResponse);
          break;
        }
        case UPDATE_POSITION -> { // update the position of the snake of the user associated with the websocket
          // on which this message was received
          User user = this.socketToUser.get(webSocket);
          String gameCode = this.userToGameCode.get(user);
          if (gameCode == null)
            throw new UserNoGameCodeException(MessageType.ERROR);
          GameState gameState = this.gameCodeToGameState.get(gameCode);
          if (gameState == null)
            throw new GameCodeNoGameStateException(MessageType.ERROR);

          Thread t = new Thread(() -> {
            try {
              new UpdatePositionHandler().handlePositionUpdate(user, deserializedMessage, gameState, webSocket, this.gameStateToSockets.get(gameState), this);
            } catch (MissingFieldException e) {
              String res = this.serialize(this.generateMessage("The message sent by the client was missing a required field", e.messageType));
              webSocket.send(res);
            } catch (InvalidRemoveCoordinateException e) {
              String res = this.serialize(this.generateMessage("Incorrect toRemove coordinate provided", e.messageType));
              webSocket.send(res);
            }
          });
          t.start();
          break;
        }
        default -> {
          MessageType messageType = this.socketToUser.containsKey(webSocket) ? MessageType.ERROR : MessageType.JOIN_ERROR;
          jsonResponse = this.serialize(this.generateMessage("The message sent by the client had an unexpected type", messageType));
          webSocket.send(jsonResponse);
          break;
        }
      }
    } catch (MissingFieldException e) {
      jsonResponse = this.serialize(this.generateMessage("The message sent by the client was missing a required field", e.messageType));
      webSocket.send(jsonResponse);
    } catch (ClientAlreadyExistsException e) {
      jsonResponse = this.serialize(this.generateMessage("Tried to add a client that already exists", e.messageType));
      webSocket.send(jsonResponse);
    } catch (IncorrectGameCodeException e) {
      jsonResponse = this.serialize(this.generateMessage("The provided gameCode was incorrect", e.messageType));
      webSocket.send(jsonResponse);
    } catch (UserNoGameCodeException e) {
      jsonResponse = this.serialize(this.generateMessage("User had no corresponding game code", e.messageType));
      webSocket.send(jsonResponse);
    } catch (GameCodeNoGameStateException e) {
      jsonResponse = this.serialize(this.generateMessage("Game code had no corresponding game state", e.messageType));
      webSocket.send(jsonResponse);
    } catch (GameCodeNoLeaderboardException e) {
      jsonResponse = this.serialize(this.generateMessage("Game code had no corresponding leaderboard", e.messageType));
      webSocket.send(jsonResponse);
    } catch (SocketAlreadyExistsException e) {
      jsonResponse = this.serialize(this.generateMessage("This socket already exists", e.messageType));
      webSocket.send(jsonResponse);
    } catch (MissingGameStateException e) {
      jsonResponse = this.serialize(this.generateMessage("Game state cannot be found", e.messageType));
      webSocket.send(jsonResponse);
    }
  }

  /**
   * Main method for the SlitherServer class. Used to instantiate an object of the SlitherServer
   * class and get it to listen for WebSocket connections on port 9000.
   *
   * @param args - a String array: arguments provided to the main method (unused in this case).
   */
  public static void main(String args[]) {
    final int port = 9000;
    new SlitherServer(port).start();
  }

}
