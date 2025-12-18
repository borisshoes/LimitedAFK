package net.borisshoes.limitedafk.callbacks;

import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.limitedafk.PlayerData;
import net.borisshoes.limitedafk.cca.DataFixer;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class PlayerConnectionCallback {
   
   public static void onPlayerJoin(ServerGamePacketListenerImpl netHandler, PacketSender packetSender, MinecraftServer server){
      ServerPlayer player = netHandler.player;
      DataFixer.onPlayerJoin(netHandler,packetSender,server);
      DataAccess.getPlayer(player.getUUID(), PlayerData.KEY).playerJoin(player);
   }
}
