package net.borisshoes.limitedafk.callbacks;

import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.limitedafk.PlayerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.List;

public class TickCallback {
   public static void onTick(MinecraftServer server){
      try{
         if(server.getTickCount() % 20 == 0){
            PlayerList playerManager = server.getPlayerList();
            List<ServerPlayer> players = playerManager.getPlayers();
            for(ServerPlayer player : players){
               DataAccess.getPlayer(player.getUUID(), PlayerData.KEY).update(player);
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
