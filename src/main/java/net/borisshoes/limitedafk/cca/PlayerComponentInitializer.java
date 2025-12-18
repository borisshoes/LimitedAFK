package net.borisshoes.limitedafk.cca;

import net.minecraft.resources.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class PlayerComponentInitializer implements EntityComponentInitializer {
   public static final ComponentKey<IPlayerProfileComponent> PLAYER_DATA = ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.fromNamespaceAndPath("limitedafk", "profile"), IPlayerProfileComponent.class);
   
   @Override
   public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
      registry.registerForPlayers(PLAYER_DATA, PlayerProfileComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
   }
}