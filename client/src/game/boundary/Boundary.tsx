import { Position } from "../GameState";
import "./Boundary.css";

/**
 * Renders the boundary for the map, with 4 borders placed according to the
 * given limits with the map centered at (0, 0), and offsetted on screen by
 * the given amount.
 * @param boundaries the size of the map
 * @param offset the offset at which to renders the boundaries
 * @returns an HTML element rendering the map's boundary
 */
export default function Boundary({
  boundaries,
  offset,
}: {
  boundaries: Position;
  offset: Position;
}): JSX.Element {
  return (
    <div>
      <div
        className="boundary"
        id="top-boundary"
        style={{
          left: offset.x - boundaries.x / 2,
          top: offset.y - boundaries.y / 2,
          width: boundaries.x + "px",
          height: "0px",
        }}
      />
      <div
        className="boundary"
        id="bottom-boundary"
        style={{
          left: offset.x - boundaries.x / 2,
          top: offset.y + boundaries.y / 2,
          width: boundaries.x + "px",
          height: "0px",
        }}
      />
      <div
        className="boundary"
        id="left-boundary"
        style={{
          left: offset.x - boundaries.x / 2,
          top: offset.y - boundaries.y / 2,
          width: "0px",
          height: boundaries.y + "px",
        }}
      />
      <div
        className="boundary"
        id="right-boundary"
        style={{
          left: offset.x + boundaries.x / 2,
          top: offset.y - boundaries.y / 2,
          width: "0px",
          height: boundaries.y + "px",
        }}
      />
    </div>
  );
}
