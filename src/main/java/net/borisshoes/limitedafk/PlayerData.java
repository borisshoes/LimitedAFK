package net.borisshoes.limitedafk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.CodecUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.borisshoes.limitedafk.gui.CaptchaGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.text.DecimalFormat;
import java.util.*;

import static net.borisshoes.limitedafk.LimitedAFK.CONFIG;
import static net.borisshoes.limitedafk.LimitedAFK.MOD_ID;

public class PlayerData implements Comparable<PlayerData>{
   
   public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         CodecUtils.UUID_CODEC.fieldOf("playerID").forGetter(data -> data.playerID),
         Codec.STRING.optionalFieldOf("username", "").forGetter(data -> data.username),
         Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("lastActionTimes").forGetter(data -> data.lastActionTimes),
         Vec3.CODEC.listOf().fieldOf("moves").forGetter(data -> new ArrayList<>(data.moves)),
         Vec2.CODEC.listOf().fieldOf("looks").forGetter(data -> new ArrayList<>(data.looks)),
         Codec.LONG.fieldOf("totalTime").forGetter(data -> data.totalTime),
         Codec.LONG.fieldOf("activeTime").forGetter(data -> data.activeTime),
         Codec.LONG.fieldOf("afkTime").forGetter(data -> data.afkTime),
         Codec.LONG.fieldOf("lastUpdate").forGetter(data -> data.lastUpdate),
         Codec.LONG.fieldOf("stateChangeTime").forGetter(data -> data.stateChangeTime),
         Codec.LONG.fieldOf("lastCaptcha").forGetter(data -> data.lastCaptcha),
         Codec.LONG.fieldOf("lastTitlePulse").forGetter(data -> data.lastTitlePulse),
         Codec.BOOL.fieldOf("afk").forGetter(data -> data.afk),
         Codec.STRING.optionalFieldOf("overrideLevel").forGetter(data -> data.overrideLevel != null ? Optional.of(data.overrideLevel.getSerializedName()) : Optional.empty()),
         Codec.BOOL.fieldOf("levelOverridden").forGetter(data -> data.levelOverridden)
   ).apply(instance, PlayerData::fromCodec));
   
   private static PlayerData fromCodec(UUID playerID, String username, Map<String, Long> lastActionTimes, List<Vec3> moves, List<Vec2> looks, long totalTime, long activeTime, long afkTime, long lastUpdate, long stateChangeTime, long lastCaptcha, long lastTitlePulse, boolean afk, Optional<String> overrideLevel, boolean levelOverridden) {
      PlayerData data = new PlayerData(playerID);
      data.setUsername(username);
      data.lastActionTimes.putAll(lastActionTimes);
      data.moves.addAll(moves);
      data.looks.addAll(looks);
      data.totalTime = totalTime;
      data.activeTime = activeTime;
      data.afkTime = afkTime;
      data.lastUpdate = lastUpdate;
      data.stateChangeTime = stateChangeTime;
      data.lastCaptcha = lastCaptcha;
      data.lastTitlePulse = lastTitlePulse;
      data.afk = afk;
      data.levelOverridden = levelOverridden;
      data.overrideLevel = overrideLevel.map(LimitedAFK.AFKLevel::valueOf).orElse((LimitedAFK.AFKLevel) CONFIG.getValue(LimitedAFK.DEFAULT_AFK_DETECTION_LEVEL.getName()));
      return data;
   }
   
   public static final DataKey<PlayerData> KEY = DataRegistry.register(DataKey.ofPlayer(Identifier.fromNamespaceAndPath(MOD_ID, "playerdata"), CODEC,PlayerData::new));
   
   private final UUID playerID;
   private String username = "";
   private final HashMap<String, Long> lastActionTimes = new HashMap<>();
   private final LinkedList<Vec3> moves = new LinkedList<>();
   private final LinkedList<Vec2> looks = new LinkedList<>();
   private long totalTime, activeTime, afkTime, lastUpdate, stateChangeTime, lastCaptcha, lastTitlePulse;
   private boolean afk;
   private LimitedAFK.AFKLevel overrideLevel;
   private boolean levelOverridden;
   
   // Actions:
   // Move, Player Action, Player Input, Set Held Item, Interact/Use
   
   public PlayerData(UUID playerID){
      this.playerID = playerID;
   }
   
   public String getUsername(){
      return username;
   }
   
   private void setUsername(String username){
      this.username = username;
   }
   
   public long getTotalTime(){
      return totalTime;
   }
   
   
   public long getActiveTime(){
      return activeTime;
   }
   
   
   public long getAfkTime(){
      return afkTime;
   }
   
   public double getPlaytimePercentage(){
      return (double)getAfkTime() / (double)(getTotalTime()+1);
   }
   
   public String getFormattedPercentage(){
      return TextUtils.readableDouble(100*getPlaytimePercentage());
   }
   
   public HashMap<String, Long> getLastActionTimes(){
      return lastActionTimes;
   }
   
   
   public long getLastActionTime(String action){
      return lastActionTimes.getOrDefault(action, -1L);
   }
   
   public void playerJoin(ServerPlayer player){
      long curTime = System.currentTimeMillis();
      updateActionTime("playerLook", curTime);
      updateActionTime("playerMove", curTime);
      updateActionTime("playerInteract", curTime);
      updateActionTime("playerAction", curTime);
      updateActionTime("selectSlot", curTime);
      updateActionTime("chatMessage", curTime);
      lastUpdate = curTime;
      stateChangeTime = curTime;
      afk = false;
      username = player.getScoreboardName();
   }
   
   private void checkMoveAndLook(ServerPlayer player){
      long curTime = System.currentTimeMillis();
      // Add New Elements and Remove Old
      moves.add(player.position());
      while(moves.size() > 10){
         moves.remove();
      }
      looks.add(player.getRotationVector());
      while(looks.size() > 5){
         looks.remove();
      }
      
      // See if the player has moved more than 10 blocks in the past 10 seconds
      Vec3 travelled = new Vec3(0, 0, 0);
      for(int i = 0; i < moves.size() - 1; i++){
         Vec3 move1 = moves.get(i);
         Vec3 move2 = moves.get(i + 1);
         travelled = travelled.add(move2.subtract(move1));
      }
      if(travelled.length() > 10){
         updateActionTime("playerMove", curTime);
      }
      
      // See if the player has at least 3 unique looks in the past 5 seconds
      List<Vec2> uniqueLooks = new ArrayList<>();
      for(Vec2 look : looks){
         boolean found = false;
         for(Vec2 uniqueLook : uniqueLooks){
            if(look.x == uniqueLook.x && look.y == uniqueLook.y){
               found = true;
               break;
            }
         }
         if(!found){
            uniqueLooks.add(look);
         }
      }
      if(uniqueLooks.size() >= 3){
         updateActionTime("playerLook", curTime);
      }
   }
   
   public void update(ServerPlayer player){
      if(!player.getUUID().equals(playerID)) return;
      if(username.isBlank()){
         username = player.getScoreboardName();
      }
      if(totalTime == 0){
         totalTime++;
      }
      checkMoveAndLook(player);
      
      long curTime = System.currentTimeMillis();
      long millis = curTime - lastUpdate;
      long afkTimer = 1000L * CONFIG.getInt(LimitedAFK.AFK_TIMER);
      
      int afkCount = 0;
      boolean lookAfk = curTime - getLastActionTime("playerLook") < afkTimer && getLastActionTime("playerLook") > 0; // Look
      boolean moveAfk = curTime - getLastActionTime("playerMove") < afkTimer && getLastActionTime("playerMove") > 0; // Move
      boolean interactAfk = curTime - getLastActionTime("playerInteract") < afkTimer && getLastActionTime("playerInteract") > 0; // Right click
      boolean actionAfk = curTime - getLastActionTime("playerAction") < afkTimer && getLastActionTime("playerAction") > 0; // Mining / Dropping Item
      boolean selectAfk = curTime - getLastActionTime("selectSlot") < afkTimer && getLastActionTime("selectSlot") > 0; // Change Equipment
      boolean chatAfk = curTime - getLastActionTime("chatMessage") < afkTimer && getLastActionTime("chatMessage") > 0; // Sent Message
      afkCount += lookAfk ? 10 : 0;
      afkCount += moveAfk ? 4 : 0;
      afkCount += selectAfk ? 3 : 0;
      afkCount += interactAfk ? 1 : 0;
      afkCount += actionAfk ? 1 : 0;
      afkCount += chatAfk ? 1 : 0;
      
      boolean curAfk = afkCount < (getAfkLevel() == LimitedAFK.AFKLevel.LOW ? 15 : 17);
      
      if((player.isCreative() || player.isSpectator()) && CONFIG.getBoolean(LimitedAFK.IGNORE_CREATIVE_AND_SPECTATOR)){
         curAfk = false;
      }else{
         if(getAfkLevel() == LimitedAFK.AFKLevel.HIGH && afkCount < 20 && player instanceof ServerPlayer serverPlayer && (curTime - lastCaptcha) > 1000L * CONFIG.getInt(LimitedAFK.CAPTCHA_TIMER)){
            lastCaptcha = curTime;
            CaptchaGui gui = new CaptchaGui(serverPlayer);
            gui.build();
            gui.open();
         }
      }
      
      if(curAfk && player instanceof ServerPlayer serverPlayer && (curTime - lastTitlePulse) > 1000L * 30){
         serverPlayer.connection.send(new ClientboundClearTitlesPacket(true));
         BorisLib.addTickTimerCallback(new GenericTimer(5, () -> serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 600, 10))));
         BorisLib.addTickTimerCallback(new GenericTimer(10, () -> serverPlayer.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("text.limitedafk.you_are_afk").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)))));
         lastTitlePulse = curTime;
      }
      
      if(curAfk && !afk){ // Switching to AFK
         stateChangeTime = curTime;
         
         if(CONFIG.getBoolean(LimitedAFK.ANNOUNCE_AFK)){
            player.level().getServer().getPlayerList().broadcastSystemMessage(
                  Component.translatable("text.limitedafk.player_now_afk",
                        player.getDisplayName()).withStyle(ChatFormatting.WHITE), false);
         }
      }
      if(!curAfk && afk){ // Switching to Active
         stateChangeTime = curTime;
         
         if(CONFIG.getBoolean(LimitedAFK.ANNOUNCE_AFK)){
            player.level().getServer().getPlayerList().broadcastSystemMessage(
                  Component.translatable("text.limitedafk.player_no_longer_afk",
                        player.getDisplayName()).withStyle(ChatFormatting.WHITE), false);
         }
         
         if(player instanceof ServerPlayer serverPlayer){
            serverPlayer.connection.send(new ClientboundClearTitlesPacket(true));
         }
      }
      
      afk = curAfk;
      if(afk){
         afkTime += millis;
         
if(CONFIG.getBoolean(LimitedAFK.ENABLED) && 100 * afkTime / totalTime >= CONFIG.getInt(LimitedAFK.ALLOWED_AFK_PERCENTAGE)){
            if(player instanceof ServerPlayer serverPlayer){
               serverPlayer.connection.disconnect(Component.translatable("text.limitedafk.too_much_afk"));
            }
         }
      }else{
         activeTime += millis;
      }
      totalTime += millis;
      lastUpdate = curTime;
   }
   
   public boolean updateActionTime(String action, long time){
      boolean has = lastActionTimes.containsKey(action);
      lastActionTimes.put(action, time);
      return has;
   }
   
   public boolean isAfk(){
      return afk;
   }
   
   public void setAfk(boolean afk){
      this.afk = afk;
   }
   
   public long getStateChangeTime(){
      return stateChangeTime;
   }
   
   public LimitedAFK.AFKLevel getAfkLevel(){
      return this.levelOverridden ? this.overrideLevel : (LimitedAFK.AFKLevel) (CONFIG.getValue(LimitedAFK.DEFAULT_AFK_DETECTION_LEVEL.getName()));
   }
   
   public void setAfkLevel(LimitedAFK.AFKLevel level){
      this.levelOverridden = true;
      this.overrideLevel = level;
   }
   
   public void resetLevel(){
      this.levelOverridden = false;
   }
   
   public void captchaFail(){
      long curTime = System.currentTimeMillis();
      long afkTimeAgo = curTime - 1000L * CONFIG.getInt(LimitedAFK.AFK_TIMER);
      
      // Set player as AFK
      if(getLastActionTime("playerLook") > afkTimeAgo) updateActionTime("playerLook", afkTimeAgo - 1);
      if(getLastActionTime("playerMove") > afkTimeAgo) updateActionTime("playerMove", afkTimeAgo - 1);
      if(getLastActionTime("playerInteract") > afkTimeAgo) updateActionTime("playerInteract", afkTimeAgo - 1);
      if(getLastActionTime("playerAction") > afkTimeAgo) updateActionTime("playerAction", afkTimeAgo - 1);
      if(getLastActionTime("selectSlot") > afkTimeAgo) updateActionTime("selectSlot", afkTimeAgo - 1);
      if(getLastActionTime("chatMessage") > afkTimeAgo) updateActionTime("chatMessage", afkTimeAgo - 1);
   }
   
   public void captchaSuccess(){
      // Mark player as active
      long curTime = System.currentTimeMillis();
      updateActionTime("playerLook", curTime);
      updateActionTime("playerMove", curTime);
      updateActionTime("playerInteract", curTime);
      updateActionTime("playerAction", curTime);
      updateActionTime("selectSlot", curTime);
      updateActionTime("chatMessage", curTime);
   }
   
   public void copyFromOldData(long totalTime, long activeTime, long afkTime, long lastUpdate, long stateChangeTime, long lastCaptcha, long lastTitlePulse, boolean afk, boolean levelOverridden, LimitedAFK.AFKLevel overrideLevel, HashMap<String, Long> lastActionTimes){
      this.totalTime = totalTime;
      this.activeTime = activeTime;
      this.afkTime = afkTime;
      this.lastUpdate = lastUpdate;
      this.stateChangeTime = stateChangeTime;
      this.lastCaptcha = lastCaptcha;
      this.lastTitlePulse = lastTitlePulse;
      this.afk = afk;
      this.levelOverridden = levelOverridden;
      this.overrideLevel = overrideLevel;
      this.lastActionTimes.putAll(lastActionTimes);
   }
   
   @Override
   public int compareTo(@NonNull PlayerData o){
      return Long.compare(o.getTotalTime(),totalTime);
   }
}
