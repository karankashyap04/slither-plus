

# Slither+

# Project Overview

This project is a modified recreation of the hit video game [slither.io](http://slither.io/). Among notable features contained in this version that the original game lacks is the ability to play with friends in custom game lobbies via unique *game codes*. <br>
With the help of websockets, a multithreaded server, and a dynamic client, this multiplayer game was successfully implemented with minimal lag.

## CSCI0320 Term Project: *Slither+*

This full-stack project is composed of a backend (Java) server combined with a frontend (TypeScript/React) client, which in coalition allow for full functionality of the Slither+ game. It uses websockets as the communication protocol between the server and all the clients.

***Repository Link***: [https://github.com/cs0320-f2022/term-project-kkashyap-mpan11-nharbiso-plestz](https://github.com/cs0320-f2022/term-project-kkashyap-mpan11-nharbiso-plestz)

Total Estimated Completion Time: 300 Hours

## Team Members & Contributions

This project had four contributors: Karan Kashyap (**`kkashyap`**), Mason Pan (**`mpan11`**), Nathan Harbison (**`nharbiso`**), and Paul Lestz (**`plestz`**).

### Contribution details
* Karan: server and client-side networking (with websockets); server-side gameState manipulation: leaderboard updates, collision checking, position updates, length increasing, collision checking, and snake growth; concurrently updating all clients with the latest game state; home screen; server-side documentation
* Mason: moving the snake with the mouse; panning rendered map portion to display snake at the center; other snake rendering; client-side documentation and testing
* Nathan: moving the snake with the mouse; panning rendered map portion to display snake at the center; other snake rendering; server-side boundary collision checking; client-side documentation and testing
* Paul: server-side orb generation and collision checking; client-side orb rendering; client-side game code display; server-side documentation and testing; README


# Project Details: Structure, Design and Implementation

Within this section will be the summary, explanations, and justifications for the design and implementation of the React and Java files within this project.

## Backend (Server)

The heart of the backend is the `SlitherServer`, which is responsible for synchronizing the data between all clients connected to their respective `GameState`. `SlitherServer` operates by routinely sending to and receiving messages from all clients (all properly serialized and deserialized) in order to allow for concurrent playability (via WebSockets). 

Another critical portion of the backend is the `GameState`, one instance of which is assigned to each game (and thus shared by all users within a single game). The `GameState` controls routine orb re-generation, snake location updating across clients, and collision checking (with other snakes, orbs, and the map boundary). 

Other key components to the backend include those in the:
- `leaderboard` package, which control the updating and structure of the leaderboard
- `gamecode` package, which manages the creation of new, unique game codes
- `orb` package, which controls the structure (location, size, color) of each individual orb, as well as routine orb generation
- `message` package, which contains all potential messages to be sent and received between the client
- `position` package, which contains a `Position` record outlining the format of a position (x/y coordinates)
- `user` package, which contains the structure of a user in a `User` class (with a unique UUID and username)
- `exceptions` package, which has custom exception classes for all potential exceptions to be thrown
- `actionHandlers` package, which has custom handlers for the updating of snake positions and the additions of new clients to new and existing games

## Frontend (Client)

The frontend facing portion of the game is split into two main pieces: `Home` and `Game`.

`Home` is responsible for rendering the initial landing screen that will load for the user. It contains a how-to-play button that will display the rules and objectives of the game. It also has an input box for entering a username, and a choice to create a new game or enter a gamecode to join an existing game.

`Game` is broken down into a few more pieces: `GameCanvas`, `Leaderboard`, and `Gamecode`.

`Leaderboard` and `Gamecode` are relatively simple, and are responsible for rendering a leaderboard and the current lobby code, respectively, on the screen. The leaderboard is updated based on the GameState it receives from the server. `Gamestate` is an interface describing the data that will be received from the server. It will contain information about each snake and their name, the positions of all other snakes, the set of all orbs that currently exist, the current scores, and the current lobby's `Gamecode`. 

`GameCanvas` is a bit more complex, and is responsible for the actual frontend functionality of the game. Using an offset, it renders your snake in the middle of the screen, the set of all orbs that are contained in the current game-state data, all other snakes in the game, and the map border. While the map border, rendered in `Boundary`, is static, all the other information is given by the `Gamestate`. This rendering is done on an interval, and refreshes at a set rate. `GameCanvas` is also responsible for the movement of the snake, which is implemented in the `moveSnake` function. This function moves your snake towards your mouse pointer at a constant rate, allowing it to follow your mouse. To show the snake moving, we utilized a double-ended queue. Once `movesnake` calculates a new position for the snake to move to, it adds that to the beginning of the queue and removes the last position, essentially shifting the snake towards the mouse. This allows for a smooth rendering of the snake as it moves.

Each `Snake` is a set of positions. For each position, we render a circle around that position. This rendering is the same for both your snake and every other snake, which is done in `OtherSnake`. Each `Orb` is rendered in a similar way: we take each orb and map its position to a circle, which is then rendered on the screen. 

Finally, `Game` has a set of defined messages which can be sent to the server based on the actions taken by the client. Some notable examples of these messages are `UPDATE_POSITION` and `INCREASE_OWN_LENGTH`. These messages communicate with the server about things that are happening on the client-side so that they can be communicated with all the other clients. This way, every client is receiving the same gamestate, and will have the correct game information rendered on their screen for them to play with.

## Accessibility Limitations & Features

The actual playability of Slither+ relies on two abilities: the ability to see and the ability to engage with a keyboard and mouse. If one is able to do both of these games, they should be able to reasonably engage with the UI of Slither+.

Unfortunately, given the fast-paced nature of Slither+ and the necessity of real-time visual information in informing one’s gameplay, a screen reader would not be able to accurately provide a visually impaired player with information quick enough to feasibly allow for them to play the game.

Additionally, given that the snake is operated by movements of a mouse by a player, it is imperative that a user is able to operate a mouse in order to enable playability of the game.

Note, accessibility features are enabled on text-based pages (e.g. the home screen and the instructions pop-up) via the use of `aria-label` and `aria-roledescription` tags. There is potential for use of a magnifier or screen-reader on such pages. However, the playability of the rest of the game is *dependent* on more than these text-based pages are, as described above. 

# Errors & Bugs

In the current version, there are no known errors or bugs present in Slither+.

# Test Suite Summary

Within the `server/src/test` directory, there are `.java` (Java) files containing tests for various aspects of the project. Sample features tested include those surrounding orbs and game codes.

Within the `client` directory, there are also `test.tsx` (TypeScript) files containing tests for their corresponding `.tsx` files.

Below can be found a summary of each test file's contents. More detailed information on each individual test can be found with the files' documentations themselves.

## Server (Backend) Tests

Contained within this section are notes on each of the backend files that have been tested thus far.

### OrbTest.java

Contains tests to confirm that all methods function properly in the `Orb` class. Among the notable items tested would be that Orb equality and hashes depend solely on an Orb's Position when producing values.

### OrbGeneratorTest.java

Contains tests to confirm that all methods function properly in the `OrbGenerator` class. Among the notable items tested was the functionality of the `generateOrbs` function (with varying levels of existing orbs, and with and without death orbs).

### OrbColorTest.java

Contains tests to confirm that orbs can properly display pseudo-randomly generated colors (from a list of potential options). 

### GameCodeGeneratorTest.java

Contains tests to confirm that all methods function properly in the `GameCodeGenerator` class. Among the notable items tested were the functionality of the `generateGameCode` function (with empty/non-empty sets of existing game codes), as well as that game codes contain the proper (i.e. capital letter) characters. The latter of these was completed using fuzz testing.

## Client (Frontend) Tests

Contained within this section are notes on each of the frontend files that have been tested thus far.

### Home.test.tsx

Tests that the components of the homepage (How to Play button, How to Play instructions, title, name input box, create game button, gamecode input box, join game button) are all on the screen.

### GameCanvas.test.tsx

Tests helper function `mod`.

### Game.test.tsx

Tests helper function `extractLeaderboardMap` on a normal and empty set of leaderboard data.

## Notes on Further Testing

The tests included in the current version of this project do not demonstrate a full and expansive suite of all items that ought to be tested. This is primarily due to the time constraint on this project. A tradeoff had to be made between testing features and developing features, with the latter being the typical choice.

Some features that would have been tested with more time include:
- SlitherServer & WebSocket functionality (server)
- GameState functionality (server)
- Leaderboard Functionality (server)
- Exceptions (server)
- RTL tests of game components (client)
- Integration testing of websocket architecture (client)
- Collision testing (client)

A more detailed description of our plan for further (more comprehensive) testing can be found in this [Testing Plan](https://docs.google.com/document/d/1j6iOY1BceXv0l3akHTfcH9yEss6rN0mA4f7uF5ofPTQ/edit?usp=sharing).

# How To Get Started

## User Guide

To utilize the project (i.e. play the game properly), first run the server in the backend. This can be done by running the `SlitherServer` class through the `server` directory. Then, navigate to the frontend, specifically the `client` directory, and type `npm start` in the terminal. *Note: If the client-side packages/libraries have not been installed before, type `npm i` in the frontend directory ***before*** `npm start`.* 

This should bring up the main menu of the game in your respective browser. From here, enter a username and either create a game or join an existing one with a game code.

Currently, since the webapp has not been deployed, playing with multiple users is being facilitated through ngrok. You would have to install ngrok and create a free account to get your authtoken. Then, when you wish to play with friends, in your terminal, you would have to run:
```
ngrok tcp 9000 --authtoken <your-authtoken-here>
```
_Note: This has to keep running while you are playing with multiple players since it will enable other users to connect to your localhost._

Now, you can take the routing link provided by ngrok and share it with your friends, and they can all paste that into the `host` field of the `AppConfig` object in `App.tsx`. You can then run your server and all the other players simply have to run the client (the frontend code) and the server you are running will get the data from all the clients, enabling everyone to play together!

_Note: Only one player (the one who runs the server) needs to donwload and start ngrok; everyone else simply needs to paste the routing link as described above and run the client-side code (with `npm start`)._

## Accessibility Guide

After loading the web page using the User Guide just above, one is able to use the site's *Accessibility Features* if desired.

*Note*: This project's accessibility features were tested using MacOS's built-in VoiceOver screenreader (which can be found in System Preferences --> Accessibility).

With VoiceOver enabled and the site loaded, it is recommended that one uses the 'Quick Nav' feature, which can be enabled by pressing the left and right arrow keys at the same time.

While in the site, Quick Nav allows for quick cycling through the command history and input area using the (left and right) arrow keys.

Quick Nav can be disabled by tapping the left and right arrow keys simultaneously again.

## Running Tests

Tests may be run in two 'traditional' ways:

1. In the terminal, navigate to the backend directory, and then utilizing [Maven](https://maven.apache.org/) run the command `mvn test`. This will run all tests in this project and demonstrate how, in this version, they all pass.

2. In the file directory, open any given testing class. Then, run that file to run only tests within that class. In IntelliJ, this is done with a green play button. In VSCode, this can be done with `npm test`, which can be accessed after running `npm i` in the `frontend` directory.
