package edu.brown.cs32.message;

/**
 * MessageType enum to represent all message communication types (including
 * those between the server and client)
 */
public enum MessageType {
  NEW_CLIENT_NO_CODE,
  NEW_CLIENT_WITH_CODE,
  UPDATE_LEADERBOARD,
  SEND_ORBS,
  REMOVE_ORB,
  UPDATE_POSITION,
  UPDATE_SCORE,
  INCREASE_OWN_LENGTH,
  INCREASE_OTHER_LENGTH,
  YOU_DIED,
  OTHER_USER_DIED,
  SET_GAME_CODE,
  ERROR,
  SUCCESS,
  JOIN_ERROR,
  JOIN_SUCCESS
}
