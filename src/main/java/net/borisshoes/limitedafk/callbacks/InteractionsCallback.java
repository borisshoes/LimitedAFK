package net.borisshoes.limitedafk.callbacks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static net.borisshoes.limitedafk.cca.PlayerComponentInitializer.PLAYER_DATA;

public class InteractionsCallback {
   public static ActionResult useItem(PlayerEntity player, World world, Hand hand){
      PLAYER_DATA.get(player).updateActionTime("playerInteract",System.currentTimeMillis());
      return ActionResult.PASS;
   }
   
   public static ActionResult useEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      PLAYER_DATA.get(player).updateActionTime("playerInteract",System.currentTimeMillis());
      return ActionResult.PASS;
   }
   
   public static ActionResult useBlock(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult){
      PLAYER_DATA.get(player).updateActionTime("playerInteract",System.currentTimeMillis());
      return ActionResult.PASS;
   }
   
   public static ActionResult attackEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      PLAYER_DATA.get(player).updateActionTime("playerInteract",System.currentTimeMillis());
      return ActionResult.PASS;
   }
}
