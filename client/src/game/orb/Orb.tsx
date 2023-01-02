import { Position } from "../GameState";
import "./orb.css";

/** A metadata representation of the orb */
export interface OrbData {
  /** The position of the orb on the map */
  position: Position;
  /** The size of the orb, as an enum */
  orbSize: OrbSize;
  /** The hexidecimal color of the orb, serialized as a string */
  color: string;
}

/** An enum representing the two possible orb sizes */
export enum OrbSize {
  SMALL = "SMALL",
  LARGE = "LARGE",
}

/**
 * Renders an orb appropriately according to the given metadata (position,
 * size, and color), at the given offset on the map.
 * @param orbInfo the metadata representation of the orb
 * @param offset the offset at which to render the orb
 * @returns a HTML element rendering of the orb
 */
export default function Orb({
  orbInfo,
  offset,
}: {
  orbInfo: OrbData;
  offset: Position;
}): JSX.Element {
  return (
    <div
      className="circle"
      style={{
        top: `${orbInfo.position.y + offset.y}px`,
        left: `${orbInfo.position.x + offset.x}px`,
        height: `${orbInfo.orbSize === OrbSize.SMALL ? 7.5 : 15}px`,
        width: `${orbInfo.orbSize === OrbSize.SMALL ? 7.5 : 15}px`,
        backgroundColor: `${orbInfo.color}`,
        boxShadow: `0 0 10px 1px ${orbInfo.color}`,
      }}
    ></div>
  );
}
