package net.borisshoes.limitedafk.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.limitedafk.cca.IPlayerProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import static net.borisshoes.limitedafk.LimitedAFK.config;
import static net.borisshoes.limitedafk.cca.PlayerComponentInitializer.PLAYER_DATA;

public class CaptchaGui extends SimpleGui {
   
   private final int timeout = 2400;
   private final boolean mode;
   private final int numPuzzles;
   
   private Item goalItem;
   private int curAttempt = 0;
   private int time = 0;
   
   boolean captchaFail = true;
   boolean captchaTimeout = false;
   
   public CaptchaGui(ServerPlayerEntity player){
      super(ScreenHandlerType.GENERIC_9X6, player, false);
      
      mode = Math.random() > 0.8;
      numPuzzles = 2 + (int)(Math.random()*6);
      
      setTitle(Text.literal("AFK Captcha"));
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
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(index < getSize() && index >= 9){
         ItemStack clicked = getSlot(index).getItemStack();
         boolean success = mode ? ((TranslatableTextContent)clicked.getName().getContent()).getKey().equals(goalItem.getTranslationKey()) : clicked.getItem().getTranslationKey().equals(goalItem.getTranslationKey());
         
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
         pane.setName(Text.literal("AFK Captcha").formatted(Formatting.RED,Formatting.BOLD));
         setSlot(i,pane);
      }
      
      goalItem = getRandomItem();
      String goalStr = mode ? "Click the item NAMED after this item." : "Click the item that LOOKS like this item.";
      
      GuiElementBuilder goalElem = new GuiElementBuilder(goalItem);
      goalElem.setName(Text.literal(goalStr).formatted(Formatting.RED,Formatting.BOLD));
      setSlot(4,goalElem);
      
      for(int i = 9; i < getSize(); i++){
         Item appearanceItem = getRandomItem();
         Item nameItem = getRandomItem();
         GuiElementBuilder itemElem = new GuiElementBuilder(appearanceItem);
         itemElem.setName(Text.translatable(nameItem.getTranslationKey()).formatted(Formatting.GREEN));
         setSlot(i,itemElem);
      }
      
      int successSlot = (int)(Math.random()*(getSize()-9)) + 9;
      GuiElementBuilder successElem;
      if(mode){
         successElem = new GuiElementBuilder(getRandomItem());
         successElem.setName(Text.translatable(goalItem.getTranslationKey()).formatted(Formatting.GREEN));
      }else{
         successElem = new GuiElementBuilder(goalItem);
         successElem.setName(Text.translatable(getRandomItem().getTranslationKey()).formatted(Formatting.GREEN));
      }
      setSlot(successSlot,successElem);
   }
   
   @Override
   public void onClose(){
      IPlayerProfileComponent profile = PLAYER_DATA.get(player);
      if(captchaFail){
         int timer = ((int)config.getValue("captchaTimer")) / 60;
         if(captchaTimeout){
            player.sendMessage(Text.literal("The captcha timed out, try again in "+timer+" minutes").formatted(Formatting.RED));
         }else{
            player.sendMessage(Text.literal("You failed the captcha, try again in "+timer+" minutes").formatted(Formatting.RED));
         }
         profile.captchaFail();
      }else{
         profile.captchaSuccess();
      }
      super.onClose();
   }
   
   private Item getRandomItem(){
      Item item = Registries.ITEM.get(((int)(Math.random()*Registries.ITEM.size())));
      return item == Items.AIR ? getRandomItem() : item;
   }
}
