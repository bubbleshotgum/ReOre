# ReOre

A Paper Minecraft plugin for regenerating ores, crops, wood, and custom blocks with configurable cooldowns, SQLite storage, and optional WorldEdit region support.

[![Modrinth](https://img.shields.io/badge/Modrinth-ReOre-1bd96a?logo=modrinth&logoColor=white)](https://modrinth.com/plugin/reore)
[![GitHub](https://img.shields.io/badge/GitHub-bubbleshotgum%2FReOre-181717?logo=github&logoColor=white)](https://github.com/bubbleshotgum/ReOre)

## Features

- **Customizable Cooldowns & Placeholders**: Configure per-material regeneration cooldowns (in milliseconds) and replacement placeholder blocks (defaulting to `minecraft:bedrock`) in `config.yml`.
- **Works Beyond Ores**: Regenerate ores, crops, wood, or any block material configured under `resources`.
- **Normal Drops & XP**: Cancels default block break behavior to compute drops using the player's held tool (respecting enchantments like Fortune or Silk Touch), applies `1` tool durability damage, grants XP, places items into the player's inventory (dropping overflow on the ground), and replaces the block with its placeholder until the timer expires.
- **Action Bar Cooldown Feedback**: Left-clicking a block currently on cooldown cancels interaction and shows remaining time in the player's action bar.
- **SQLite Persistence via HikariCP**: Stores registered block locations in a local SQLite database (`<database>.db`) with connection pooling for fast loading on startup.
- **Optional WorldEdit Integration**: Bulk-add or bulk-remove matching blocks inside your active WorldEdit selection when `pos` is omitted.
- **Built-in Localization**: Includes English (`en`) and Russian (`ru`) locale entries out of the box, with support for adding custom locale blocks in `config.yml`.

## Requirements

- **Server Software**: Paper (uses Paper's Brigadier lifecycle command API and Adventure components; listed on Modrinth for Bukkit/Paper/Purpur/Spigot across Minecraft `1.16.x`–`1.21.x` and `26.x`, with `api-version: 1.21` in `plugin.yml`).
- **Player Access**: Commands require an in-game **Operator (`OP`)** player.
- **Optional Dependency**: [WorldEdit](https://modrinth.com/plugin/worldedit) (`softdepend`) for region-based `add` and `remove` actions.
- **Runtime Libraries**: `com.zaxxer:HikariCP:5.1.0` and `org.slf4j:slf4j-simple:2.0.13` (declared in `plugin.yml` `libraries`).

## Installation

1. Download the `ReOre` plugin JAR and place it in your server's `plugins/` folder.
2. *(Optional)* Install **WorldEdit** if you want to add or remove blocks using region selections.
3. Start or restart the server to generate the default `config.yml` and SQLite database file (`reore.db` by default).
4. Edit `plugins/ReOre/config.yml` to adjust cooldowns, placeholders, SQLite settings, or locale, then run `/reore reload` in-game.

## Commands

All commands are executed in-game by an **OP** player under `/reore`:

| Command | Argument | Required? | Behavior when omitted | Description |
| :--- | :--- | :--- | :--- | :--- |
| `/reore add [pos]` | `pos` (`x y z`) | No | Uses the player's active WorldEdit selection in the current world | Registers a single block at `pos` or all matching configured blocks in the selection |
| `/reore remove [pos]` | `pos` (`x y z`) | No | Uses the player's active WorldEdit selection in the current world | Unregisters a single block at `pos` or matching registered blocks in the selection |
| `/reore list [page]` | `page` (integer `>= 1`) | No | Defaults to page `1` | Lists up to `10` registered block locations per page (`world x y z`) |
| `/reore reload` | — | — | — | Reloads `config.yml`, cancels active cooldown timers (restoring blocks), and reloads data |

## Configuration Overview

A minimal snippet of `config.yml`:

```yaml
resources:
  COAL_ORE:
    cooldown: 300000
    placeholder: minecraft:bedrock
  DIAMOND_ORE:
    cooldown: 3600000
    placeholder: minecraft:bedrock

sql:
  database: db
  locationsTable: locs

locale: en
locales:
  en:
    # ...
  ru:
    # ...