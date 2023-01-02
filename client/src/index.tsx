import React from "react";
import ReactDOM from "react-dom/client";

import "./index.css";

import App from "./App";

/** The root element in index.html at which the React element is placed. */
const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);

root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
