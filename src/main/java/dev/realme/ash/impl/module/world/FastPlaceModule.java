package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.ItemListConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.mixin.accessor.AccessorMinecraftClient;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.world.SneakBlocks;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

public class FastPlaceModule
extends ToggleModule {
    Config<Selection> selectionConfig = new EnumConfig<Selection>("Selection", "The selection of items to apply fast placements", Selection.WHITELIST, Selection.values());
    Config<Integer> delayConfig = new NumberConfig<Integer>("Delay", "Fast place click delay", 0, 1, 4);
    Config<Float> startDelayConfig = new NumberConfig<Float>("StartDelay", "Fast place start delay", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(1.0f));
    Config<Boolean> ghostFixConfig = new BooleanConfig("GhostFix", "Fixes item ghosting issue on some servers", false);
    Config<List<Item>> whitelistConfig = new ItemListConfig("Whitelist", "Valid item whitelist", Items.EXPERIENCE_BOTTLE, Items.SNOWBALL, Items.EGG);
    Config<List<Item>> blacklistConfig = new ItemListConfig("Blacklist", "Valid item blacklist", Items.ENDER_PEARL, Items.ENDER_EYE);
    private final CacheTimer startTimer = new CacheTimer();

    public FastPlaceModule() {
        super("FastPlace", "Place items and blocks faster", ModuleCategory.WORLD);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (!FastPlaceModule.mc.options.useKey.isPressed()) {
            this.startTimer.reset();
        } else if (this.startTimer.passed(this.startDelayConfig.getValue(), TimeUnit.SECONDS) && ((AccessorMinecraftClient)((Object)mc)).hookGetItemUseCooldown() > this.delayConfig.getValue() && this.placeCheck(FastPlaceModule.mc.player.getMainHandStack())) {
            if (this.ghostFixConfig.getValue().booleanValue()) {
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(FastPlaceModule.mc.player.getActiveHand(), id));
            }
            ((AccessorMinecraftClient)((Object)mc)).hookSetItemUseCooldown(this.delayConfig.getValue());
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        if (FastPlaceModule.mc.player == null || FastPlaceModule.mc.world == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerInteractBlockC2SPacket) {
            BlockState state;
            PlayerInteractBlockC2SPacket packet2 = (PlayerInteractBlockC2SPacket)packet;
            if (this.ghostFixConfig.getValue().booleanValue() && !event.isClientPacket() && this.placeCheck(FastPlaceModule.mc.player.getStackInHand(packet2.getHand())) && !SneakBlocks.isSneakBlock(state = FastPlaceModule.mc.world.getBlockState(packet2.getBlockHitResult().getBlockPos()))) {
                event.cancel();
            }
        }
    }

    private boolean placeCheck(ItemStack held) {
        return switch (this.selectionConfig.getValue()) {
            default -> throw new IncompatibleClassChangeError();
            case WHITELIST -> ((ItemListConfig)this.whitelistConfig).contains(held.getItem());
            case BLACKLIST -> !((ItemListConfig) this.blacklistConfig).contains(held.getItem());
            case ALL -> true;
        };
    }

    public static enum Selection {
        WHITELIST,
        BLACKLIST,
        ALL;

    }
}
