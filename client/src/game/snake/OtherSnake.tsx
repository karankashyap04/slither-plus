import { Position } from "../GameState";

/**
 * Renders all other snakes, given a set of each segment's positions (serialized
 * as JSON) - renders a circle for every single position to render them.
 * @param positions the serialized positions of all the other snakes
 * @param offset the offset at which the snake it to be rendered
 * @returns a rendering of all other snake positions on screen
 */
export default function OtherSnake({
  positions,
  offset,
}: {
  positions: Set<string>;
  offset: Position;
}): JSX.Element {
  const parsedPositions: Set<Position> = new Set();
  positions.forEach((posString: string) => {
    parsedPositions.add(JSON.parse(posString));
  });
  return (
    <div>
      {Array.from(parsedPositions).map((bodyPart: Position) => (
        <div
          className="snake"
          style={{ left: bodyPart.x + offset.x, top: bodyPart.y + offset.y }}
        ></div>
      ))}
    </div>
  );
}
