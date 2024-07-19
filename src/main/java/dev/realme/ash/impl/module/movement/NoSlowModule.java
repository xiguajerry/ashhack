package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.block.BlockSlipperinessEvent;
import dev.realme.ash.impl.event.block.SteppedOnSlimeBlockEvent;
import dev.realme.ash.impl.event.entity.SlowMovementEvent;
import dev.realme.ash.impl.event.entity.VelocityMultiplierEvent;
import dev.realme.ash.impl.event.network.GameJoinEvent;
import dev.realme.ash.impl.event.network.MovementSlowdownEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.SetCurrentHandEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.mixin.accessor.AccessorKeyBinding;
import dev.realme.ash.util.player.InventoryUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class NoSlowModule
extends ToggleModule {
    Config<Boolean> strictConfig = new BooleanConfig("Strict", "Strict NCP bypass for ground slowdowns", false);
    Config<Boolean> airStrictConfig = new BooleanConfig("AirStrict", "Strict NCP bypass for air slowdowns", false);
    Config<Boolean> grimConfig = new BooleanConfig("Grim", "Strict Grim bypass for slowdown", false);
    Config<Boolean> strafeFixConfig = new BooleanConfig("StrafeFix", "Old NCP bypass for strafe", false);
    Config<Boolean> inventoryMoveConfig = new BooleanConfig("InventoryMove", "Allows the player to move while in inventories or screens", true);
    Config<Boolean> arrowMoveConfig = new BooleanConfig("ArrowMove", "Allows the player to look while in inventories or screens by using the arrow keys", false);
    Config<Boolean> itemsConfig = new BooleanConfig("Items", "Removes the slowdown effect caused by using items", true);
    Config<Boolean> shieldsConfig = new BooleanConfig("Shields", "Removes the slowdown effect caused by shields", true);
    Config<Boolean> websConfig = new BooleanConfig("Webs", "Removes the slowdown caused when moving through webs", false);
    Config<Boolean> berryBushConfig = new BooleanConfig("BerryBush", "Removes the slowdown caused when moving through webs", false);
    Config<Float> webSpeedConfig = new NumberConfig<Float>("WebSpeed", "Speed to fall through webs", 0.0f, 3.5f, 20.0f, () -> this.websConfig.getValue());
    Config<Boolean> soulsandConfig = new BooleanConfig("SoulSand", "Removes the slowdown effect caused by walking over SoulSand blocks", false);
    Config<Boolean> honeyblockConfig = new BooleanConfig("HoneyBlock", "Removes the slowdown effect caused by walking over Honey blocks", false);
    Config<Boolean> slimeblockConfig = new BooleanConfig("SlimeBlock", "Removes the slowdown effect caused by walking over Slime blocks", false);
    private boolean sneaking;

    public NoSlowModule() {
        super("NoSlow", "Prevents items from slowing down player", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (this.airStrictConfig.getValue().booleanValue() && this.sneaking) {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(NoSlowModule.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
        this.sneaking = false;
        Managers.TICK.setClientTick(1.0f);
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event) {
        this.onEnable();
    }

    @EventListener
    public void onSetCurrentHand(SetCurrentHandEvent event) {
        if (this.airStrictConfig.getValue().booleanValue() && !this.sneaking && this.checkSlowed()) {
            this.sneaking = true;
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(NoSlowModule.mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }

    @EventListener
    public void onPlayerUpdate(UpdateWalkingEvent event) {
        if (event.getStage() == EventStage.PRE && this.grimConfig.getValue().booleanValue() && NoSlowModule.mc.player.isUsingItem() && !NoSlowModule.mc.player.isSneaking() && this.itemsConfig.getValue().booleanValue()) {
            ItemStack offHandStack = NoSlowModule.mc.player.getOffHandStack();
            if (NoSlowModule.mc.player.getActiveHand() != Hand.OFF_HAND && !offHandStack.isFood() && offHandStack.getItem() != Items.BOW && offHandStack.getItem() != Items.CROSSBOW && offHandStack.getItem() != Items.SHIELD) {
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id));
            }
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            if (this.airStrictConfig.getValue().booleanValue() && this.sneaking && !NoSlowModule.mc.player.isUsingItem()) {
                this.sneaking = false;
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(NoSlowModule.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            if (!this.strafeFixConfig.getValue().booleanValue() || this.checkSlowed()) {
                // empty if block
            }
            if (this.inventoryMoveConfig.getValue().booleanValue() && this.checkScreen()) {
                KeyBinding[] keys;
                long handle = mc.getWindow().getHandle();
                for (KeyBinding binding : keys = new KeyBinding[]{NoSlowModule.mc.options.jumpKey, NoSlowModule.mc.options.forwardKey, NoSlowModule.mc.options.backKey, NoSlowModule.mc.options.rightKey, NoSlowModule.mc.options.leftKey}) {
                    binding.setPressed(InputUtil.isKeyPressed(handle, ((AccessorKeyBinding) binding).getBoundKey().getCode()));
                }
                if (this.arrowMoveConfig.getValue().booleanValue()) {
                    float yaw = NoSlowModule.mc.player.getYaw();
                    float pitch = NoSlowModule.mc.player.getPitch();
                    if (InputUtil.isKeyPressed(handle, 265)) {
                        pitch -= 3.0f;
                    } else if (InputUtil.isKeyPressed(handle, 264)) {
                        pitch += 3.0f;
                    } else if (InputUtil.isKeyPressed(handle, 263)) {
                        yaw -= 3.0f;
                    } else if (InputUtil.isKeyPressed(handle, 262)) {
                        yaw += 3.0f;
                    }
                    NoSlowModule.mc.player.setYaw(yaw);
                    NoSlowModule.mc.player.setPitch(MathHelper.clamp(pitch, -90.0f, 90.0f));
                }
            }
            if (this.grimConfig.getValue().booleanValue() && (this.websConfig.getValue().booleanValue() || this.berryBushConfig.getValue().booleanValue())) {
                for (BlockPos pos : this.getIntersectingWebs()) {
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.DOWN));
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
                }
            }
        }
    }

    @EventListener
    public void onSlowMovement(SlowMovementEvent event) {
        Block block = event.getState().getBlock();
        if (block instanceof CobwebBlock && this.websConfig.getValue().booleanValue() || block instanceof SweetBerryBushBlock && this.berryBushConfig.getValue().booleanValue()) {
            if (this.grimConfig.getValue().booleanValue()) {
                event.cancel();
            } else if (NoSlowModule.mc.player.isOnGround()) {
                Managers.TICK.setClientTick(1.0f);
            } else {
                Managers.TICK.setClientTick(this.webSpeedConfig.getValue().floatValue() / 2.0f);
            }
        }
    }

    @EventListener
    public void onMovementSlowdown(MovementSlowdownEvent event) {
        if (this.checkSlowed()) {
            event.input.movementForward *= 5.0f;
            event.input.movementSideways *= 5.0f;
        }
    }

    @EventListener
    public void onVelocityMultiplier(VelocityMultiplierEvent event) {
        if (event.getBlock() == Blocks.SOUL_SAND && this.soulsandConfig.getValue().booleanValue() || event.getBlock() == Blocks.HONEY_BLOCK && this.honeyblockConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onSteppedOnSlimeBlock(SteppedOnSlimeBlockEvent event) {
        if (this.slimeblockConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onBlockSlipperiness(BlockSlipperinessEvent event) {
        if (event.getBlock() == Blocks.SLIME_BLOCK && this.slimeblockConfig.getValue().booleanValue()) {
            event.cancel();
            event.setSlipperiness(0.6f);
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        PlayerMoveC2SPacket packet;
        if (NoSlowModule.mc.player == null || NoSlowModule.mc.world == null || mc.isInSingleplayer()) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof PlayerMoveC2SPacket && (packet = (PlayerMoveC2SPacket) packet2).changesPosition() && this.strictConfig.getValue().booleanValue() && this.checkSlowed()) {
            InventoryUtil.doSwap(NoSlowModule.mc.player.getInventory().selectedSlot);
        } else if (event.getPacket() instanceof ClickSlotC2SPacket && this.strictConfig.getValue().booleanValue()) {
            if (NoSlowModule.mc.player.isUsingItem()) {
                NoSlowModule.mc.player.stopUsingItem();
            }
            if (this.sneaking || Managers.POSITION.isSneaking()) {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(NoSlowModule.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            if (Managers.POSITION.isSprinting()) {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(NoSlowModule.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }
    }

    public boolean checkSlowed() {
        return !NoSlowModule.mc.player.isRiding() && !NoSlowModule.mc.player.isSneaking() && (NoSlowModule.mc.player.isUsingItem() && this.itemsConfig.getValue() || NoSlowModule.mc.player.isBlocking() && this.shieldsConfig.getValue() && !this.grimConfig.getValue());
    }

    public boolean checkScreen() {
        return NoSlowModule.mc.currentScreen != null && !(NoSlowModule.mc.currentScreen instanceof ChatScreen) && !(NoSlowModule.mc.currentScreen instanceof SignEditScreen) && !(NoSlowModule.mc.currentScreen instanceof DeathScreen);
    }

    public List<BlockPos> getIntersectingWebs() {
        int radius = 5;
        ArrayList<BlockPos> blocks = new ArrayList<BlockPos>();
        for (int x = radius; x > -radius; --x) {
            for (int y = radius; y > -radius; --y) {
                for (int z = radius; z > -radius; --z) {
                    BlockPos blockPos = BlockPos.ofFloored(NoSlowModule.mc.player.getX() + (double)x, NoSlowModule.mc.player.getY() + (double)y, NoSlowModule.mc.player.getZ() + (double)z);
                    BlockState state = NoSlowModule.mc.world.getBlockState(blockPos);
                    if ((!(state.getBlock() instanceof CobwebBlock) || !this.websConfig.getValue().booleanValue()) && (!(state.getBlock() instanceof SweetBerryBushBlock) || !this.berryBushConfig.getValue().booleanValue())) continue;
                    blocks.add(blockPos);
                }
            }
        }
        return blocks;
    }

    public boolean getStrafeFix() {
        return this.strafeFixConfig.getValue();
    }
}
