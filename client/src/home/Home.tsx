import React, { useState, Dispatch, SetStateAction } from "react";

import "./Home.css";

import { registerSocket } from "../App";
import GameState from "../game/GameState";
import { OrbData } from "../game/orb/Orb";
import HowToPlay from "./HowToPlay";

/**
 * Interface representing data for an HTML input that updates metadata based
 * on text editing and has some functionality on keys pressed.
 */
interface ControlledInputProps {
  /** A read-only value representing the value of the text input element. */
  value: string;
  /** A function that sets the value of the given read-only value. */
  setValue: Dispatch<SetStateAction<string>>;
  /** A function for the event the enter key is pressed. */
  onEnter: () => void;
  /** The text placeholder of the input HTML element. */
  placeholder: string;
  /** The class of the input HTML element. */
  className: string;
}

/**
 * Creates and returns an input HTML element that updates metadata based
 * on text editing and with a custom functionality for when the enter key
 * is pressed
 *
 * @param value a read-only value representing the value of the text input element
 * @param setValue a function that sets the given read-only value
 * @param onEnter a function called when the enter key is pressed
 * @param placeholder the text placeholder for the returned input HTML element
 * @param className the class of the returned input HTML element
 * @returns
 */
function ControlledInput({
  value,
  setValue,
  onEnter,
  placeholder,
  className,
}: ControlledInputProps): JSX.Element {
  return (
    <input
      value={value}
      onChange={(ev: React.ChangeEvent<HTMLInputElement>): void =>
        setValue(ev.target.value)
      }
      onKeyDown={(ev: React.KeyboardEvent<HTMLInputElement>): void => {
        if (ev.key === "Enter") {
          onEnter();
        }
      }}
      placeholder={placeholder}
      className={className}
    ></input>
  );
}

/**
 * An interface representing data passed to the home page HTML element
 */
interface HomeProps {
  /** A function that sets whether or not the client has started playing the game */
  setGameStarted: Dispatch<SetStateAction<boolean>>;
  /** A function that sets the current leaderboard (set of scores) for the game */
  setScores: Dispatch<SetStateAction<Map<string, number>>>;
  /** A function that sets the game code for the lobby the client is playing in */
  setGameCode: Dispatch<SetStateAction<string>>;
  /** A metadata representation of the current state of the game */
  gameState: GameState;
  /** A function that sets the current state of the game */
  setGameState: Dispatch<SetStateAction<GameState>>;
  /** A list of all orbs stored in metadata form */
  orbSet: Set<OrbData>;
}

/**
 * Creates and returns the home page, rendering a button which displays
 * how-to-play instructions upon clicking, an input box for specifying one's
 * username, a button to create a new game, and an input box for specifying a
 * custom, already live game, with a button to join said game
 *
 * @param setGameStarted A function that sets whether or not the client has started playing the game
 * @param setScores A function that sets the current leaderboard (set of scores) for the game
 * @param setGameCode A function that sets the game code for the lobby the client is playing in
 * @param gameState A metadata representation of the current state of the game
 * @param setGameState A function that sets the current state of the game
 * @param orbSet A list of all orbs stored in metadata form
 * @returns the home page of the Slither+ game
 */
export default function Home({
  setGameStarted,
  setScores,
  setGameCode,
  gameState,
  setGameState,
  orbSet,
}: HomeProps): JSX.Element {
  const [username, setUsername] = useState("");
  const [inputGamecode, setInputGamecode] = useState("");
  const [errorText, setErrorText] = useState("");
  const [displayHowToPlay, setDisplayHowToPlay] = useState(false);

  // registers the client's websocket to handle joining a new game
  const startNewGame = (): void => {
    if (username.trim().length === 0) {
      setErrorText("Your username should be non-empty!");
      return;
    }
    setErrorText("");
    try {
      registerSocket(
        setScores,
        setGameStarted,
        setErrorText,
        setGameCode,
        orbSet,
        gameState,
        setGameState,
        username,
        false
      );
    } catch (e) {
      // check server status
      setErrorText("Error: Could not connect to server!");
    }
  };

  // registers the client's websocket to handle joining a game with a code
  const startGameWithCode = (): void => {
    if (username.trim().length === 0) {
      //check that name is not empty
      setErrorText("Your username should be non-empty!");
      return;
    }
    setErrorText("");
    try {
      registerSocket(
        setScores,
        setGameStarted,
        setErrorText,
        setGameCode,
        orbSet,
        gameState,
        setGameState,
        username,
        true,
        inputGamecode
      );
    } catch (e) {
      // check server status
      setErrorText("Error: Could not connect to server!");
    }
  };

  return (
    <div className="main-container">
      <div className="how-to-play-display">
        {displayHowToPlay ? (
          <HowToPlay setDisplayHowToPlay={setDisplayHowToPlay} />
        ) : null}
      </div>
      <div className="HomeContainer">
        <button
          className="btn btn-light how-to-play-button"
          aria-label="How To Play button"
          onClick={() => setDisplayHowToPlay(true)}
        >
          How to play?
        </button>
        <h1 className="main-title">
          Slither
          <span className="title-plus" aria-label="Title: Slither+">
            +
          </span>
        </h1>
        <h2
          className="username-prompt"
          aria-label="Prompt: Enter your username"
        >
          Enter your username:
        </h2>
        <ControlledInput
          value={username}
          setValue={setUsername}
          onEnter={() => {
            if (inputGamecode.length === 0) {
              startNewGame();
            } else {
              startGameWithCode();
            }
          }}
          placeholder="Type your username here:"
          className="username-input"
          aria-label="Username input box"
        />
        <p className="error-text">{errorText}</p>
        <div className="container">
          <div className="row">
            <div className="col-lg-5 col-md-5 col-sm-12">
              <button
                className="btn btn-light new-game-button"
                aria-label="New Game Button"
                onClick={startNewGame}
              >
                Create a new game
              </button>
            </div>
            <div className="col-lg-2 col-md-2 col-sm-12">
              <div className="or-text">OR</div>
            </div>
            <div className="col-lg-5 col-md-5 col-sm-12">
              <h4
                className="join-with-gamecode-text"
                aria-label="Prompt: Join with a game code"
              >
                Join with a game code
              </h4>
              <ControlledInput
                value={inputGamecode}
                setValue={setInputGamecode}
                onEnter={startGameWithCode}
                placeholder="Enter gamecode here:"
                className="gamecode-input"
                aria-label="Gamecode input box"
              />
              <br />
              <button
                className="btn btn-outline-light"
                aria-label="Join game button"
                onClick={startGameWithCode}
              >
                Join with a game code
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
