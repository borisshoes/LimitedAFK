package net.borisshoes.limitedafk.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.limitedafk.LimitedAFK;
import net.borisshoes.limitedafk.PlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static net.borisshoes.limitedafk.LimitedAFK.CONFIG;

public class CaptchaGui extends SimpleGui {
   
   private final int timeout = 2400;
   private final boolean mode;
   private final int numPuzzles;
   
   private Item goalItem;
   private int curAttempt = 0;
   private int time = 0;
   
   boolean captchaFail = true;
   boolean captchaTimeout = false;
   
   public CaptchaGui(ServerPlayer player){
      super(MenuType.GENERIC_9x6, player, false);
      
      mode = Math.random() > 0.8;
      numPuzzles = 2 + (int)(Math.random()*6);
      
      setTitle(Component.translatable("text.limitedafk.captcha_title"));
   }
   
   @Override
   public void onTick(){
      time++;
      if(time >= timeout){
         captchaTimeout = true;
         close();
      }
      super.onTick();
   }
   
   @Override
   public boolean onAnyClick(int index, eu.pb4.sgui.api.ClickType type, ClickType action){
      if(index < getSize() && index >= 9){
         ItemStack clicked = getSlot(index).getItemStack();
         boolean success = mode ? ((TranslatableContents)clicked.getHoverName().getContents()).getKey().equals(goalItem.getDescriptionId()) : clicked.getItem().getDescriptionId().equals(goalItem.getDescriptionId());
         
         if(success){
            if(curAttempt < numPuzzles){
               curAttempt++;
               build();
            }else{
               captchaFail = false;
               close();
            }
         }else{
            close();
         }
      }
      return true;
   }
   
   public void build(){
      for(int i = 0; i < 9; i++){
         GuiElementBuilder pane = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE);
         pane.setName(Component.translatable("text.limitedafk.captcha_title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
         setSlot(i,pane);
      }
      
      goalItem = getRandomItem();
      
      GuiElementBuilder goalElem = new GuiElementBuilder(goalItem);
      goalElem.setName(Component.translatable(mode ? "text.limitedafk.captcha_goal_name" : "text.limitedafk.captcha_goal_look").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
      setSlot(4,goalElem);
      
      for(int i = 9; i < getSize(); i++){
         Item appearanceItem = getRandomItem();
         Item nameItem = getRandomItem();
         GuiElementBuilder itemElem = new GuiElementBuilder(appearanceItem);
         itemElem.setName(Component.translatable(nameItem.getDescriptionId()).withStyle(ChatFormatting.GREEN));
         setSlot(i,itemElem);
      }
      
      int successSlot = (int)(Math.random()*(getSize()-9)) + 9;
      GuiElementBuilder successElem;
      if(mode){
         successElem = new GuiElementBuilder(getRandomItem());
         successElem.setName(Component.translatable(goalItem.getDescriptionId()).withStyle(ChatFormatting.GREEN));
      }else{
         successElem = new GuiElementBuilder(goalItem);
         successElem.setName(Component.translatable(getRandomItem().getDescriptionId()).withStyle(ChatFormatting.GREEN));
      }
      setSlot(successSlot,successElem);
   }
   
   @Override
   public void onClose(){
      PlayerData profile = DataAccess.getPlayer(player.getUUID(),PlayerData.KEY);
      if(captchaFail){
         int timer = (CONFIG.getInt(LimitedAFK.CAPTCHA_TIMER) / 60);
         if(captchaTimeout){
            player.sendSystemMessage(Component.translatable("text.limitedafk.captcha_timeout",
                  Component.literal(String.valueOf(timer)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.RED));
         }else{
            player.sendSystemMessage(Component.translatable("text.limitedafk.captcha_failed",
                  Component.literal(String.valueOf(timer)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.RED));
         }
         profile.captchaFail();
      }else{
         profile.captchaSuccess();
      }
      super.onClose();
   }
   
   private Item getRandomItem(){
      Item item = BuiltInRegistries.ITEM.byId(((int)(Math.random()* BuiltInRegistries.ITEM.size())));
      return item == Items.AIR ? getRandomItem() : item;
   }
}
