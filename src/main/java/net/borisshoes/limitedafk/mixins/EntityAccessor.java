package net.borisshoes.limitedafk.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
   
   @Invoker
   public void callSetWorld(World world);
}