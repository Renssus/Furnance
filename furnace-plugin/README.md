# VirtualFurnace Plugin

Een Minecraft Paper 1.21.1 plugin die virtuele ovens biedt voor spelers met bepaalde ranks/permissions.

## Features

- **Virtuele Furnace** (`/furnace`) - Standaard oven
- **Virtuele Blast Furnace** (`/blastfurnace`) - Blast furnace voor ertsen (2x sneller)
- **Virtuele Smoker** (`/smoker`) - Smoker voor voedsel (2x sneller)

Elke speler heeft zijn eigen persoonlijke oven van elk type. De inhoud blijft opgeslagen, zelfs na uitloggen of server restart.

## Installatie

1. Bouw de plugin met Maven:
```bash
cd furnace-plugin
mvn clean package
```

2. Kopieer het JAR bestand van `target/VirtualFurnace-1.0.0.jar` naar je server's `plugins` folder.

3. Herstart de server.

## Permissions

| Permission | Beschrijving |
|------------|--------------|
| `furnace.use.furnace` | Toegang tot virtuele furnace |
| `furnace.use.blastfurnace` | Toegang tot virtuele blast furnace |
| `furnace.use.smoker` | Toegang tot virtuele smoker |
| `furnace.use.*` | Toegang tot alle virtuele ovens (standaard: op) |

## LuckPerms Configuratie

Voeg permissions toe aan een rank:

```bash
/lp group vip permission set furnace.use.furnace true
/lp group vip permission set furnace.use.blastfurnace true
/lp group vip permission set furnace.use.smoker true
```

Of gebruik de wildcard:
```bash
/lp group vip permission set furnace.use.* true
```

## Commands

| Command | Permission | Beschrijving |
|---------|------------|--------------|
| `/furnace` | `furnace.use.furnace` | Open je virtuele furnace |
| `/blastfurnace` | `furnace.use.blastfurnace` | Open je virtuele blast furnace |
| `/smoker` | `furnace.use.smoker` | Open je virtuele smoker |

## Data Opslag

Speler data wordt opgeslagen in JSON formaat in:
```
plugins/VirtualFurnace/playerdata/<uuid>.json
```

## Vereisten

- Paper/Spigot 1.21.1+
- Java 21+
- LuckPerms (aanbevolen voor permission management)

## Bouwen

```bash
mvn clean package
```

Het output JAR bestand staat in `target/VirtualFurnace-1.0.0.jar`.
