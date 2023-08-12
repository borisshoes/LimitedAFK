package net.borisshoes.limitedafk.callbacks;

import com.mojang.brigadier.CommandDispatcher;
import net.borisshoes.limitedafk.LimitedAFK;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandRegisterCallback {
   public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
      
      dispatcher.register(literal("afklist").executes(LimitedAFK::listAfkCmd));
      
      dispatcher.register(literal("playtime")
            .executes(LimitedAFK::playtimeCmd)
            .then(argument("player",player()).requires(source -> source.hasPermissionLevel(2))
                  .executes(context -> LimitedAFK.playtimePlayerCmd(context,getPlayer(context,"player"))))
            .then(literal("all").requires(source -> source.hasPermissionLevel(2))
                  .executes(LimitedAFK::playtimeAllCmd))
            .then(literal("actions").requires(source -> source.hasPermissionLevel(2))
                  .then(argument("player",player()).requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> LimitedAFK.actionsPlayerCmd(context,getPlayer(context,"player")))))
      );
      
      dispatcher.register(LimitedAFK.config.generateCommand());
   }
}
