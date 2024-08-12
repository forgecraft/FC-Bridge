# FC Bridge

Our Forgecraft Bridge mod is intended as a small bridge mod that provides essential features for our testing server when we need them.

---

> ## Publishing
> 
> Builds are published to the following repositories:
> 
> `https://maven.forgecraft.net/repository/maven-public/`
> 
> `https://github.com/forgecraft/fc-bridge/releases`

---

## Features

### QOL

- Player AFK marker in the tab menu
  - Configurable in the server config file

### Misc

- Ding on login (when `ding` is not present)
- Time taken to start toast on load complete
- Toast control for Advancements, Recipes, and Tutorials (When `toastcontrol` is not present)

### Discord

- Discord channel `topic` updating to show specific information about the running server on startup (Server only)

### Commands

- `/fc dev sudo <command>`: Run a command as the server owner
  - Allowed commands are configured in the server config file
- `/fc share-location` to share your location
- `/fc dev spectator` to toggle spectator mode
- `/fc show {screen}` to show a screen
  - `tps` brings up a screen that shows the TPS of dims
  - `client_settings` give control of the toast stuff

## TODO

- [ ] Home commands (with a nice gui)
- [ ] Inventory snapshotting and restoring
