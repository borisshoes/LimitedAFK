package net.borisshoes.limitedafk.cca;

import net.borisshoes.limitedafk.LimitedAFK;
import org.ladysnake.cca.api.v3.component.ComponentV3;

import java.util.HashMap;

public interface IPlayerProfileComponent extends ComponentV3, Comparable<IPlayerProfileComponent> {
   long getTotalTime();
   long getActiveTime();
   long getAfkTime();
   long getLastUpdate();
   long getStateChangeTime();
   long getLastCaptcha();
   long getLastTitlePulse();
   boolean isAfk();
   boolean isLevelOverridden();
   LimitedAFK.AFKLevel getOverrideLevel();
   HashMap<String, Long> getLastActionTimes();
   void clear();
}
