# Limited AFK

A fairly straight-forward server-sided mod to limit how much your players can AFK and tracks all players' playtime.

##### This mod should only be installed on a server.

### Player Commands
* ```/playtime``` Tells the player how much real-time they have played and AFK'd on the server

### Admin Commands & Configuration
Configuration can be done through the properties file generated when loaded on a server or through commands.
* ```/playtime <player>``` Gets the playtime of the specified player
* ```/playtime all``` Gets the playtime of all players who have logged on the server. Dumps to console and gives a clipboard copy to the admin.
* ```/playtime actions <player>``` Gets the amount of time since a player has performed one of the actions tracked by the AFK algorithm.
* ```/limitedafk config``` Gets the current configuration settings
* ```/limitedafk config enabled <true/false>``` Enables/disables the AFK tracker
* ```/limitedafk config ignoreCreativeAndSpectator <true/false>``` Sets whether the AFK tracker should mark Creative and Spectator players as AFK
* ```/limitedafk config allowedAfkPercentage <0-100>``` Sets the percentage of a player's playtime can be spent AFK before kicking them
* ```/limitedafk config announceAfk <true/false>``` Sets whether a chat message is announced when a player goes AFK or comes back from AFK
* ```/limitedafk config afkTimer <60+>``` Sets how many seconds of inactivity it takes until a player is marked as AFK


### LICENSE NOTICE
By using this project in any form, you hereby give your "express assent" for the terms of the license of this project, and acknowledge that I, BorisShoes, have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License.
