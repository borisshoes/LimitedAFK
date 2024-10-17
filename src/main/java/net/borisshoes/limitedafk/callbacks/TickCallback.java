package net.borisshoes.limitedafk.callbacks;

import net.borisshoes.limitedafk.cca.IPlayerProfileComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.borisshoes.limitedafk.LimitedAFK.SERVER_TIMER_CALLBACKS;
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
      
      // Tick Timer Callbacks
      ArrayList<TickTimerCallback> toRemove = new ArrayList<>();
      for(TickTimerCallback t : SERVER_TIMER_CALLBACKS){
         if(t.decreaseTimer() == 0){
            t.onTimer();
            toRemove.add(t);
         }
      }
      SERVER_TIMER_CALLBACKS.removeIf(toRemove::contains);
   }
}
