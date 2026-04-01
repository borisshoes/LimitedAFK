# Limited AFK

A fairly straight-forward server-sided mod to limit how much your players can AFK and tracks all players' playtime.

##### This mod should only be installed on a server.

### Player Commands
* ```/playtime``` Tells the player how much real-time they have played and AFK'd on the server
* ```/afklist``` Lists the players online and who is/isn't AFK

### Admin Commands & Configuration
Configuration can be done through the properties file generated when loaded on a server or through commands.
* ```/playtime <player>``` Gets the playtime of the specified player
* ```/playtime all``` Gets the playtime of all players who have logged on the server. Dumps to console and gives a clipboard copy to the admin.
* ```/playtime actions <player>``` Gets the amount of time since a player has performed one of the actions tracked by the AFK algorithm.
* ```/limitedafk``` Gets the current configuration settings
* ```/limitedafk enabled <true/false>``` Enables/disables the AFK tracker
* ```/limitedafk ignoreCreativeAndSpectator <true/false>``` Sets whether the AFK tracker should mark Creative and Spectator players as AFK
* ```/limitedafk allowedAfkPercentage <0-100>``` Sets the percentage of a player's playtime can be spent AFK before kicking them
* ```/limitedafk announceAfk <true/false>``` Sets whether a chat message is announced when a player goes AFK or comes back from AFK
* ```/limitedafk afkTimer <60+>``` Sets how many seconds of inactivity it takes until a player is marked as AFK
* ```/limitedafk defaultAfkDetectionLevel <LOW/MEDIUM/HIGH>``` Sets how aggressive the AFK detection is by default (LOW and MEDIUM require various levels of activity, and HIGH requires a captcha)
* ```/limitedafk captchaTimer <120+>``` Sets the interval between when someone suspected of being AFK is given a captcha (in seconds)
* ```/limitedafk logCommandUsage <true/false>``` Sets whether successful command executions are logged to the server console
* ```/afklevel get <player>``` Gets the modified (or default) AFK detection level required for the specified player
* ```/afklevel set <player> <LOW/MEDIUM/HIGH>``` Sets the modified AFK detection level required for the specified player
* ```/afklevel reset <player>``` Resets the modified AFK detection level required for the specified player to the default value

### Permission Nodes
LimitedAFK uses the [Fabric Permissions API](https://github.com/lucko/fabric-permissions-api) for command permissions. Each node has a fallback vanilla permission level for servers without a permissions mod.

#### General
| Node | Default | Description |
|------|---------|-------------|
| `limitedafk.afklist` | `ALL` | Use `/afklist` to see who is AFK |
| `limitedafk.playtime` | `ALL` | Use `/playtime` to check your own playtime |
| `limitedafk.playtime.others` | `GAMEMASTERS` | Use `/playtime <player>` to check another player's playtime |
| `limitedafk.playtime.all` | `GAMEMASTERS` | Use `/playtime all` to dump playtime for all players |
| `limitedafk.playtime.actions` | `GAMEMASTERS` | Use `/playtime actions <player>` to check a player's action timestamps |
| `limitedafk.afklevel.get` | `GAMEMASTERS` | Use `/afklevel get <player>` to read a player's AFK detection level |
| `limitedafk.afklevel.set` | `GAMEMASTERS` | Use `/afklevel set <player> <level>` to change a player's AFK detection level |
| `limitedafk.afklevel.reset` | `GAMEMASTERS` | Use `/afklevel reset <player>` to reset a player's AFK detection level to default |

#### Config
Config commands are generated automatically by BorisLib per config value.

| Node | Default | Description |
|------|---------|-------------|
| `limitedafk.config` | `GAMEMASTERS` | List all config values via `/limitedafk` |
| `limitedafk.config.<name>.get` | `GAMEMASTERS` | Read a specific config value |
| `limitedafk.config.<name>.set` | `GAMEMASTERS` | Change a specific config value |

### Try My Other Mods!
All server-side Fabric mods — no client installation required.

|                                                                                                                | Mod                      | Description                                                                                               | Links                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|:--------------------------------------------------------------------------------------------------------------:|--------------------------|-----------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://cdn.modrinth.com/data/9J7sCd3t/e6ce366187de25be0efc7ecc736fc27f05452888_96.webp" width="32"> | **Arcana Novum**         | Minecraft's biggest server-only full-feature Magic Mod! Adds powerful items, multiblocks and bosses!      | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/ArcanaNovum/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/arcana-novum) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/arcana-novum)                        |
| <img src="https://cdn.modrinth.com/data/xHHbHfVj/c6c224a3d8068cfb9b054e2a03eb9704906dd8cb_96.webp" width="32"> | **Ancestral Archetypes** | A highly configurable, Origins-style mod that lets players pick a mob to gain unique abilities!           | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/AncestralArchetypes) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/ancestral-archetypes) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/ancestral-archetypes) |
| <img src="https://cdn.modrinth.com/data/QfXOzeIK/b35cbf33da842f170d0aa562033aaddc2a9ab653_96.webp" width="32"> | **Ender Nexus**          | Highly configurable /home, /spawn, /warp, /tpa and /rtp commands all in one, and individually disablable. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/EnderNexus/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/ender-nexus) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/ender-nexus-fabric-teleports)          |
| <img src="https://cdn.modrinth.com/data/Z63eULDV/dae01789d609498b8f1637ab31d8fe20b6108020_96.webp" width="32"> | **Fabric Mail**          | An in-game virtual mailbox system for sending packages and messages between online and offline players.   | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-mail/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/fabric-mail) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/fabric-mail)                          |
| <img src="https://cdn.modrinth.com/data/u40ARaBc/028062616fc2fb729afdbdc697d60f93ff61a918_96.webp" width="32"> | **Fabric Trade**         | Adds /trade, a secure player-to-player trading interface.                                                 | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-trade/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/fabric-trade) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/fabric-trade)                       |
| <img src="https://cdn.modrinth.com/data/WdlqG9Gd/a401b9bf08c33d85c907025d6689c657b5168508_96.webp" width="32"> | **Limited AFK**          | AFK detection and management with configurable kick thresholds for servers.                               | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/LimitedAFK/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/limited-afk) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/limited-afk)                           |
| <img src="https://cdn.modrinth.com/data/klpvLefw/97afbda2e56c3f14e04d0f9e0e1fe99db6bd2f27_96.webp" width="32"> | **Links in Chat**        | Makes URLs posted in chat clickable.                                                                      | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-linksinchat/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/links-in-chat) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/links-in-chat)               |


### LICENSE NOTICE
By using this project in any form, you hereby give your "express assent" for the terms of the license of this project, and acknowledge that I, BorisShoes, have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License.
