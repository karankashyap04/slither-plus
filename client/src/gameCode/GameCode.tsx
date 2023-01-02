import "./GameCode.css";

/**
 * Renders the current lobby's game code, in the top left.
 * @param gameCode the lobby's game code
 * @returns an HTML element rendering the lobby's game code
 */
export default function GameCode({
  gameCode,
}: {
  gameCode: string;
}): JSX.Element {
  return (
    <div className="codeDisplay">
      <p className="code-tagline">Your game code:</p>
      <p className="codeText">{gameCode}</p>
    </div>
  );
}
