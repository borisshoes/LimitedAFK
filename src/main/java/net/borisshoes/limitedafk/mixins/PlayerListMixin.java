package net.borisshoes.limitedafk.mixins;

import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.limitedafk.PlayerData;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
   
   // Called when a player sends a chat message
   @Inject(at = @At("HEAD"), method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V")
   private void broadcast(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params, CallbackInfo ci) {
      DataAccess.getPlayer(sender.getUUID(), PlayerData.KEY).updateActionTime("chatMessage",System.currentTimeMillis());
   }
}
