package net.borisshoes.limitedafk.cca;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class PlayerComponentInitializer implements EntityComponentInitializer {
   public static final ComponentKey<IPlayerProfileComponent> PLAYER_DATA =
         ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("limitedafk", "profile"), IPlayerProfileComponent.class);
   
   @Override
   public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
      registry.registerForPlayers(PLAYER_DATA, PlayerProfileComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
   }
}