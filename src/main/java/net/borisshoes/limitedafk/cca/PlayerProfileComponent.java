package net.borisshoes.limitedafk.cca;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.borisshoes.limitedafk.LimitedAFK.config;

public class PlayerProfileComponent implements IPlayerProfileComponent{
   private final PlayerEntity player;
   private final HashMap<String,Long> lastActionTimes = new HashMap<>();
   private final LinkedList<Vec3d> moves = new LinkedList<>();
   private final LinkedList<Vec2f> looks = new LinkedList<>();
   private long totalTime, activeTime, afkTime, lastUpdate, stateChangeTime;
   private boolean afk;
   
   // Actions:
   // Move, Player Action, Player Input, Set Held Item, Interact/Use
   
   public PlayerProfileComponent(PlayerEntity player){
      this.player = player;
   }
   
   @Override
   public PlayerEntity getPlayer(){
      return player;
   }
   
   @Override
   public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup){
      lastActionTimes.clear();
      
      totalTime = tag.getLong("totalTime");
      activeTime = tag.getLong("activeTime");
      afkTime = tag.getLong("afkTime");
      lastUpdate = tag.getLong("lastUpdate");
      stateChangeTime = tag.getLong("stateChangeTime");
      afk = tag.getBoolean("isAfk");
      
      NbtCompound lastActionsTag = tag.getCompound("lastActions");
      Set<String> keys = lastActionsTag.getKeys();
      keys.forEach(key ->{
         lastActionTimes.put(key,lastActionsTag.getLong(key));
      });
   }
   
   @Override
   public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup){
      tag.putLong("totalTime",totalTime);
      tag.putLong("activeTime",activeTime);
      tag.putLong("afkTime",afkTime);
      tag.putLong("lastUpdate",lastUpdate);
      tag.putLong("stateChangeTime",stateChangeTime);
      tag.putBoolean("isAfk",afk);
      
      NbtCompound lastActionsTag = new NbtCompound();
      lastActionTimes.forEach(lastActionsTag::putLong);
      tag.put("lastActions",lastActionsTag);
   }
   
   @Override
   public long getTotalTime(){
      return totalTime;
   }
   
   @Override
   public long getActiveTime(){
      return activeTime;
   }
   
   @Override
   public long getAfkTime(){
      return afkTime;
   }
   
   @Override
   public HashMap<String, Long> getLastActionTimes(){
      return lastActionTimes;
   }
   
   @Override
   public long getLastActionTime(String action){
      return lastActionTimes.getOrDefault(action, -1L);
   }
   
   @Override
   public void playerJoin(){
      long curTime = System.currentTimeMillis();
      updateActionTime("playerLook",curTime);
      updateActionTime("playerMove",curTime);
      updateActionTime("playerInteract",curTime);
      updateActionTime("playerAction",curTime);
      updateActionTime("selectSlot",curTime);
      updateActionTime("chatMessage",curTime);
      lastUpdate = curTime;
      stateChangeTime = curTime;
      afk = false;
   }
   
   private void checkMoveAndLook(){
      long curTime = System.currentTimeMillis();
      // Add New Elements and Remove Old
      moves.add(player.getPos());
      while(moves.size() > 10){
         moves.remove();
      }
      looks.add(player.getRotationClient());
      while(looks.size() > 5){
         looks.remove();
      }
      
      // See if the player has moved more than 10 blocks in the past 10 seconds
      Vec3d travelled = new Vec3d(0,0,0);
      for(int i = 0; i < moves.size()-1; i++){
         Vec3d move1 = moves.get(i);
         Vec3d move2 = moves.get(i+1);
         travelled = travelled.add(move2.subtract(move1));
      }
      if(travelled.length() > 10){
         updateActionTime("playerMove",curTime);
      }
      
      // See if the player has at least 3 unique looks in the past 5 seconds
      List<Vec2f> uniqueLooks = new ArrayList<>();
      for(Vec2f look : looks){
         boolean found = false;
         for(Vec2f uniqueLook : uniqueLooks){
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
         updateActionTime("playerLook",curTime);
      }
   }
   
   @Override
   public void update(){
      if(totalTime == 0){totalTime++;}
      checkMoveAndLook();
      
      long curTime = System.currentTimeMillis();
      long millis = curTime - lastUpdate;
      long afkTimer = 1000L*((int)config.getValue("afkTimer"));
      
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
      
      boolean curAfk = afkCount < 18;
      
      if((player.isCreative() || player.isSpectator()) && (boolean)config.getValue("ignoreCreativeAndSpectator")){
         curAfk = false;
      }
      
      if(curAfk && !afk){ // Switching to AFK
         stateChangeTime = curTime;
         
         if((boolean)config.getValue("announceAfk")){
            player.getServer().getPlayerManager().broadcast(
                  Text.literal("")
                        .append(player.getDisplayName())
                        .append(" is now AFK").formatted(Formatting.WHITE),false);
         }
      }
      if(!curAfk && afk){ // Switching to Active
         stateChangeTime = curTime;
         
         if((boolean)config.getValue("announceAfk")){
            player.getServer().getPlayerManager().broadcast(
                  Text.literal("")
                        .append(player.getDisplayName())
                        .append(" is no longer AFK").formatted(Formatting.WHITE),false);
         }
      }
      
      afk = curAfk;
      if(afk){
         afkTime += millis;
         
         if((boolean)config.getValue("enabled") && 100*afkTime/totalTime >= (int)config.getValue("allowedAfkPercentage")){
            if(player instanceof ServerPlayerEntity serverPlayer){
               serverPlayer.networkHandler.disconnect(Text.literal("You have AFK'd too much"));
            }
         }
      }else{
         activeTime += millis;
      }
      totalTime += millis;
      lastUpdate = curTime;
   }
   
   @Override
   public boolean updateActionTime(String action, long time){
      boolean has = lastActionTimes.containsKey(action);
      lastActionTimes.put(action,time);
      return has;
   }
   
   @Override
   public boolean isAfk(){
      return afk;
   }
   
   @Override
   public void setAfk(boolean afk){
      this.afk = afk;
   }
   
   @Override
   public long getStateChangeTime(){
      return stateChangeTime;
   }
   
   @Override
   public int compareTo(@NotNull IPlayerProfileComponent o){
      return Long.compare(o.getTotalTime(),totalTime);
   }
}
