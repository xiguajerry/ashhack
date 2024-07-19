package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.imixin.IMinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;

public class AutoFishModule
extends ToggleModule {
    Config<Boolean> openInventoryConfig = new BooleanConfig("OpenInventory", "Allows you to fish while in the inventory", true);
    Config<Integer> castDelayConfig = new NumberConfig<Integer>("CastingDelay", "The delay between fishing rod casts", 10, 15, 25);
    Config<Float> maxSoundDistConfig = new NumberConfig<Float>("MaxSoundDist", "The maximum distance from the splash sound", Float.valueOf(0.0f), Float.valueOf(2.0f), Float.valueOf(5.0f));
    private boolean autoReel;
    private int autoReelTicks;
    private int autoCastTicks;

    public AutoFishModule() {
        super("AutoFish", "Automatically casts and reels fishing rods", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        PlaySoundS2CPacket packet;
        if (AutoFishModule.mc.player == null) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof PlaySoundS2CPacket && (packet = (PlaySoundS2CPacket)((Object)packet2)).getSound().value() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH && AutoFishModule.mc.player.getMainHandStack().getItem() == Items.FISHING_ROD) {
            FishingBobberEntity fishHook = AutoFishModule.mc.player.fishHook;
            if (fishHook == null || fishHook.getPlayerOwner() != AutoFishModule.mc.player) {
                return;
            }
            double dist = fishHook.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ());
            if (dist <= (double)this.maxSoundDistConfig.getValue().floatValue()) {
                this.autoReel = true;
                this.autoReelTicks = 4;
            }
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (AutoFishModule.mc.currentScreen == null || AutoFishModule.mc.currentScreen instanceof ChatScreen || this.openInventoryConfig.getValue().booleanValue()) {
            if (AutoFishModule.mc.player.getMainHandStack().getItem() != Items.FISHING_ROD) {
                return;
            }
            FishingBobberEntity fishHook = AutoFishModule.mc.player.fishHook;
            if ((fishHook == null || fishHook.getHookedEntity() != null) && this.autoCastTicks <= 0) {
                ((IMinecraftClient)((Object)mc)).rightClick();
                this.autoCastTicks = this.castDelayConfig.getValue();
                return;
            }
            if (this.autoReel) {
                if (this.autoReelTicks <= 0) {
                    ((IMinecraftClient)((Object)mc)).rightClick();
                    this.autoReel = false;
                    return;
                }
                --this.autoReelTicks;
            }
        }
        --this.autoCastTicks;
    }
}
