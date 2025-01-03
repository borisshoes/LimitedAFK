package net.borisshoes.limitedafk;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import net.borisshoes.limitedafk.callbacks.*;
import net.borisshoes.limitedafk.cca.IPlayerProfileComponent;
import net.borisshoes.limitedafk.utils.ConfigUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.UserCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.*;

import static net.borisshoes.limitedafk.cca.PlayerComponentInitializer.PLAYER_DATA;

public class LimitedAFK implements ModInitializer {
   
   private static final Logger logger = LogManager.getLogger("Limited AFK");
   private static final String CONFIG_NAME = "LimitedAFK.properties";
   public static ConfigUtils config;
   
   public static final ArrayList<TickTimerCallback> SERVER_TIMER_CALLBACKS = new ArrayList<>();
   
   @Override
   public void onInitialize(){
      ServerTickEvents.END_SERVER_TICK.register(TickCallback::onTick);
      CommandRegistrationCallback.EVENT.register(CommandRegisterCallback::registerCommands);
      ServerPlayConnectionEvents.JOIN.register(PlayerConnectionCallback::onPlayerJoin);
      UseItemCallback.EVENT.register(InteractionsCallback::useItem);
      UseEntityCallback.EVENT.register(InteractionsCallback::useEntity);
      UseBlockCallback.EVENT.register(InteractionsCallback::useBlock);
      AttackEntityCallback.EVENT.register(InteractionsCallback::attackEntity);
      
      config = new ConfigUtils(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME).toFile(), logger, Arrays.asList(new ConfigUtils.IConfigValue[] {
            new ConfigUtils.BooleanConfigValue("enabled", true, "Is Limited AFK enabled",
                  new ConfigUtils.Command("Limiting AFK: %s", "Limiting AFK is now %s")),
            new ConfigUtils.IntegerConfigValue("allowedAfkPercentage", 50, new ConfigUtils.IntegerConfigValue.IntLimits(0,100),"How long can people AFK",
                  new ConfigUtils.Command("Permitted Time AFK'ing is %s percent", "Permitted Time AFK'ing is now %s percent")),
            new ConfigUtils.BooleanConfigValue("announceAfk", true,"Does Limited AFK announce when people go AFK",
                  new ConfigUtils.Command("Announcing AFK is %s", "Announcing AFK is now %s")),
            new ConfigUtils.IntegerConfigValue("afkTimer", 900, new ConfigUtils.IntegerConfigValue.IntLimits(60), "How long until someone is AFK",
                  new ConfigUtils.Command("Time till AFK is %s seconds", "Time till AFK is now %s seconds")),
            new ConfigUtils.BooleanConfigValue("ignoreCreativeAndSpectator", true, "Does Limited AFK track creative and spectator players",
                  new ConfigUtils.Command("Ignoring Creative and Spectators: %s", "Ignoring Creative and Spectators is now: %s")),
            new ConfigUtils.EnumConfigValue<>("defaultAfkDetectionLevel", AFKLevel.LOW, "How aggressive is the AFK detection (LOW and MEDIUM require various levels of activity, and HIGH requires a captcha)",
                  new ConfigUtils.Command("Default AFK detection level: %s", "Default AFK detection level is now: %s"), AFKLevel.class),
            new ConfigUtils.IntegerConfigValue("captchaTimer", 600, new ConfigUtils.IntegerConfigValue.IntLimits(120), "Interval between when someone suspected of being AFK is given a captcha",
                  new ConfigUtils.Command("The captcha timer is %s seconds", "The captcha timer is now %s seconds")),
      }));
   }
   
   
   
   /**
    * Uses built in logger to log a message
    * @param level 0 - Info | 1 - Warn | 2 - Error | 3 - Fatal | Else - Debug
    * @param msg  The {@code String} to be printed.
    */
   public static void log(int level, String msg){
      switch(level){
         case 0 -> logger.info(msg);
         case 1 -> logger.warn(msg);
         case 2 -> logger.error(msg);
         case 3 -> logger.fatal(msg);
         default -> logger.debug(msg);
      }
   }
   
   public static int listAfkCmd(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      MinecraftServer server = source.getServer();
      PlayerManager playerManager = server.getPlayerManager();
      List<ServerPlayerEntity> players = playerManager.getPlayerList();
      
      int afkCount = 0;
      
      source.sendFeedback(() -> Text.literal("===== Players ====="),false);
      for(ServerPlayerEntity player : players){
         IPlayerProfileComponent profile = PLAYER_DATA.get(player);
         boolean afk = profile.isAfk();
         if(afk){
            afkCount++;
         }
         source.sendFeedback(() -> Text.literal("")
               .append(player.getDisplayName())
               .append(Text.literal(" - ").formatted(Formatting.WHITE))
               .append(Text.literal(afk ? "AFK" : "Active").formatted(afk ? Formatting.RED : Formatting.GREEN))
               .append(Text.literal(" for [").formatted(Formatting.WHITE))
               .append(Text.literal(timeToStr(System.currentTimeMillis() - profile.getStateChangeTime())).formatted(Formatting.GRAY))
               .append(Text.literal("]").formatted(Formatting.WHITE)),false);
      }
      source.sendFeedback(() -> Text.literal("==================="),false);
      return afkCount;
   }
   
   public static int playtimeCmd(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      if(!source.isExecutedByPlayer()){
         source.sendError(Text.literal("Command must be executed by a player").formatted(Formatting.RED));
      }
      DecimalFormat df = new DecimalFormat( "#.00" );
      
      ServerPlayerEntity player = source.getPlayer();
      IPlayerProfileComponent profile = PLAYER_DATA.get(player);
      source.sendFeedback(() -> Text.literal("")
            .append(player.getDisplayName())
            .append(Text.literal(" has played for [").formatted(Formatting.WHITE))
            .append(Text.literal(timeToStr(profile.getTotalTime())).formatted(Formatting.GRAY))
            .append(Text.literal("]").formatted(Formatting.WHITE)),false);
      source.sendFeedback(() -> Text.literal("")
            .append(player.getDisplayName())
            .append(Text.literal(" has AFK'd a total of [").formatted(Formatting.RED))
            .append(Text.literal(timeToStr(profile.getAfkTime())).formatted(Formatting.GRAY))
            .append(Text.literal("]").formatted(Formatting.RED)),false);
      source.sendFeedback(() -> Text.literal("")
            .append(player.getDisplayName())
            .append(Text.literal(" has been Active for a total of [").formatted(Formatting.GREEN))
            .append(Text.literal(timeToStr(profile.getActiveTime())).formatted(Formatting.GRAY))
            .append(Text.literal("]").formatted(Formatting.GREEN)),false);
      source.sendFeedback(() -> Text.literal("")
            .append(player.getDisplayName())
            .append(Text.literal(" has an AFK percentage of ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(df.format(100L*profile.getAfkTime()/(profile.getTotalTime()+1))).formatted(Formatting.AQUA))
            .append(Text.literal("%").formatted(Formatting.AQUA)),false);
      
      return 1;
   }
   
   public static int playtimeAllCmd(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      MinecraftServer server = source.getServer();
      PlayerManager playerManager = server.getPlayerManager();
      UserCache userCache = server.getUserCache();
      List<ServerPlayerEntity> allPlayers = new ArrayList<>();
      List<UserCache.Entry> cacheEntries = userCache.load();
      
      for(UserCache.Entry cacheEntry : cacheEntries){
         GameProfile reqProfile = cacheEntry.getProfile();
         ServerPlayerEntity reqPlayer = playerManager.getPlayer(reqProfile.getName());
         
         if(reqPlayer == null){ // Player Offline
            reqPlayer = playerManager.createPlayer(reqProfile, SyncedClientOptions.createDefault());
            server.getPlayerManager().loadPlayerData(reqPlayer);
         }
         allPlayers.add(reqPlayer);
      }
      
      DecimalFormat df = new DecimalFormat("#.00");
      
      log(0,"An Operator has initiated a playtime dump:");
      StringBuilder masterString = new StringBuilder("===== Full Playtime List =====");
      
      ArrayList<IPlayerProfileComponent> allPlaytime = new ArrayList<>();
      
      for(ServerPlayerEntity player : allPlayers){
         IPlayerProfileComponent profile = PLAYER_DATA.get(player);
         allPlaytime.add(profile);
      }
      
      Collections.sort(allPlaytime);
      
      for(IPlayerProfileComponent profile : allPlaytime){
         if(profile == null){
            log(1,"An error occurred loading a null profile");
         }else{
            String str = "\n" + profile.getPlayer().getNameForScoreboard() + " has played for a total of [" + timeToStr(profile.getTotalTime()) + "] - (" + timeToStr(profile.getActiveTime()) + " Active | " + timeToStr(profile.getAfkTime()) + " AFK) - <" + df.format(100L * profile.getAfkTime() / (profile.getTotalTime()+1)) + "%>";
            masterString.append(str);
         }
      }
      
      source.sendFeedback(() -> Text.literal("Click to copy full dump").styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, masterString.toString()))),false);
      log(0,masterString.toString());
      
      return allPlayers.size();
   }
   
   public static int playtimePlayerCmd(CommandContext<ServerCommandSource> context, ServerPlayerEntity player){
      ServerCommandSource source = context.getSource();
      DecimalFormat df = new DecimalFormat( "#.00" );
      
      IPlayerProfileComponent profile = PLAYER_DATA.get(player);
      source.sendFeedback(() -> Text.literal("")
            .append(player.getDisplayName())
            .append(Text.literal(" has played for [").formatted(Formatting.WHITE))
            .append(Text.literal(timeToStr(profile.getTotalTime())).formatted(Formatting.GRAY))
            .append(Text.literal("]").formatted(Formatting.WHITE)),false);
      source.sendFeedback(() -> Text.literal("")
            .append(player.getDisplayName())
            .append(Text.literal(" has AFK'd a total of [").formatted(Formatting.RED))
            .append(Text.literal(timeToStr(profile.getAfkTime())).formatted(Formatting.GRAY))
            .append(Text.literal("]").formatted(Formatting.RED)),false);
      source.sendFeedback(() -> Text.literal("")
            .append(player.getDisplayName())
            .append(Text.literal(" has been Active for a total of [").formatted(Formatting.GREEN))
            .append(Text.literal(timeToStr(profile.getActiveTime())).formatted(Formatting.GRAY))
            .append(Text.literal("]").formatted(Formatting.GREEN)),false);
      source.sendFeedback(() -> Text.literal("")
            .append(player.getDisplayName())
            .append(Text.literal(" has an AFK percentage of ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(df.format(100L*profile.getAfkTime()/(profile.getTotalTime()+1))).formatted(Formatting.AQUA))
            .append(Text.literal("%").formatted(Formatting.AQUA)),false);
      
      return 1;
   }
   
   public static int actionsPlayerCmd(CommandContext<ServerCommandSource> context, ServerPlayerEntity player){
      ServerCommandSource source = context.getSource();
      IPlayerProfileComponent profile = PLAYER_DATA.get(player);
      long curTime = System.currentTimeMillis();
      
      source.sendFeedback(() -> Text.literal("")
            .append(Text.literal("Player Actions for: "))
            .append(player.getDisplayName()),false);
      for(Map.Entry<String, Long> entry : profile.getLastActionTimes().entrySet()){
         source.sendFeedback(() -> Text.literal(" - "+entry.getKey()+": "+timeToStr(curTime-entry.getValue())), false);
      }
      
      return 1;
   }
   
   public static String timeToStr(long millis){
      long time = millis / 1000;
      if(time <= 0) return "0 Seconds";
      long subtract = time;
      long hoursDif = subtract / 3600;
      subtract -= hoursDif * 3600;
      long minutesDif = subtract / 60;
      subtract -= minutesDif * 60;
      long secondsDiff = subtract;
      
      String diff = "";
      if(hoursDif > 0 ) diff += hoursDif+" Hours ";
      if(minutesDif > 0 ) diff += minutesDif+" Minutes ";
      if(secondsDiff > 0 ) diff += secondsDiff+" Seconds ";
      diff = diff.substring(0,diff.length()-1);
      
      return diff;
   }
   
   public static int setAfkLevel(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, AFKLevel level){
      if(level == null){
         context.getSource().sendError(Text.literal("Invalid AFK Level"));
         return -1;
      }
      PLAYER_DATA.get(player).setAfkLevel(level);
      context.getSource().sendFeedback(() -> Text.literal(player.getNameForScoreboard()+"'s AFK Level is now "+PLAYER_DATA.get(player).getAfkLevel().asString()),false);
      return 1;
   }
   
   public static int getAfkLevel(CommandContext<ServerCommandSource> context, ServerPlayerEntity player){
      context.getSource().sendFeedback(() -> Text.literal(player.getNameForScoreboard()+"'s AFK Level is "+PLAYER_DATA.get(player).getAfkLevel().asString()),false);
      return 1;
   }
   
   public static int resetAfkLevel(CommandContext<ServerCommandSource> context, ServerPlayerEntity player){
      PLAYER_DATA.get(player).resetLevel();
      context.getSource().sendFeedback(() -> Text.literal("Reset "+player.getNameForScoreboard()+"'s AFK Level"),false);
      return 1;
   }
   
   public static boolean addTickTimerCallback(TickTimerCallback callback){
      return SERVER_TIMER_CALLBACKS.add(callback);
   }
   
   
   public enum AFKLevel implements StringIdentifiable {
      LOW("LOW"),
      MEDIUM("MEDIUM"),
      HIGH("HIGH");
      
      private final String id;
      
      AFKLevel(String id){
         this.id = id;
      }
      
      @Override
      public String asString(){
         return this.id;
      }
   }
}
