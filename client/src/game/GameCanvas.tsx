import { useEffect, Dispatch, SetStateAction } from "react";

import GameState, { Position } from "./GameState";
import Snake, { SnakeData, SNAKE_VELOCITY } from "./snake/Snake";
import Orb, { OrbData } from "./orb/Orb";
import Border from "./boundary/Boundary";
import OtherSnake from "./snake/OtherSnake";

import { sendUpdatePositionMessage } from "../message/message";

/**
 * The size of the map. The map is rendered centered on the origin, so
 * the map ranges from -x/2 to x/2 horiziontally, and -y/2 to y/2 vertically
 */
const canvasSize: Position = { x: 3000, y: 3000 };
/** The current position of the client's mouse on the screen */
const mousePos: Position = { x: 0, y: 0 };
/**
 * The offset from the coordinates of the client's snake's head to the
 * middle of the window
 */
const offset: Position = { x: 0, y: 0 };
// let lastUpdatedPosition: Position = { x: 0, y: 0 };
// let lastUpdatedTime: number = new Date().getTime();

/**
 * An interface representing data passed to the HTML element responsible for
 * rendering the Slither+ game map
 */
interface GameCanvasProps {
  /** A metadata representation of the current state of the game */
  gameState: GameState;
  /** A function that sets the current state of the game */
  setGameState: Dispatch<SetStateAction<GameState>>;
  /** The client's websocket for communication with the Slither+ server */
  socket: WebSocket;
}

/**
 * Returns an HTML element that renders the Slither+ game map, which includes
 * your snake, whose head is always at the screen's center, all other snakes in
 * the game, all existing orbs, and the map border.
 * @param gameState A metadata representation of the current state of the game
 * @param setGameState A function that sets the current state of the game
 * @param user The username of the client
 * @param websocket The client's websocket for communication with the Slither+ server
 * @returns a rendered representation of the current game map for the client
 */
export default function GameCanvas({
  gameState,
  setGameState,
  socket,
}: GameCanvasProps): JSX.Element {
  const onMouseMove = (e: MouseEvent) => {
    mousePos.x = e.pageX;
    mousePos.y = e.pageY;
  };

  const updatePositions = () => {
    const newGameState: GameState = { ...gameState };
    const updatedSnake: SnakeData = moveSnake(gameState.snake, socket);
    // constantly update your own snake using moveSnake
    newGameState.snake = updatedSnake;
    setGameState(newGameState);
  };

  useEffect(() => {
    // updates position of the client's snake every 50 ms
    const interval = setInterval(updatePositions, 50);
    // updates mouse position when moved, determines target direction for snake
    window.addEventListener("mousemove", onMouseMove);

    return () => {
      // clean up upon closing
      clearInterval(interval);
      window.removeEventListener("mousemove", onMouseMove);
    };
  }, []);

  // calculate offset to center snake on screen and place other objects relative to snake
  const front: Position | undefined = gameState.snake.snakeBody.peekFront();
  if (front !== undefined) {
    offset.x = window.innerWidth / 2 - front.x;
    offset.y = window.innerHeight / 2 - front.y;
  }

  return (
    <div>
      <Snake snake={gameState.snake} offset={offset} />
      {Array.from(gameState.orbs).map((orb: OrbData, ind: number) => (
        <Orb orbInfo={orb} offset={offset} key={ind} />
      ))}
      <OtherSnake positions={gameState.otherBodies} offset={offset} />
      snakes
      <Border boundaries={canvasSize} offset={offset} />
    </div>
  );
}

/**
 * Changes the given snake's velocity to follow the mouse's position,
 * and sends the new position to the Slither+ server
 * @param snake A metadata representation of the client's snake
 * @param socket The client's websocket for communication with the Slither+ server
 * @returns the newly updated metadata for the client's snake
 */
export function moveSnake(snake: SnakeData, socket: WebSocket): SnakeData {
  // remove last position from the end (to simulate movement)
  const removePosition: Position | undefined = snake.snakeBody.pop();
  const front: Position | undefined = snake.snakeBody.peekFront();
  if (front !== undefined) {
    const accel_angle: number = Math.atan2(
      // find the angle of acceleration based on your current position and the mouse position
      mousePos.y - offset.y - front.y,
      mousePos.x - offset.x - front.x
    );
    let vel_angle: number = Math.atan2(snake.velocityY, snake.velocityX);
    const angle_diff = mod(accel_angle - vel_angle, 2 * Math.PI);
    // changes the angle of velocity to move towards the mouse position
    vel_angle += angle_diff < Math.PI ? 0.1 : -0.1;

    // calculate new velocity
    snake.velocityX = SNAKE_VELOCITY * Math.cos(vel_angle);
    snake.velocityY = SNAKE_VELOCITY * Math.sin(vel_angle);

    // find new position of head based on velocity
    const newPosition: Position = {
      x: front.x + snake.velocityX,
      y: front.y + snake.velocityY,
    };

    // add new position to the front (to simulate movement)
    snake.snakeBody.unshift({ x: newPosition.x, y: newPosition.y });

    if (removePosition !== undefined) {
      const toAdd: Position = {
        x: Number(newPosition.x.toFixed(2)),
        y: Number(newPosition.y.toFixed(2)),
      };
      const toRemove: Position = {
        x: Number(removePosition.x.toFixed(2)),
        y: Number(removePosition.y.toFixed(2)),
      };
      // send message to server with add and remove positions
      sendUpdatePositionMessage(socket, toAdd, toRemove);
    }
  }
  return snake;
}

/**
 * Takes the modulo of the first argument by the second argument (n % m)
 * @param n the number whose modulo is being calculated
 * @param m the modulus of the operation
 */
export function mod(n: number, m: number): number {
  return ((n % m) + m) % m;
}
