package net.borisshoes.limitedafk.callbacks;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.borisshoes.limitedafk.cca.PlayerComponentInitializer.PLAYER_DATA;

public class PlayerConnectionCallback {
   
   public static void onPlayerJoin(ServerPlayNetworkHandler netHandler, PacketSender packetSender, MinecraftServer server){
      ServerPlayerEntity player = netHandler.player;
      PLAYER_DATA.get(player).playerJoin();
   }
}
