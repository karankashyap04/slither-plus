import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import App from "../App";
import userEvent from "@testing-library/user-event";

test("Checking the how-to-play button works properly", async () => {
  render(<App />);

  let howToPlayButton = screen.getByLabelText("How To Play button");
  expect(howToPlayButton).toBeInTheDocument(); //button exists

  userEvent.click(howToPlayButton); //click button
  let howToPlayScreen = screen.getByLabelText("How To Play Box");
  expect(howToPlayScreen).toBeInTheDocument(); //instructions appear
});

test("Checking the title exists", async () => {
  render(<App />);

  let title = screen.getByLabelText("Title: Slither+");
  expect(title).toBeInTheDocument();
});

test("Checking the input box exists", async () => {
  render(<App />);

  let inputPrompt = screen.getByLabelText("Prompt: Enter your username");
  expect(inputPrompt).toBeInTheDocument();

  let inputBox = screen.getByPlaceholderText("Type your username here:");
  expect(inputBox).toBeInTheDocument();
});

test("Checking the start new game button exists", async () => {
  render(<App />);

  let newGameButton = screen.getByLabelText("New Game Button");
  expect(newGameButton).toBeInTheDocument();
});

test("Checking join with gamecode", async () => {
  render(<App />);

  let joinGamePrompt = screen.getByLabelText("Prompt: Join with a game code");
  expect(joinGamePrompt).toBeInTheDocument();

  let joinGameInput = screen.getByPlaceholderText("Enter gamecode here:");
  expect(joinGameInput).toBeInTheDocument();

  let joinGameButton = screen.getByLabelText("Join game button");
  expect(joinGameButton).toBeInTheDocument();
});
