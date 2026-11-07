# Casino Mod — Minecraft Java 1.20.1 (Forge)

A casino minigame for Forge 1.20.1 / Forge 47.2.0.

## Features
- Right-click the Casino Key to open the casino.
- Searchable item catalog includes vanilla items and items registered by installed Forge mods.
- Select any catalog item as the prize; the chosen prize is shown in the prize slot.
- Put a wager in the stake slot to roll. A win awards the selected item; a loss consumes the wager.
- Gold-and-purple casino interface and a custom-drawn Casino Key texture.
- Wager odds are based on item category, rarity and stack size (maximum 90%).

## Build
Requirements: Java 17 and Gradle 8.1 or newer.

Run `gradle build` from this folder. The JAR is created in `build/libs/`.

Install the same JAR on the Forge 1.20.1 client and server. Give yourself the key with:
```
/give @p casinomod:casino_key
```

## Notes
The prize catalog is populated from the Forge item registry on the client, and the selected registry ID is validated on the server before it is accepted.
