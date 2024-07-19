package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.RotateEvent;
import dev.realme.ash.impl.event.network.AttackBlockEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.impl.gui.click.ClickGuiScreen;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.IPlayerMoveC2SPacket;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.render.FadeUtils;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PacketDiggingModule
extends RotationModule {
    final Config<Float> delay = new NumberConfig<>("Delay", "Added delays", 0.0f, 50.0f, 500.0f);
    final Config<Double> damage = new NumberConfig<>("Damage", "The speed to mine blocks", 0.0, 1.0, 1.0);
    final Config<Integer> maxBreak = new NumberConfig<>("BreakCount", "", 0, 8, 50);
    final Config<Float> range = new NumberConfig<>("Range", "", 0.0f, 5.0f, 8.0f);
    final Config<Boolean> doubleBreak = new BooleanConfig("DoubleBreak", "", true);
    final Config<Boolean> autoSwap = new BooleanConfig("AutoSwap", "", false);
    final Config<Boolean> clickSlot = new BooleanConfig("ClickSlot", "", false);
    final Config<Double> switchDamage = new NumberConfig<>("SwitchDamage", "", 0.0, 0.8, 1.0);
    final Config<Double> stopDamage = new NumberConfig<>("StopDamage", "", 0.0, 0.8, 1.0);
    final Config<Double> failedDamage = new NumberConfig<>("FailedDamage", "", 1.0, 1.5, 2.5);
    final Config<Boolean> startingRotate = new BooleanConfig("StartingRotate", "", false);
    final Config<Boolean> endingRotate = new BooleanConfig("EndingRotate", "", false);
    final Config<Float> time = new NumberConfig<>("Time", "", 0.0f, 100.0f, 2000.0f);
    final Config<Float> subtractTime = new NumberConfig<>("SubtractTime", "", 0.0f, 100.0f, 2000.0f);
    final Config<Boolean> pauseEat = new BooleanConfig("PauseEat", "Not attacking while using items", true);
    final Config<Boolean> checkGround = new BooleanConfig("CheckGround", "", true);
    final Config<Boolean> retry = new BooleanConfig("Retry", "", false);
    final Config<Boolean> syncBreak = new BooleanConfig("SyncBreak", "", false);
    final Config<Boolean> wait = new BooleanConfig("Stay", "", true);
    final Config<Boolean> instant = new BooleanConfig("Instant", "", true);
    final Config<Boolean> hotBar = new BooleanConfig("HotBarSwap", "", true);
    final Config<Color> color = new ColorConfig("Color", "", new Color(90, 90, 255), false, false);
    final Config<RenderMode> renderMode = new EnumConfig<>("RenderMode", "", RenderMode.Grow, RenderMode.values());
    final Config<Double> expandLine = new NumberConfig<>("ExpandLine", "", 0.0, 0.1, 0.5);
    final Config<Boolean> box = new BooleanConfig("Box", "", true);
    final Config<Integer> boxAlpha = new NumberConfig<>("BoxAlpha", "", 0, 80, 255);
    final Config<Boolean> line = new BooleanConfig("lines", "", false);
    final Config<Integer> olAlpha = new NumberConfig<>("OLAlpha", "", 0, 255, 255);
    final Config<Float> olWidth = new NumberConfig<>("OLWidth", "", 0.1f, 1.5f, 5.0f);
    final Config<Boolean> ignoreSame = new BooleanConfig("IgnoreSame", "", true);
    final Config<Color> double_color = new ColorConfig("Double_Color", "", new Color(255, 255, 255), false, false);
    final Config<Boolean> double_box = new BooleanConfig("Double_Box", "", true);
    final Config<Integer> double_boxAlpha = new NumberConfig<>("Double_BoxAlpha", "", 0, 80, 255);
    final Config<Boolean> double_line = new BooleanConfig("Double_lines", "", false);
    final Config<Integer> double_olAlpha = new NumberConfig<>("Double_OLAlpha", "", 0, 255, 255);
    final Config<Float> double_olWidth = new NumberConfig<>("Double_OLWidth", "", 0.1f, 1.5f, 5.0f);
    public static final List<Block> godBlocks = Arrays.asList(Blocks.COMMAND_BLOCK, Blocks.LAVA_CAULDRON, Blocks.LAVA, Blocks.WATER_CAULDRON, Blocks.WATER, Blocks.BEDROCK, Blocks.BARRIER, Blocks.END_PORTAL, Blocks.NETHER_PORTAL, Blocks.END_PORTAL_FRAME);
    public BlockPos breakPos;
    public BlockPos secondPos;
    private final Timer mineTimer = new CacheTimer();
    private final FadeUtils animationTime = new FadeUtils(1000L);
    private final FadeUtils secondAnim = new FadeUtils(1000L);
    public boolean startMine = false;
    public int breakNumber = 0;
    private final Timer secondTimer = new CacheTimer();
    private final Timer delayTimer = new CacheTimer();
    int lastSlot = -1;

    public PacketDiggingModule() {
        super("PacketDigging", "Packet mine.", ModuleCategory.WORLD);
    }

    @Override
    public String getModuleData() {
        if (this.breakPos == null) {
            return "0.0%";
        }
        int slot = this.getTool(this.breakPos);
        if (slot == -1) {
            slot = PacketDiggingModule.mc.player.getInventory().selectedSlot;
        }
        double breakTime = this.getBreakTime(this.breakPos, slot, this.damage.getValue(), this.checkGround.getValue());
        double progress = (double)this.mineTimer.getElapsedTime() / breakTime;
        DecimalFormat df = new DecimalFormat("0.0");
        return BlockUtil.isAir(this.breakPos) ? "Done" : df.format(progress * 100.0) + "%";
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        this.startMine = false;
        this.breakPos = null;
        this.secondPos = null;
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        this.update();
    }

    @EventListener
    public void onPlayerUpdate(UpdateWalkingEvent event) {
        this.update();
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        this.update();
        if (!PacketDiggingModule.mc.player.isCreative()) {
            if (this.breakPos != null) {
                int slot = this.getTool(this.breakPos);
                if (slot == -1) {
                    slot = PacketDiggingModule.mc.player.getInventory().selectedSlot;
                }
                this.animationTime.setLength((long)this.getBreakTime(this.breakPos, slot, this.damage.getValue(), this.checkGround.getValue()));
                double ease = switch (this.renderMode.getValue()) {
                    default -> throw new IncompatibleClassChangeError();
                    case Double -> this.animationTime.easeOutQuad();
                    case Grow -> (1.0 - this.animationTime.easeOutQuad()) * 0.5;
                };
                double easeLine = switch (this.renderMode.getValue()) {
                    default -> throw new IncompatibleClassChangeError();
                    case Double -> this.animationTime.easeOutQuad() + this.expandLine.getValue();
                    case Grow -> (1.0 - this.animationTime.easeOutQuad()) * 0.5;
                };
                ColorConfig first = (ColorConfig)this.color;
                if (this.box.getValue()) {
                    RenderManager.renderBox(event.getMatrices(), new Box(this.breakPos).shrink(ease, ease, ease).shrink(-ease, -ease, -ease), first.getRgb(this.boxAlpha.getValue()));
                }
                if (this.line.getValue()) {
                    RenderManager.renderBoundingBox(event.getMatrices(), new Box(this.breakPos).shrink(easeLine, easeLine, easeLine).shrink(-easeLine, -easeLine, -easeLine), this.olWidth.getValue(), first.getRgb(this.olAlpha.getValue()));
                }
            }
            if (this.secondPos != null) {
                if (this.secondPos.equals(this.breakPos) && this.ignoreSame.getValue()) {
                    return;
                }
                double ease = switch (this.renderMode.getValue()) {
                    default -> throw new IncompatibleClassChangeError();
                    case Double -> this.secondAnim.easeOutQuad();
                    case Grow -> (1.0 - this.secondAnim.easeOutQuad()) * 0.5;
                };
                double easeLine = switch (this.renderMode.getValue()) {
                    default -> throw new IncompatibleClassChangeError();
                    case Double -> this.secondAnim.easeOutQuad() + this.expandLine.getValue();
                    case Grow -> (1.0 - this.secondAnim.easeOutQuad()) * 0.5;
                };
                ColorConfig second = (ColorConfig)this.double_color;
                if (this.double_box.getValue()) {
                    RenderManager.renderBox(event.getMatrices(), new Box(this.secondPos).shrink(ease, ease, ease).shrink(-ease, -ease, -ease), second.getRgb(this.double_boxAlpha.getValue()));
                }
                if (this.double_line.getValue()) {
                    RenderManager.renderBoundingBox(event.getMatrices(), new Box(this.secondPos).shrink(easeLine, easeLine, easeLine).shrink(-easeLine, -easeLine, -easeLine), this.double_olWidth.getValue(), second.getRgb(this.double_olAlpha.getValue()));
                }
            }
        }
    }

    @EventListener(priority=-200)
    public void onPacketSend(PacketEvent.Send event) {
        if (PacketDiggingModule.nullCheck() || PacketDiggingModule.mc.player.isCreative()) {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            if (this.breakPos != null && !BlockUtil.isAir(this.breakPos) && this.subtractTime.getValue() > 0.0f && MathHelper.sqrt((float)this.breakPos.toCenterPos().squaredDistanceTo(EntityUtil.getEyesPos())) <= this.range.getValue() + 2.0f) {
                double breakTime;
                int slot = this.getTool(this.breakPos);
                if (slot == -1) {
                    slot = PacketDiggingModule.mc.player.getInventory().selectedSlot;
                }
                if ((breakTime = this.getBreakTime(this.breakPos, slot, this.damage.getValue(), this.checkGround.getValue()) - (double) this.subtractTime.getValue()) <= 0.0 || this.mineTimer.passed((long)breakTime)) {
                    ((IPlayerMoveC2SPacket) event.getPacket()).setOnGround(true);
                }
            }
            return;
        }
        if (!(event.getPacket() instanceof PlayerActionC2SPacket)) {
            return;
        }
        if (((PlayerActionC2SPacket)event.getPacket()).getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            if (this.breakPos == null || !((PlayerActionC2SPacket)event.getPacket()).getPos().equals(this.breakPos)) {
                event.cancel();
                return;
            }
            this.startMine = true;
        } else if (((PlayerActionC2SPacket)event.getPacket()).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
            if (this.breakPos == null || !((PlayerActionC2SPacket)event.getPacket()).getPos().equals(this.breakPos)) {
                event.cancel();
                return;
            }
            if (!this.instant.getValue()) {
                this.startMine = false;
            }
        }
    }

    @EventListener(priority=-100)
    public void onRotate(RotateEvent event) {
        if (PacketDiggingModule.nullCheck() || PacketDiggingModule.mc.player.isCreative()) {
            return;
        }
        if (this.endingRotate.getValue() && this.breakPos != null && !BlockUtil.isAir(this.breakPos) && this.time.getValue() > 0.0f) {
            double breakTime;
            if (MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(this.breakPos.toCenterPos())) > this.range.getValue()) {
                return;
            }
            int slot = this.getTool(this.breakPos);
            if (slot == -1) {
                slot = PacketDiggingModule.mc.player.getInventory().selectedSlot;
            }
            if ((breakTime = this.getBreakTime(this.breakPos, slot, this.damage.getValue(), this.checkGround.getValue()) - (double) this.time.getValue()) <= 0.0 || this.mineTimer.passed((long)breakTime)) {
                PacketDiggingModule.facePosFacing(this.breakPos, Managers.INTERACT.getClickDirection(this.breakPos), event);
            }
        }
    }

    public static void facePosFacing(BlockPos pos, Direction side, RotateEvent event) {
        if (pos == null || side == null || event == null) {
            return;
        }
        Vec3d hitVec = pos.toCenterPos().add(new Vec3d((double)side.getVector().getX() * 0.5, (double)side.getVector().getY() * 0.5, (double)side.getVector().getZ() * 0.5));
        PacketDiggingModule.faceVector(hitVec, event);
    }

    private static void faceVector(Vec3d vec, RotateEvent event) {
        float[] rotations = EntityUtil.getLegitRotations(vec);
        event.setRotation(rotations[0], rotations[1]);
    }

    @EventListener
    public void onAttackBlock(AttackBlockEvent event) {
        if (PacketDiggingModule.mc.player == null || PacketDiggingModule.mc.world == null || PacketDiggingModule.mc.player.isCreative()) {
            return;
        }
        event.cancel();
        if (godBlocks.contains(PacketDiggingModule.mc.world.getBlockState(event.getPos()).getBlock())) {
            return;
        }
        if (event.getPos().equals(this.breakPos)) {
            return;
        }
        this.breakPos = event.getPos();
        this.mineTimer.reset();
        this.animationTime.reset();
        this.startMine();
    }

    public void update() {
        if (PacketDiggingModule.nullCheck()) {
            return;
        }
        if (PacketDiggingModule.mc.player.isCreative()) {
            this.startMine = false;
            this.breakNumber = 0;
            this.breakPos = null;
            return;
        }
        if (this.breakPos == null) {
            this.breakNumber = 0;
            this.startMine = false;
            this.secondPos = null;
            return;
        }
        if (BlockUtil.isAir(this.breakPos)) {
            this.breakNumber = 0;
        }
        if (this.breakNumber > this.maxBreak.getValue() - 1 && this.maxBreak.getValue() > 0 || !this.wait.getValue() && BlockUtil.isAir(this.breakPos) && !this.instant.getValue()) {
            if (this.breakPos.equals(this.secondPos)) {
                this.secondPos = null;
            }
            if (this.retry.getValue()) {
                this.mineTimer.reset();
                this.animationTime.reset();
            } else {
                this.breakPos = null;
            }
            this.breakNumber = 0;
            this.startMine = false;
            return;
        }
        if (godBlocks.contains(BlockUtil.getState(this.breakPos).getBlock())) {
            this.breakPos = null;
            this.startMine = false;
            return;
        }
        if (MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(this.breakPos.toCenterPos())) > this.range.getValue()) {
            this.startMine = false;
            this.breakNumber = 0;
            this.breakPos = null;
            this.secondPos = null;
            return;
        }
        if (!(this.hotBar.getValue() || PacketDiggingModule.mc.currentScreen == null || PacketDiggingModule.mc.currentScreen instanceof ChatScreen || PacketDiggingModule.mc.currentScreen instanceof InventoryScreen || PacketDiggingModule.mc.currentScreen instanceof ClickGuiScreen)) {
            return;
        }
        int slot = this.getTool(this.breakPos);
        if (slot == -1) {
            slot = PacketDiggingModule.mc.player.getInventory().selectedSlot;
        }
        if (this.pauseEat.getValue() && PacketDiggingModule.mc.player.isUsingItem()) {
            return;
        }
        if (this.secondPos != null && this.secondTimer.passed(this.getBreakTime(this.secondPos, slot, this.failedDamage.getValue(), false))) {
            this.secondPos = null;
        }
        int secondSlot = -1;
        if (this.autoSwap.getValue()) {
            if (this.secondPos != null) {
                secondSlot = this.getTool(this.secondPos);
                if (secondSlot == -1) {
                    secondSlot = PacketDiggingModule.mc.player.getInventory().selectedSlot;
                }
                if (!this.secondPos.equals(this.breakPos) && secondSlot != PacketDiggingModule.mc.player.getInventory().selectedSlot && this.secondTimer.passed(this.getBreakTime(this.secondPos, secondSlot, this.switchDamage.getValue(), false))) {
                    if (this.lastSlot == -1) {
                        this.lastSlot = PacketDiggingModule.mc.player.getInventory().selectedSlot;
                    }
                    if (!this.clickSlot.getValue()) {
                        if (secondSlot < 9) {
                            InventoryUtil.doSwap(secondSlot);
                        }
                    } else {
                        if (secondSlot < 9) {
                            secondSlot += 36;
                        }
                        InventoryUtil.doInvSwap(secondSlot);
                    }
                }
            }
            if ((this.secondPos == null || BlockUtil.isAir(this.secondPos)) && this.lastSlot != -1 && this.lastSlot != PacketDiggingModule.mc.player.getInventory().selectedSlot) {
                if (!this.clickSlot.getValue()) {
                    InventoryUtil.doSwap(this.lastSlot);
                } else if (secondSlot != -1) {
                    InventoryUtil.doInvSwap(secondSlot);
                }
                this.lastSlot = -1;
            }
        }
        if (PacketDiggingModule.mc.player.isDead()) {
            this.secondPos = null;
        }
        if (BlockUtil.isAir(this.secondPos)) {
            this.secondPos = null;
            this.secondTimer.reset();
        }
        if (Managers.INTERACT.getClickDirection(this.breakPos) == null) {
            return;
        }
        if (this.breakPos.equals(Modules.AUTO_ANCHOR.currentPos)) {
            return;
        }
        if (BlockUtil.getBlock(this.breakPos) == Blocks.FIRE) {
            return;
        }
        if (!this.delayTimer.passed(this.delay.getValue())) {
            return;
        }
        if (this.secondPos != null && !this.secondPos.equals(this.breakPos) && this.secondTimer.passed(this.getBreakTime(this.secondPos, PacketDiggingModule.mc.player.getInventory().selectedSlot, this.stopDamage.getValue(), false))) {
            PacketDiggingModule.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.secondPos, Managers.INTERACT.getClickDirection(this.secondPos)));
        }
        if (this.startMine) {
            if (BlockUtil.isAir(this.breakPos)) {
                return;
            }
            if (Modules.CEV_BREAKER.isEnabled() && !Modules.CEV_BREAKER.isCrystal && BlockUtil.getBlock(this.breakPos) == Blocks.OBSIDIAN) {
                return;
            }
            if (this.mineTimer.passed((long)this.getBreakTime(this.breakPos, slot, this.damage.getValue(), this.checkGround.getValue()))) {
                boolean shouldSwitch;
                int old = PacketDiggingModule.mc.player.getInventory().selectedSlot;
                if (this.hotBar.getValue()) {
                    shouldSwitch = slot != old;
                } else {
                    if (slot < 9) {
                        slot += 36;
                    }
                    boolean bl = shouldSwitch = old + 36 != slot;
                }
                if (this.syncBreak.getValue() && this.secondPos != null && !this.secondPos.equals(this.breakPos)) {
                    return;
                }
                if (shouldSwitch) {
                    if (this.hotBar.getValue()) {
                        InventoryUtil.doSwap(slot);
                    } else {
                        InventoryUtil.doInvSwap(slot);
                    }
                }
                PacketDiggingModule.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.breakPos, Managers.INTERACT.getClickDirection(this.breakPos)));
                if (shouldSwitch) {
                    if (this.hotBar.getValue()) {
                        InventoryUtil.doSwap(old);
                    } else {
                        InventoryUtil.doInvSwap(slot);
                    }
                }
                ++this.breakNumber;
                this.delayTimer.reset();
            }
        } else {
            this.animationTime.setLength((long)this.getBreakTime(this.breakPos, slot, this.damage.getValue(), this.checkGround.getValue()));
            this.mineTimer.reset();
            if (this.secondPos != null && !this.secondPos.equals(this.breakPos)) {
                PacketDiggingModule.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.secondPos, Managers.INTERACT.getClickDirection(this.secondPos)));
            }
            PacketDiggingModule.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.breakPos, Managers.INTERACT.getClickDirection(this.breakPos)));
            this.delayTimer.reset();
        }
    }

    private int getTool(BlockPos pos) {
        if (this.hotBar.getValue()) {
            int index = -1;
            float CurrentFastest = 1.0f;
            for (int i = 0; i < 9; ++i) {
                float destroySpeed;
                float digSpeed;
                ItemStack stack = PacketDiggingModule.mc.player.getInventory().getStack(i);
                if (stack == ItemStack.EMPTY || !((digSpeed = (float)EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack)) + (destroySpeed = stack.getMiningSpeedMultiplier(PacketDiggingModule.mc.world.getBlockState(pos))) > CurrentFastest)) continue;
                CurrentFastest = digSpeed + destroySpeed;
                index = i;
            }
            return index;
        }
        AtomicInteger slot = new AtomicInteger();
        slot.set(-1);
        float CurrentFastest = 1.0f;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            float destroySpeed;
            float digSpeed;
            if (entry.getValue().getItem() instanceof AirBlockItem || !((digSpeed = (float)EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, entry.getValue())) + (destroySpeed = entry.getValue().getMiningSpeedMultiplier(PacketDiggingModule.mc.world.getBlockState(pos))) > CurrentFastest)) continue;
            CurrentFastest = digSpeed + destroySpeed;
            slot.set(entry.getKey());
        }
        return slot.get();
    }

    public final double getBreakTime(BlockPos pos, int slot, double damage, boolean checkGround) {
        return (double)(1.0f / this.getBlockStrength(pos, PacketDiggingModule.mc.player.getInventory().getStack(slot), checkGround) / 20.0f * 1000.0f) * damage;
    }

    public float getBlockStrength(BlockPos position, ItemStack itemStack, boolean checkGround) {
        BlockState state = PacketDiggingModule.mc.world.getBlockState(position);
        float hardness = state.getHardness(PacketDiggingModule.mc.world, position);
        if (hardness < 0.0f) {
            return 0.0f;
        }
        if (!this.canBreak(position)) {
            return this.getDigSpeed(state, itemStack, checkGround) / hardness / 100.0f;
        }
        return this.getDigSpeed(state, itemStack, checkGround) / hardness / 30.0f;
    }

    private boolean canBreak(BlockPos pos) {
        BlockState blockState = PacketDiggingModule.mc.world.getBlockState(pos);
        Block block = blockState.getBlock();
        return block.getHardness() != -1.0f;
    }

    private float getDigSpeed(BlockState state, ItemStack itemStack, boolean checkGround) {
        int efficiencyModifier;
        float digSpeed = this.getDestroySpeed(state, itemStack);
        if (digSpeed > 1.0f && (efficiencyModifier = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack)) > 0 && !itemStack.isEmpty()) {
            digSpeed += (float)(StrictMath.pow(efficiencyModifier, 2.0) + 1.0);
        }
        if (PacketDiggingModule.mc.player.hasStatusEffect(StatusEffects.HASTE)) {
            digSpeed *= 1.0f + (float)(PacketDiggingModule.mc.player.getStatusEffect(StatusEffects.HASTE).getAmplifier() + 1) * 0.2f;
        }
        if (PacketDiggingModule.mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            digSpeed *= (switch (PacketDiggingModule.mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            });
        }
        if (PacketDiggingModule.mc.player.isSubmergedInWater() && !EnchantmentHelper.hasAquaAffinity(PacketDiggingModule.mc.player)) {
            digSpeed /= 5.0f;
        }
        if (!PacketDiggingModule.mc.player.isOnGround() && checkGround) {
            digSpeed /= 5.0f;
        }
        return digSpeed < 0.0f ? 0.0f : digSpeed;
    }

    public float getDestroySpeed(BlockState state, ItemStack itemStack) {
        float destroySpeed = 1.0f;
        if (itemStack != null && !itemStack.isEmpty()) {
            destroySpeed *= itemStack.getMiningSpeedMultiplier(state);
        }
        return destroySpeed;
    }

    private void startMine() {
        if (this.startingRotate.getValue()) {
            this.setRotation(this.breakPos, Managers.INTERACT.getClickDirection(this.breakPos));
        }
        PacketDiggingModule.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.breakPos, Managers.INTERACT.getClickDirection(this.breakPos)));
        if (this.doubleBreak.getValue()) {
            if (this.secondPos == null || BlockUtil.isAir(this.secondPos)) {
                int slot = this.getTool(this.breakPos);
                if (slot == -1) {
                    slot = PacketDiggingModule.mc.player.getInventory().selectedSlot;
                }
                double breakTime = this.getBreakTime(this.breakPos, slot, 1.0, false);
                this.secondAnim.reset();
                this.secondAnim.setLength((long)breakTime);
                this.secondTimer.reset();
                this.secondPos = this.breakPos;
            }
            PacketDiggingModule.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.breakPos, Managers.INTERACT.getClickDirection(this.breakPos)));
            PacketDiggingModule.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.breakPos, Managers.INTERACT.getClickDirection(this.breakPos)));
        }
        this.breakNumber = 0;
    }

    public void mine(BlockPos pos) {
        if (PacketDiggingModule.nullCheck() || PacketDiggingModule.mc.player.isCreative()) {
            return;
        }
        if (!Modules.PACKET_DIGGING.isEnabled()) {
            return;
        }
        if (godBlocks.contains(PacketDiggingModule.mc.world.getBlockState(pos).getBlock())) {
            return;
        }
        if (pos.equals(this.breakPos)) {
            return;
        }
        if (this.breakPos != null && BlockUtil.getBlock(this.breakPos) == Blocks.COBWEB) {
            return;
        }
        this.breakPos = pos;
        this.mineTimer.reset();
        this.animationTime.reset();
        this.startMine();
    }

    public enum RenderMode {
        Grow,
        Double

    }
}
