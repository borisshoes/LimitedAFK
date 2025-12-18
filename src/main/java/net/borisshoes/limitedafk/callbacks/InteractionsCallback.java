package net.borisshoes.limitedafk.callbacks;

import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.limitedafk.PlayerData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public class InteractionsCallback {
   public static InteractionResult useItem(Player player, Level world, InteractionHand hand){
      DataAccess.getPlayer(player.getUUID(),PlayerData.KEY).updateActionTime("playerInteract",System.currentTimeMillis());
      return InteractionResult.PASS;
   }
   
   public static InteractionResult useEntity(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      DataAccess.getPlayer(player.getUUID(),PlayerData.KEY).updateActionTime("playerInteract",System.currentTimeMillis());
      return InteractionResult.PASS;
   }
   
   public static InteractionResult useBlock(Player player, Level world, InteractionHand hand, BlockHitResult blockHitResult){
      DataAccess.getPlayer(player.getUUID(),PlayerData.KEY).updateActionTime("playerInteract",System.currentTimeMillis());
      return InteractionResult.PASS;
   }
   
   public static InteractionResult attackEntity(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      DataAccess.getPlayer(player.getUUID(),PlayerData.KEY).updateActionTime("playerInteract",System.currentTimeMillis());
      return InteractionResult.PASS;
   }
}
