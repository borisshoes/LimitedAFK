package net.borisshoes.limitedafk.callbacks;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class TickTimerCallback {
   private int timer;
   protected ServerPlayerEntity player;
   private final Runnable task;
   
   public TickTimerCallback(int time, @Nullable ServerPlayerEntity player, Runnable task){
      timer = time;
      this.player = player;
      this.task = task;
   }
   
   public void onTimer(){
      task.run();
   }
   
   public int getTimer(){
      return timer;
   }
   
   public int decreaseTimer(){
      return this.timer--;
   }
   
   public void setTimer(int timer){
      this.timer = timer;
   }
   
   public ServerPlayerEntity getPlayer(){
      return player;
   }
}
