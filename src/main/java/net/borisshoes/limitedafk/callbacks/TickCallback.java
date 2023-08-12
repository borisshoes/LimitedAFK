package net.borisshoes.limitedafk.callbacks;

import net.borisshoes.limitedafk.cca.IPlayerProfileComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

import static net.borisshoes.limitedafk.cca.PlayerComponentInitializer.PLAYER_DATA;

public class TickCallback {
   public static void onTick(MinecraftServer server){
      try{
         if(server.getTicks() % 20 == 0){
            PlayerManager playerManager = server.getPlayerManager();
            List<ServerPlayerEntity> players = playerManager.getPlayerList();
            for(ServerPlayerEntity player : players){
               IPlayerProfileComponent profile = PLAYER_DATA.get(player);
               profile.update();
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
