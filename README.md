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
* ```/afklevel get <player>``` Gets the modified (or default) AFK detection level required for the specified player
* ```/afklevel set <player> <LOW/MEDIUM/HIGH>``` Sets the modified AFK detection level required for the specified player
* ```/afklevel reset <player>``` Resets the modified AFK detection level required for the specified player to the default value

### LICENSE NOTICE
By using this project in any form, you hereby give your "express assent" for the terms of the license of this project, and acknowledge that I, BorisShoes, have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License.
