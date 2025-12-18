package net.borisshoes.limitedafk.cca;

import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.limitedafk.PlayerData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import static net.borisshoes.limitedafk.cca.PlayerComponentInitializer.PLAYER_DATA;

public class DataFixer {
   public static void onPlayerJoin(ServerGamePacketListenerImpl handler, PacketSender packetSender, MinecraftServer server){
      ServerPlayer player = handler.getPlayer();
      IPlayerProfileComponent oldData = PLAYER_DATA.get(player);
      
      if(oldData.getTotalTime() > 0){
         PlayerData playerData = DataAccess.getPlayer(player.getUUID(), PlayerData.KEY);
         playerData.copyFromOldData(
               oldData.getTotalTime(),
               oldData.getActiveTime(),
               oldData.getAfkTime(),
               oldData.getLastUpdate(),
               oldData.getStateChangeTime(),
               oldData.getLastCaptcha(),
               oldData.getLastTitlePulse(),
               oldData.isAfk(),
               oldData.isLevelOverridden(),
               oldData.getOverrideLevel(),
               oldData.getLastActionTimes()
         );
         oldData.clear();
      }
   }
}
