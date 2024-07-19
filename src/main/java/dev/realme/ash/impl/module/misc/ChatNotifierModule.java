package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.entity.EntityDeathEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.impl.event.world.AddEntityEvent;
import dev.realme.ash.impl.event.world.RemoveEntityEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.chat.ChatUtil;
import java.util.HashMap;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;

public class ChatNotifierModule
extends ToggleModule {
    Config<Boolean> totemPopConfig = new BooleanConfig("TotemPop", "Notifies in chat when a player pops a totem", true);
    Config<Boolean> visualRangeConfig = new BooleanConfig("VisualRange", "Notifies in chat when player enters visual range", false);
    Config<Boolean> friendsConfig = new BooleanConfig("Friends", "Notifies for friends", false);
    Config<Boolean> armorNotify = new BooleanConfig("ArmorNotify", "", false);
    Config<Double> threshold = new NumberConfig<Double>("Threshold", "", 0.0, 30.0, 100.0);
    Config<Integer> volume = new NumberConfig<Integer>("Volume", "", 0, 10, 20);
    private boolean alertedHelmet;
    private boolean alertedChestplate;
    private boolean alertedLeggings;
    private boolean alertedBoots;
    public static HashMap<String, Integer> PopContainer = new HashMap();

    public ChatNotifierModule() {
        super("ChatNotifier", "Notifies in chat", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (ChatNotifierModule.nullCheck()) {
            return;
        }
        if (this.armorNotify.getValue().booleanValue()) {
            Iterable<ItemStack> armorPieces = ChatNotifierModule.mc.player.getArmorItems();
            for (ItemStack armorPiece : armorPieces) {
                if (ChatNotifierModule.checkNotifyThreshold(armorPiece, this.threshold.getValue())) {
                    if (ChatNotifierModule.isHelmetArmor(armorPiece) && !this.alertedHelmet || ChatNotifierModule.isChestplateArmor(armorPiece) && !this.alertedChestplate || ChatNotifierModule.isLeggingsArmor(armorPiece) && !this.alertedLeggings || ChatNotifierModule.isBootsArmor(armorPiece) && !this.alertedBoots) {
                        ChatUtil.sendChatMessageWidthId("Your armors durability is low.", this.hashCode() - 1337);
                        ChatNotifierModule.mc.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, this.volume.getValue().intValue(), ChatNotifierModule.mc.player.getPitch());
                    }
                    if (ChatNotifierModule.isHelmetArmor(armorPiece) && !this.alertedHelmet) {
                        this.alertedHelmet = true;
                        continue;
                    }
                    if (ChatNotifierModule.isChestplateArmor(armorPiece) && !this.alertedChestplate) {
                        this.alertedChestplate = true;
                        continue;
                    }
                    if (ChatNotifierModule.isLeggingsArmor(armorPiece) && !this.alertedLeggings) {
                        this.alertedLeggings = true;
                        continue;
                    }
                    if (!ChatNotifierModule.isBootsArmor(armorPiece) || this.alertedBoots) continue;
                    this.alertedBoots = true;
                    continue;
                }
                if (ChatNotifierModule.checkNotifyThreshold(armorPiece, this.threshold.getValue())) continue;
                if (ChatNotifierModule.isHelmetArmor(armorPiece) && this.alertedHelmet) {
                    this.alertedHelmet = false;
                    continue;
                }
                if (ChatNotifierModule.isChestplateArmor(armorPiece) && this.alertedChestplate) {
                    this.alertedChestplate = false;
                    continue;
                }
                if (ChatNotifierModule.isLeggingsArmor(armorPiece) && this.alertedLeggings) {
                    this.alertedLeggings = false;
                    continue;
                }
                if (!ChatNotifierModule.isBootsArmor(armorPiece) || !this.alertedBoots) continue;
                this.alertedBoots = false;
            }
        }
    }

    public static boolean checkNotifyThreshold(ItemStack i, double threshold) {
        return ChatNotifierModule.getArmorDamage(i) <= threshold;
    }

    public static double getArmorDamage(ItemStack i) {
        return (double)(i.getMaxDamage() - i.getDamage()) / (double)i.getMaxDamage() * 100.0;
    }

    public static boolean isHelmetArmor(ItemStack itemStack) {
        ArmorItem armorItem;
        if (itemStack == null) {
            return false;
        }
        Item item = itemStack.getItem();
        return item instanceof ArmorItem && (armorItem = (ArmorItem)((Object)item)).getSlotType() == EquipmentSlot.HEAD;
    }

    public static boolean isChestplateArmor(ItemStack itemStack) {
        ArmorItem armorItem;
        if (itemStack == null) {
            return false;
        }
        Item item = itemStack.getItem();
        return item instanceof ArmorItem && (armorItem = (ArmorItem)((Object)item)).getSlotType() == EquipmentSlot.CHEST;
    }

    public static boolean isLeggingsArmor(ItemStack itemStack) {
        ArmorItem armorItem;
        if (itemStack == null) {
            return false;
        }
        Item item = itemStack.getItem();
        return item instanceof ArmorItem && (armorItem = (ArmorItem)((Object)item)).getSlotType() == EquipmentSlot.LEGS;
    }

    public static boolean isBootsArmor(ItemStack itemStack) {
        ArmorItem armorItem;
        if (itemStack == null) {
            return false;
        }
        Item item = itemStack.getItem();
        return item instanceof ArmorItem && (armorItem = (ArmorItem)((Object)item)).getSlotType() == EquipmentSlot.FEET;
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        EntityStatusS2CPacket packet;
        if (ChatNotifierModule.nullCheck()) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof EntityStatusS2CPacket && (packet = (EntityStatusS2CPacket)((Object)packet2)).getStatus() == 35 && this.totemPopConfig.getValue().booleanValue()) {
            Entity entity = packet.getEntity(ChatNotifierModule.mc.world);
            if (!(entity instanceof LivingEntity) || entity.getDisplayName() == null) {
                return;
            }
            int totems = Managers.TOTEM.getTotems(entity);
            String playerName = entity.getDisplayName().getString();
            boolean isFriend = Managers.SOCIAL.isFriend(playerName);
            if (isFriend && !this.friendsConfig.getValue().booleanValue()) {
                return;
            }
            PopContainer.put(playerName, totems);
            if (totems == 1) {
                if (entity.equals(ChatNotifierModule.mc.player)) {
                    ChatUtil.sendChatMessageWidthId(Formatting.GRAY + "[" + Formatting.AQUA + "PopCounter" + Formatting.GRAY + "] " + Formatting.WHITE + "You" + Formatting.WHITE + " popped " + Formatting.RED + totems + Formatting.WHITE + " Totem.", entity.getId());
                } else {
                    ChatUtil.sendChatMessageWidthId(Formatting.GRAY + "[" + Formatting.AQUA + "PopCounter" + Formatting.GRAY + "] " + Formatting.WHITE + playerName + " popped " + Formatting.RED + totems + Formatting.WHITE + " Totem.", entity.getId());
                }
            } else if (entity.equals(ChatNotifierModule.mc.player)) {
                ChatUtil.sendChatMessageWidthId(Formatting.GRAY + "[" + Formatting.AQUA + "PopCounter" + Formatting.GRAY + "] " + Formatting.WHITE + "You" + Formatting.WHITE + " popped " + Formatting.RED + totems + Formatting.WHITE + " Totems.", entity.getId());
            } else {
                ChatUtil.sendChatMessageWidthId(Formatting.GRAY + "[" + Formatting.AQUA + "PopCounter" + Formatting.GRAY + "] " + Formatting.WHITE + playerName + Formatting.WHITE + " popped " + Formatting.RED + totems + Formatting.WHITE + " Totems.", entity.getId());
            }
        }
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event) {
        PlayerEntity player;
        block5: {
            block4: {
                Entity entity;
                if (!this.visualRangeConfig.getValue().booleanValue() || !((entity = event.getEntity()) instanceof PlayerEntity)) break block4;
                player = (PlayerEntity)entity;
                if (event.getEntity().getDisplayName() != null) break block5;
            }
            return;
        }
        String playerName = Objects.requireNonNull(player.getDisplayName()).getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !this.friendsConfig.getValue().booleanValue() || event.getEntity() == ChatNotifierModule.mc.player) {
            return;
        }
        ChatUtil.sendChatMessageWidthId("\u00a7s[VisualRange] " + (String)(isFriend ? "\u00a7b" + playerName : playerName) + "\u00a7f entered your visual range.", player.getId());
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        PlayerEntity player;
        block5: {
            block4: {
                Entity entity;
                if (!this.visualRangeConfig.getValue().booleanValue() || !((entity = event.getEntity()) instanceof PlayerEntity)) break block4;
                player = (PlayerEntity)entity;
                if (event.getEntity().getDisplayName() != null) break block5;
            }
            return;
        }
        String playerName = Objects.requireNonNull(player.getDisplayName()).getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !this.friendsConfig.getValue().booleanValue() || event.getEntity() == ChatNotifierModule.mc.player) {
            return;
        }
        ChatUtil.sendChatMessageWidthId("\u00a7s[VisualRange] " + (isFriend ? "\u00a7b" + playerName : "\u00a7c" + playerName) + "\u00a7f left your visual range.", player.getId());
    }

    @EventListener
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity livingEntity;
        if (event.getEntity().getDisplayName() == null || !this.totemPopConfig.getValue().booleanValue() || !((livingEntity = event.getEntity()) instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity)livingEntity;
        int totems = Managers.TOTEM.getTotems(event.getEntity());
        String playerName = Objects.requireNonNull(player.getDisplayName()).getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !this.friendsConfig.getValue().booleanValue()) {
            return;
        }
        if (totems == 0) {
            if (player.equals(ChatNotifierModule.mc.player)) {
                ChatUtil.sendChatMessageWidthId("You died.", player.getId());
            } else {
                ChatUtil.sendChatMessageWidthId(playerName + " died.", player.getId());
            }
            return;
        }
        if (PopContainer.containsKey(playerName)) {
            PopContainer.remove(playerName);
            if (totems == 1) {
                if (player.equals(ChatNotifierModule.mc.player)) {
                    ChatUtil.sendChatMessageWidthId(Formatting.GRAY + "[" + Formatting.AQUA + "PopCounter" + Formatting.GRAY + "] " + Formatting.WHITE + "You" + Formatting.WHITE + " died after popping " + Formatting.RED + totems + Formatting.WHITE + " Totem.", player.getId());
                } else {
                    ChatUtil.sendChatMessageWidthId(Formatting.GRAY + "[" + Formatting.AQUA + "PopCounter" + Formatting.GRAY + "] " + Formatting.WHITE + playerName + Formatting.WHITE + " died after popping " + Formatting.RED + totems + Formatting.WHITE + " Totem.", player.getId());
                }
            } else if (player.equals(ChatNotifierModule.mc.player)) {
                ChatUtil.sendChatMessageWidthId(Formatting.GRAY + "[" + Formatting.AQUA + "PopCounter" + Formatting.GRAY + "] " + Formatting.WHITE + "You" + Formatting.WHITE + " died after popping " + Formatting.RED + totems + Formatting.WHITE + " Totems.", player.getId());
            } else {
                ChatUtil.sendChatMessageWidthId(Formatting.GRAY + "[" + Formatting.AQUA + "PopCounter" + Formatting.GRAY + "] " + Formatting.WHITE + playerName + Formatting.WHITE + " died after popping " + Formatting.RED + totems + Formatting.WHITE + " Totems.", player.getId());
            }
        }
    }
}
