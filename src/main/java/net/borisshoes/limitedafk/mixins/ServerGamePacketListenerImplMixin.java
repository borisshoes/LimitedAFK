package net.borisshoes.limitedafk.mixins;

import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.limitedafk.PlayerData;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
   // Actions:
   // Move (Check Regularly), Interact/Use (Use Fabric Events), Player Action, Player Input, Set Held Item
   
   @Inject(method = "handlePlayerInput", at = @At("HEAD"))
   private void limitedafk_onPlayerInput(ServerboundPlayerInputPacket packet, CallbackInfo ci){
      ServerGamePacketListenerImpl handler = (ServerGamePacketListenerImpl) (Object) this;
      ServerPlayer player = handler.player;
      DataAccess.getPlayer(player.getUUID(), PlayerData.KEY).updateActionTime("playerInput",System.currentTimeMillis());
   }
   
   @Inject(method = "handlePlayerAction", at = @At("HEAD"))
   private void limitedafk_onPlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci){
      ServerGamePacketListenerImpl handler = (ServerGamePacketListenerImpl) (Object) this;
      ServerPlayer player = handler.player;
      DataAccess.getPlayer(player.getUUID(),PlayerData.KEY).updateActionTime("playerAction",System.currentTimeMillis());
   }
   
   @Inject(method = "handleSetCarriedItem", at = @At("TAIL"))
   private void limitedafk_onChangeHeld(ServerboundSetCarriedItemPacket packet, CallbackInfo ci){
      ServerGamePacketListenerImpl handler = (ServerGamePacketListenerImpl) (Object) this;
      ServerPlayer player = handler.player;
      DataAccess.getPlayer(player.getUUID(),PlayerData.KEY).updateActionTime("selectSlot",System.currentTimeMillis());
   }
   
}
