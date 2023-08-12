package net.borisshoes.limitedafk.cca;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;

import java.util.HashMap;

public interface IPlayerProfileComponent extends ComponentV3 {
   long getTotalTime();
   long getActiveTime();
   long getAfkTime();
   HashMap<String,Long> getLastActionTimes();
   long getLastActionTime(String action);
   
   void update();
   void playerJoin();
   boolean updateActionTime(String action, long time);
   
   boolean isAfk();
   void setAfk(boolean afk);
   long getStateChangeTime();
}
