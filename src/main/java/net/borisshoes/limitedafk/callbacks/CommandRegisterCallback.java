package net.borisshoes.limitedafk.callbacks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.borisshoes.borislib.config.values.EnumConfigValue;
import net.borisshoes.limitedafk.LimitedAFK;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;

public class CommandRegisterCallback {
   
   public static CompletableFuture<Suggestions> getPlayerSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase(Locale.ROOT);
      Set<String> items = new HashSet<>(context.getSource().getServer().getPlayerList().getPlayers().stream().map(ServerPlayer::getScoreboardName).toList());
      items.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
      
      dispatcher.register(literal("afklist")
            .requires(Permissions.require(LimitedAFK.MOD_ID + ".afklist", PermissionLevel.ALL))
            .executes(LimitedAFK::listAfkCmd));
      
      dispatcher.register(literal("playtime")
            .requires(Permissions.require(LimitedAFK.MOD_ID + ".playtime", PermissionLevel.ALL))
            .executes(LimitedAFK::playtimeCmd)
            .then(argument("player", word()).suggests(CommandRegisterCallback::getPlayerSuggestions)
                  .requires(Permissions.require(LimitedAFK.MOD_ID + ".playtime.others", PermissionLevel.GAMEMASTERS))
                  .executes(context -> LimitedAFK.playtimePlayerCmd(context, getString(context, "player"))))
            .then(literal("all")
                  .requires(Permissions.require(LimitedAFK.MOD_ID + ".playtime.all", PermissionLevel.GAMEMASTERS))
                  .executes(LimitedAFK::playtimeAllCmd))
            .then(literal("actions")
                  .requires(Permissions.require(LimitedAFK.MOD_ID + ".playtime.actions", PermissionLevel.GAMEMASTERS))
                  .then(argument("player", player())
                        .executes(context -> LimitedAFK.actionsPlayerCmd(context, getPlayer(context, "player")))))
      );
      
      dispatcher.register(literal("afklevel")
            .then(literal("set")
                  .requires(Permissions.require(LimitedAFK.MOD_ID + ".afklevel.set", PermissionLevel.GAMEMASTERS))
                  .then(argument("player", word()).suggests(CommandRegisterCallback::getPlayerSuggestions)
                        .then(argument("level", string()).suggests((context, builder) -> EnumConfigValue.getEnumSuggestions(context, builder, LimitedAFK.AFKLevel.class))
                              .executes(context -> LimitedAFK.setAfkLevel(context, getString(context, "player"), EnumConfigValue.parseEnum(getString(context, "level"), LimitedAFK.AFKLevel.class))))))
            .then(literal("get")
                  .requires(Permissions.require(LimitedAFK.MOD_ID + ".afklevel.get", PermissionLevel.GAMEMASTERS))
                  .then(argument("player", word()).suggests(CommandRegisterCallback::getPlayerSuggestions)
                        .executes(context -> LimitedAFK.getAfkLevel(context, getString(context, "player")))))
            .then(literal("reset")
                  .requires(Permissions.require(LimitedAFK.MOD_ID + ".afklevel.reset", PermissionLevel.GAMEMASTERS))
                  .then(argument("player", word()).suggests(CommandRegisterCallback::getPlayerSuggestions)
                        .executes(context -> LimitedAFK.resetAfkLevel(context, getString(context, "player")))))
      );
      
      dispatcher.register(LimitedAFK.CONFIG.generateCommand("limitedafk", ""));
   }
}
