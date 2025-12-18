package net.borisshoes.limitedafk.cca;

import net.borisshoes.limitedafk.LimitedAFK;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class PlayerProfileComponent implements IPlayerProfileComponent{
   private final HashMap<String,Long> lastActionTimes = new HashMap<>();
   private final LinkedList<Vec3> moves = new LinkedList<>();
   private final LinkedList<Vec2> looks = new LinkedList<>();
   private long totalTime, activeTime, afkTime, lastUpdate, stateChangeTime, lastCaptcha, lastTitlePulse;
   private boolean afk;
   private LimitedAFK.AFKLevel overrideLevel;
   private boolean levelOverridden;
   
   // Actions:
   // Move, Player Action, Player Input, Set Held Item, Interact/Use
   
   public PlayerProfileComponent(Player player){}
   
   @Override
   public void readData(ValueInput view){
      lastActionTimes.clear();
      
      totalTime = view.getLongOr("totalTime",0L);
      activeTime = view.getLongOr("activeTime",0L);
      afkTime = view.getLongOr("afkTime",0L);
      lastUpdate = view.getLongOr("lastUpdate",0L);
      stateChangeTime = view.getLongOr("stateChangeTime",0L);
      afk = view.getBooleanOr("isAfk",false);
      levelOverridden = view.getBooleanOr("levelOverridden",false);

      try{
         overrideLevel = LimitedAFK.AFKLevel.valueOf(view.getStringOr("afkLevel",""));
      }catch(Exception e){
         overrideLevel = (LimitedAFK.AFKLevel) LimitedAFK.CONFIG.getValue(LimitedAFK.DEFAULT_AFK_DETECTION_LEVEL.getName());
      }
      
      CompoundTag lastActionsTag = view.read("lastActions", CompoundTag.CODEC).orElse(new CompoundTag());
      Set<String> keys = lastActionsTag.keySet();
      keys.forEach(key ->{
         lastActionTimes.put(key,lastActionsTag.getLongOr(key,0L));
      });
   }
   
   @Override
   public void writeData(ValueOutput view){
      view.putLong("totalTime",totalTime);
      view.putLong("activeTime",activeTime);
      view.putLong("afkTime",afkTime);
      view.putLong("lastUpdate",lastUpdate);
      view.putLong("stateChangeTime",stateChangeTime);
      view.putBoolean("isAfk",afk);
      view.putBoolean("levelOverridden",levelOverridden);
      
      try{
         view.putString("afkLevel", overrideLevel.getSerializedName());
      }catch(Exception e){
         view.putString("afkLevel", ((LimitedAFK.AFKLevel) LimitedAFK.CONFIG.getValue(LimitedAFK.DEFAULT_AFK_DETECTION_LEVEL.getName())).getSerializedName());
      }
      
      CompoundTag lastActionsTag = new CompoundTag();
      lastActionTimes.forEach(lastActionsTag::putLong);
      view.store("lastActions", CompoundTag.CODEC,lastActionsTag);
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
   public long getLastUpdate(){
      return lastUpdate;
   }
   
   @Override
   public long getStateChangeTime(){
      return stateChangeTime;
   }
   
   @Override
   public long getLastCaptcha(){
      return lastCaptcha;
   }
   
   @Override
   public long getLastTitlePulse(){
      return lastTitlePulse;
   }
   
   @Override
   public boolean isAfk(){
      return afk;
   }
   
   @Override
   public boolean isLevelOverridden(){
      return levelOverridden;
   }
   
   @Override
   public LimitedAFK.AFKLevel getOverrideLevel(){
      return overrideLevel;
   }
   
   @Override
   public HashMap<String, Long> getLastActionTimes(){
      return lastActionTimes;
   }
   
   @Override
   public void clear(){
      lastActionTimes.clear();
      moves.clear();
      looks.clear();
      totalTime = 0;
      activeTime = 0;
      afkTime = 0;
      lastUpdate = 0;
      stateChangeTime = 0;
      lastCaptcha = 0;
      lastTitlePulse = 0;
      afk = false;
      overrideLevel = null;
      levelOverridden = false;
   }
   
   @Override
   public int compareTo(@NotNull IPlayerProfileComponent o){
      return Long.compare(o.getTotalTime(),totalTime);
   }
}
