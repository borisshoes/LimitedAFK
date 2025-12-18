package net.borisshoes.limitedafk;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Lifecycle;
import net.borisshoes.borislib.config.ConfigManager;
import net.borisshoes.borislib.config.ConfigSetting;
import net.borisshoes.borislib.config.IConfigSetting;
import net.borisshoes.borislib.config.values.BooleanConfigValue;
import net.borisshoes.borislib.config.values.EnumConfigValue;
import net.borisshoes.borislib.config.values.IntConfigValue;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.limitedafk.callbacks.CommandRegisterCallback;
import net.borisshoes.limitedafk.callbacks.InteractionsCallback;
import net.borisshoes.limitedafk.callbacks.PlayerConnectionCallback;
import net.borisshoes.limitedafk.callbacks.TickCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.util.StringRepresentable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class LimitedAFK implements ModInitializer {
   
   public static final Logger LOGGER = LogManager.getLogger("Limited AFK");
   private static final String CONFIG_NAME = "LimitedAFK.properties";
   public static final String MOD_ID = "limitedafk";
   public static ConfigManager CONFIG;
   
   public static final Registry<IConfigSetting<?>> CONFIG_SETTINGS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"config_settings")), Lifecycle.stable());
   
   public static final IConfigSetting<?> ENABLED = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("enabled", true)));
   
   public static final IConfigSetting<?> ALLOWED_AFK_PERCENTAGE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("allowedAfkPercentage", 50, new IntConfigValue.IntLimits(0, 100))));
   
   public static final IConfigSetting<?> ANNOUNCE_AFK = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("announceAfk", true)));
   
   public static final IConfigSetting<?> AFK_TIMER = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("afkTimer", 900, new IntConfigValue.IntLimits(60))));
   
   public static final IConfigSetting<?> IGNORE_CREATIVE_AND_SPECTATOR = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("ignoreCreativeAndSpectator", true)));
   
   public static final IConfigSetting<?> DEFAULT_AFK_DETECTION_LEVEL = registerConfigSetting(new ConfigSetting<>(
         new EnumConfigValue<>("defaultAfkDetectionLevel", AFKLevel.LOW, AFKLevel.class)));
   
   public static final IConfigSetting<?> CAPTCHA_TIMER = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("captchaTimer", 600, new IntConfigValue.IntLimits(120))));
   
   private static IConfigSetting<?> registerConfigSetting(IConfigSetting<?> setting){
      Registry.register(CONFIG_SETTINGS, Identifier.fromNamespaceAndPath(MOD_ID,setting.getId()),setting);
      return setting;
   }
   
   @Override
   public void onInitialize(){
      ServerTickEvents.END_SERVER_TICK.register(TickCallback::onTick);
      CommandRegistrationCallback.EVENT.register(CommandRegisterCallback::registerCommands);
      ServerPlayConnectionEvents.JOIN.register(PlayerConnectionCallback::onPlayerJoin);
      UseItemCallback.EVENT.register(InteractionsCallback::useItem);
      UseEntityCallback.EVENT.register(InteractionsCallback::useEntity);
      UseBlockCallback.EVENT.register(InteractionsCallback::useBlock);
      AttackEntityCallback.EVENT.register(InteractionsCallback::attackEntity);
      
      CONFIG = new ConfigManager(MOD_ID,"Ancestral Archetypes",CONFIG_NAME,CONFIG_SETTINGS);
   }
   
   /**
    * Uses built in logger to log a message
    * @param level 0 - Info | 1 - Warn | 2 - Error | 3 - Fatal | Else - Debug
    * @param msg  The {@code String} to be printed.
    */
   public static void log(int level, String msg){
      switch(level){
         case 0 -> LOGGER.info(msg);
         case 1 -> LOGGER.warn(msg);
         case 2 -> LOGGER.error(msg);
         case 3 -> LOGGER.fatal(msg);
         default -> LOGGER.debug(msg);
      }
   }
   
   public static int listAfkCmd(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      MinecraftServer server = source.getServer();
      PlayerList playerManager = server.getPlayerList();
      List<ServerPlayer> players = playerManager.getPlayers();
      
      int afkCount = 0;
      
      source.sendSuccess(() -> Component.translatable("text.limitedafk.players_header"),false);
      for(ServerPlayer player : players){
         PlayerData profile = DataAccess.getPlayer(player.getUUID(),PlayerData.KEY);
         boolean afk = profile.isAfk();
         if(afk){
            afkCount++;
         }
         source.sendSuccess(() -> Component.translatable("text.limitedafk.player_status",
               player.getDisplayName(),
               Component.translatable(afk ? "text.limitedafk.status_afk" : "text.limitedafk.status_active").withStyle(afk ? ChatFormatting.RED : ChatFormatting.GREEN),
               timeToStr(System.currentTimeMillis() - profile.getStateChangeTime()).withStyle(ChatFormatting.GRAY)),false);
      }
      source.sendSuccess(() -> Component.translatable("text.limitedafk.players_footer"),false);
      return afkCount;
   }
   
   public static int playtimeCmd(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      if(!source.isPlayer()){
         source.sendFailure(Component.translatable("text.limitedafk.must_be_player").withStyle(ChatFormatting.RED));
      }
      
      ServerPlayer player = source.getPlayer();
      PlayerData profile = DataAccess.getPlayer(player.getUUID(),PlayerData.KEY);
      source.sendSuccess(() -> Component.translatable("text.limitedafk.playtime_total",
            player.getDisplayName(),
            timeToStr(profile.getTotalTime()).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.WHITE),false);
      source.sendSuccess(() -> Component.translatable("text.limitedafk.playtime_afk",
            player.getDisplayName(),
            timeToStr(profile.getAfkTime()).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.RED),false);
      source.sendSuccess(() -> Component.translatable("text.limitedafk.playtime_active",
            player.getDisplayName(),
            timeToStr(profile.getActiveTime()).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GREEN),false);
      source.sendSuccess(() -> Component.translatable("text.limitedafk.playtime_percentage",
            player.getDisplayName(),
            Component.literal(profile.getFormattedPercentage()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.DARK_AQUA),false);
      
      return 1;
   }
   
   public static int playtimeAllCmd(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      Map<UUID,PlayerData> allPlayers = DataAccess.allPlayerDataFor(PlayerData.KEY);
      
      log(0,"An Operator has initiated a playtime dump:");
      StringBuilder masterString = new StringBuilder("===== Full Playtime List =====");
      
      ArrayList<PlayerData> allPlaytime = new ArrayList<>(allPlayers.values());
      
      Collections.sort(allPlaytime);
      
      for(PlayerData profile : allPlaytime){
         if(profile == null){
            log(1,"An error occurred loading a null profile");
         }else{
            String str = "\n" + profile.getUsername() +
                  " has played for a total of [" +
                  timeToStr(profile.getTotalTime()).getString() +
                  "] - (" +
                  timeToStr(profile.getActiveTime()).getString() +
                  " Active | " +
                  timeToStr(profile.getAfkTime()).getString() +
                  " AFK) - <" +
                  profile.getFormattedPercentage() +
                  "%>";
            masterString.append(str);
         }
      }
      
      source.sendSuccess(() -> Component.translatable("text.limitedafk.click_to_copy").withStyle(s -> s.withClickEvent(new ClickEvent.CopyToClipboard(masterString.toString()))),false);
      log(0,masterString.toString());
      
      return allPlayers.size();
   }
   
   public static int playtimePlayerCmd(CommandContext<CommandSourceStack> context, String name){
      CommandSourceStack source = context.getSource();
      ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(name);
      PlayerData profile;
      if(player == null){
         profile = DataAccess.allPlayerDataFor(PlayerData.KEY).values().stream().filter(data -> data.getUsername().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))).findAny().orElse(null);
         if(profile == null){
            source.sendFailure(Component.translatable("text.limitedafk.no_player_found"));
            return 0;
         }
      }else{
         profile = DataAccess.getPlayer(player.getUUID(),PlayerData.KEY);
      }
      
      source.sendSuccess(() -> Component.translatable("text.limitedafk.playtime_total",
            profile.getUsername(),
            timeToStr(profile.getTotalTime()).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.WHITE),false);
      source.sendSuccess(() -> Component.translatable("text.limitedafk.playtime_afk",
            profile.getUsername(),
            timeToStr(profile.getAfkTime()).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.RED),false);
      source.sendSuccess(() -> Component.translatable("text.limitedafk.playtime_active",
            profile.getUsername(),
            timeToStr(profile.getActiveTime()).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GREEN),false);
      source.sendSuccess(() -> Component.translatable("text.limitedafk.playtime_percentage",
            profile.getUsername(),
            Component.literal(profile.getFormattedPercentage()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.DARK_AQUA),false);
      
      return 1;
   }
   
   public static int actionsPlayerCmd(CommandContext<CommandSourceStack> context, ServerPlayer player){
      CommandSourceStack source = context.getSource();
      PlayerData profile = DataAccess.getPlayer(player.getUUID(),PlayerData.KEY);
      long curTime = System.currentTimeMillis();
      
      source.sendSuccess(() -> Component.translatable("text.limitedafk.player_actions_header",
            player.getDisplayName()),false);
      for(Map.Entry<String, Long> entry : profile.getLastActionTimes().entrySet()){
         source.sendSuccess(() -> Component.translatable("text.limitedafk.player_action_entry",
               Component.literal(entry.getKey()).withStyle(ChatFormatting.YELLOW),
               timeToStr(curTime-entry.getValue()).withStyle(ChatFormatting.GRAY)), false);
      }
      
      return 1;
   }
   
   public static MutableComponent timeToStr(long millis){
      long time = millis / 1000;
      if(time <= 0) return Component.literal("0 ").append(Component.translatable("text.limitedafk.seconds"));
      long subtract = time;
      long daysDif = subtract / 86400;
      subtract -= daysDif * 86400;
      long hoursDif = subtract / 3600;
      subtract -= hoursDif * 3600;
      long minutesDif = subtract / 60;
      subtract -= minutesDif * 60;
      long secondsDiff = subtract;
      
      MutableComponent text = Component.literal("");
      boolean needSpace = false;
      if(daysDif > 0){
         text.append(Component.literal(daysDif+" "));
         text.append(Component.translatable("text.limitedafk.days"));
         needSpace = true;
      }
      if(hoursDif > 0){
         if(needSpace) text.append(Component.literal(" "));
         text.append(Component.literal(hoursDif+" "));
         text.append(Component.translatable("text.limitedafk.hours"));
         needSpace = true;
      }
      if(minutesDif > 0){
         if(needSpace) text.append(Component.literal(" "));
         text.append(Component.literal(minutesDif+" "));
         text.append(Component.translatable("text.limitedafk.minutes"));
         needSpace = true;
      }
      if(secondsDiff > 0){
         if(needSpace) text.append(Component.literal(" "));
         text.append(Component.literal(secondsDiff+" "));
         text.append(Component.translatable("text.limitedafk.seconds"));
      }
      return text;
   }
   
   public static int setAfkLevel(CommandContext<CommandSourceStack> context, String name, AFKLevel level){
      if(level == null){
         context.getSource().sendFailure(Component.translatable("text.limitedafk.invalid_afk_level"));
         return -1;
      }
      ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(name);
      PlayerData profile;
      if(player == null){
         profile = DataAccess.allPlayerDataFor(PlayerData.KEY).values().stream().filter(data -> data.getUsername().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))).findAny().orElse(null);
         if(profile == null){
            context.getSource().sendFailure(Component.translatable("text.limitedafk.no_player_found"));
            return 0;
         }
      }else{
         profile = DataAccess.getPlayer(player.getUUID(),PlayerData.KEY);
      }
      
      profile.setAfkLevel(level);
      context.getSource().sendSuccess(() -> Component.translatable("text.limitedafk.afk_level_set",
            profile.getUsername(),
            Component.literal(profile.getAfkLevel().getSerializedName()).withStyle(ChatFormatting.AQUA)),false);
      return 1;
   }
   
   public static int getAfkLevel(CommandContext<CommandSourceStack> context, String name){
      ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(name);
      PlayerData profile;
      if(player == null){
         profile = DataAccess.allPlayerDataFor(PlayerData.KEY).values().stream().filter(data -> data.getUsername().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))).findAny().orElse(null);
         if(profile == null){
            context.getSource().sendFailure(Component.translatable("text.limitedafk.no_player_found"));
            return 0;
         }
      }else{
         profile = DataAccess.getPlayer(player.getUUID(),PlayerData.KEY);
      }
      
      context.getSource().sendSuccess(() -> Component.translatable("text.limitedafk.afk_level_get",
            profile.getUsername(),
            Component.literal(profile.getAfkLevel().getSerializedName()).withStyle(ChatFormatting.AQUA)),false);
      return 1;
   }
   
   public static int resetAfkLevel(CommandContext<CommandSourceStack> context, String name){
      ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(name);
      PlayerData profile;
      if(player == null){
         profile = DataAccess.allPlayerDataFor(PlayerData.KEY).values().stream().filter(data -> data.getUsername().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))).findAny().orElse(null);
         if(profile == null){
            context.getSource().sendFailure(Component.translatable("text.limitedafk.no_player_found"));
            return 0;
         }
      }else{
         profile = DataAccess.getPlayer(player.getUUID(),PlayerData.KEY);
      }
      
      profile.resetLevel();
      context.getSource().sendSuccess(() -> Component.translatable("text.limitedafk.afk_level_reset",profile.getUsername()),false);
      return 1;
   }
   
   public enum AFKLevel implements StringRepresentable {
      LOW("LOW"),
      MEDIUM("MEDIUM"),
      HIGH("HIGH");
      
      private final String id;
      
      AFKLevel(String id){
         this.id = id;
      }
      
      @Override
      public String getSerializedName(){
         return this.id;
      }
   }
}
