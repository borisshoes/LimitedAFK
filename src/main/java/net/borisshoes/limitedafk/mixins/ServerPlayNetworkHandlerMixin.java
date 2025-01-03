package net.borisshoes.limitedafk.mixins;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.borisshoes.limitedafk.cca.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
   // Actions:
   // Move (Check Regularly), Interact/Use (Use Fabric Events), Player Action, Player Input, Set Held Item
   
   @Inject(method = "onPlayerInput", at = @At("HEAD"))
   private void limitedafk_onPlayerInput(PlayerInputC2SPacket packet, CallbackInfo ci){
      ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
      ServerPlayerEntity player = handler.player;
      PLAYER_DATA.get(player).updateActionTime("playerInput",System.currentTimeMillis());
   }
   
   @Inject(method = "onPlayerAction", at = @At("HEAD"))
   private void limitedafk_onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci){
      ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
      ServerPlayerEntity player = handler.player;
      PLAYER_DATA.get(player).updateActionTime("playerAction",System.currentTimeMillis());
   }
   
   @Inject(method = "onUpdateSelectedSlot", at = @At("TAIL"))
   private void limitedafk_onChangeHeld(UpdateSelectedSlotC2SPacket packet, CallbackInfo ci){
      ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
      ServerPlayerEntity player = handler.player;
      PLAYER_DATA.get(player).updateActionTime("selectSlot",System.currentTimeMillis());
   }
   
}
