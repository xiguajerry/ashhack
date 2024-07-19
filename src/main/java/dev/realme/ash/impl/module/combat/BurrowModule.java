package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.RunTickEvent;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.chat.ChatUtil;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockPosX;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import java.util.ArrayList;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BurrowModule
extends RotationModule {
    final Config<InventoryUtil.SwapMode> swapMode = new EnumConfig<>("SwapMode", "", InventoryUtil.SwapMode.SILENT, InventoryUtil.SwapMode.values());
    final Config<Boolean> wait = new BooleanConfig("Wait", "", false);
    final Config<Boolean> noSelfPos = new BooleanConfig("NoSelfPos", "", false);
    final Config<Integer> multiPlace = new NumberConfig<>("MultiPlace", "", 0, 1, 5);
    final Config<RotateMode> rotateMode = new EnumConfig<>("RotateMode", "", RotateMode.Down, RotateMode.values());
    final Config<LagBackMode> lagBackMode = new EnumConfig<>("LagBackMode", "", LagBackMode.Troll, LagBackMode.values());
    final Config<Boolean> swing = new BooleanConfig("Swing", "", false);
    int progress = 0;
    int slot;
    Vec3d rotatePos;
    BlockPos placePos;
    public boolean cancelRotate = false;

    public BurrowModule() {
        super("Burrow", "Rubberband clips you into a block", ModuleCategory.COMBAT, 1200);
    }

    @Override
    public void onDisable() {
        this.cancelRotate = false;
        this.rotatePos = null;
        this.placePos = null;
    }

    @EventListener
    public void onRunTick(RunTickEvent event) {
        this.update();
    }

    @EventListener
    public void onTick(PlayerTickEvent event) {
        this.update();
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        this.update();
    }

    private void update() {
        if (BurrowModule.mc.player == null || BurrowModule.mc.world == null) {
            return;
        }
        this.progress = 0;
        if (!BurrowModule.mc.player.isOnGround()) {
            return;
        }
        this.slot = this.getBlock();
        if (this.slot == -1) {
            ChatUtil.sendChatMessageWidthId("No Item.", this.hashCode());
            this.disable();
            return;
        }
        BlockPos selfPos = PlayerUtil.playerPos(BurrowModule.mc.player);
        BlockPosX pos1 = new BlockPosX(BurrowModule.mc.player.getX() + 0.3, BurrowModule.mc.player.getY() + 0.5, BurrowModule.mc.player.getZ() + 0.3);
        BlockPosX pos2 = new BlockPosX(BurrowModule.mc.player.getX() - 0.3, BurrowModule.mc.player.getY() + 0.5, BurrowModule.mc.player.getZ() + 0.3);
        BlockPosX pos3 = new BlockPosX(BurrowModule.mc.player.getX() + 0.3, BurrowModule.mc.player.getY() + 0.5, BurrowModule.mc.player.getZ() - 0.3);
        BlockPosX pos4 = new BlockPosX(BurrowModule.mc.player.getX() - 0.3, BurrowModule.mc.player.getY() + 0.5, BurrowModule.mc.player.getZ() - 0.3);
        if (!(this.canPlace(pos1) || this.canPlace(pos2) || this.canPlace(pos3) || this.canPlace(pos4))) {
            if (!this.wait.getValue()) {
                this.disable();
            }
            return;
        }
        if (this.canPlace(pos1) && !this.canPlace(pos2) && !this.canPlace(pos3) && !this.canPlace(pos4)) {
            this.rotatePos = pos1.toCenterPos();
        }
        if (!this.canPlace(pos1) && this.canPlace(pos2) && !this.canPlace(pos3) && !this.canPlace(pos4)) {
            this.rotatePos = pos2.toCenterPos();
        }
        if (!this.canPlace(pos1) && !this.canPlace(pos2) && this.canPlace(pos3) && !this.canPlace(pos4)) {
            this.rotatePos = pos3.toCenterPos();
        }
        if (!this.canPlace(pos1) && !this.canPlace(pos2) && !this.canPlace(pos3) && this.canPlace(pos4)) {
            this.rotatePos = pos4.toCenterPos();
        }
        if (this.canPlace(pos1) && this.canPlace(pos2) && !this.canPlace(pos3) && !this.canPlace(pos4)) {
            this.rotatePos = new Vec3d(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 0.5, BurrowModule.mc.player.getZ() + 0.3);
        }
        if (!this.canPlace(pos1) && !this.canPlace(pos2) && this.canPlace(pos3) && this.canPlace(pos4)) {
            this.rotatePos = new Vec3d(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 0.5, BurrowModule.mc.player.getZ() - 0.3);
        }
        if (this.canPlace(pos1) && !this.canPlace(pos2) && !this.canPlace(pos3) && !this.canPlace(pos4)) {
            this.placePos = pos1;
        }
        if (!this.canPlace(pos1) && this.canPlace(pos2) && !this.canPlace(pos3) && !this.canPlace(pos4)) {
            this.placePos = pos2;
        }
        if (!this.canPlace(pos1) && !this.canPlace(pos2) && this.canPlace(pos3) && !this.canPlace(pos4)) {
            this.placePos = pos3;
        }
        if (!this.canPlace(pos1) && !this.canPlace(pos2) && !this.canPlace(pos3) && this.canPlace(pos4)) {
            this.placePos = pos4;
        }
        if (this.canPlace(pos1) && this.canPlace(pos2) && !this.canPlace(pos3) && !this.canPlace(pos4)) {
            this.placePos = new BlockPosX(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 0.5, BurrowModule.mc.player.getZ() + 0.3);
        }
        if (!this.canPlace(pos1) && !this.canPlace(pos2) && this.canPlace(pos3) && this.canPlace(pos4)) {
            this.placePos = new BlockPosX(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 0.5, BurrowModule.mc.player.getZ() - 0.3);
        }
        if (PlayerUtil.isInWeb(BurrowModule.mc.player)) {
            return;
        }
        BlockPos headPos = PlayerUtil.playerPos(BurrowModule.mc.player).up(2);
        if (BurrowModule.mc.player.isInSneakingPose() || this.Trapped(headPos) || this.Trapped(headPos.add(1, 0, 0)) || this.Trapped(headPos.add(-1, 0, 0)) || this.Trapped(headPos.add(0, 0, 1)) || this.Trapped(headPos.add(0, 0, -1)) || this.Trapped(headPos.add(1, 0, -1)) || this.Trapped(headPos.add(-1, 0, -1)) || this.Trapped(headPos.add(1, 0, 1)) || this.Trapped(headPos.add(-1, 0, 1))) {
            BlockPos finPos = this.placePos == null ? selfPos : this.placePos;
            Vec3d offVec = this.getVec3dDirection(finPos);
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX() + offVec.x, BurrowModule.mc.player.getY() + 0.2, BurrowModule.mc.player.getZ() + offVec.z, false));
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX() + offVec.x, BurrowModule.mc.player.getY() + 0.2, BurrowModule.mc.player.getZ() + offVec.z, false));
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX() + offVec.x, BurrowModule.mc.player.getY() + 0.2, BurrowModule.mc.player.getZ() + offVec.z, false));
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX() + offVec.x, BurrowModule.mc.player.getY() + 0.2, BurrowModule.mc.player.getZ() + offVec.z, false));
        } else {
            BurrowModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 0.4199999868869781, BurrowModule.mc.player.getZ(), false));
            BurrowModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 0.7531999805212017, BurrowModule.mc.player.getZ(), false));
            BurrowModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 0.9999957640154541, BurrowModule.mc.player.getZ(), false));
            BurrowModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 1.1661092609382138, BurrowModule.mc.player.getZ(), false));
        }
        EntityUtil.attackCrystal(pos1);
        EntityUtil.attackCrystal(pos2);
        EntityUtil.attackCrystal(pos3);
        EntityUtil.attackCrystal(pos4);
        int oldSlot = BurrowModule.mc.player.getInventory().selectedSlot;
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                InventoryUtil.doSwap(this.slot);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.slot);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.slot);
            }
        }
        if (this.rotateMode.getValue().equals(RotateMode.Test)) {
            if (this.rotatePos == null) {
                this.cancelRotate = true;
                this.setRotation(BurrowModule.mc.player.getYaw(), 90.0f);
            } else {
                this.cancelRotate = true;
                this.setRotation(this.rotatePos, true);
            }
        }
        if (this.rotateMode.getValue().equals(RotateMode.Down)) {
            this.cancelRotate = true;
            this.setRotation(Managers.ROTATION.lastYaw, 90.0f);
        }
        if (this.canPlace(pos1)) {
            this.doPlace(pos1);
        }
        if (this.canPlace(pos2)) {
            this.doPlace(pos2);
        }
        if (this.canPlace(pos3)) {
            this.doPlace(pos3);
        }
        if (this.canPlace(pos4)) {
            this.doPlace(pos4);
        }
        switch (this.swapMode.getValue()) {
            case SILENT: {
                InventoryUtil.doSwap(oldSlot);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.slot);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.slot);
            }
        }
        this.doLagBack();
        this.disable();
    }

    public Vec3d getVec3dDirection(BlockPos burBlockPos) {
        assert (BurrowModule.mc.player != null && BurrowModule.mc.world != null);
        BlockPos playerPos = PlayerUtil.playerPos(BurrowModule.mc.player);
        Vec3d centerPos = burBlockPos.toCenterPos();
        Vec3d subtracted = BurrowModule.mc.player.getPos().subtract(centerPos);
        Vec3d off = Vec3d.ZERO;
        if (Math.abs(subtracted.x) >= Math.abs(subtracted.z) && Math.abs(subtracted.x) > 0.2) {
            off = subtracted.x > 0.0 ? new Vec3d(0.8 - subtracted.x, 0.0, 0.0) : new Vec3d(-0.8 - subtracted.x, 0.0, 0.0);
        } else if (Math.abs(subtracted.z) >= Math.abs(subtracted.x) && Math.abs(subtracted.z) > 0.2) {
            off = subtracted.z > 0.0 ? new Vec3d(0.0, 0.0, 0.8 - subtracted.z) : new Vec3d(0.0, 0.0, -0.8 - subtracted.z);
        } else if (burBlockPos.equals(playerPos)) {
            ArrayList<Direction> facList = new ArrayList<>();
            for (Direction dir : Direction.values()) {
                if (dir == Direction.UP || dir == Direction.DOWN || BlockUtil.solid(playerPos.offset(dir)) || BlockUtil.solid(playerPos.offset(dir).offset(Direction.UP))) continue;
                facList.add(dir);
            }
            Vec3d vec3d = Vec3d.ZERO;
            Vec3d[] offVec1 = new Vec3d[1];
            Vec3d[] offVec2 = new Vec3d[1];
            facList.sort((dir1, dir2) -> {
                offVec1[0] = vec3d.add(new Vec3d(dir1.getOffsetX(), dir1.getOffsetY(), dir1.getOffsetZ()).multiply(0.5));
                offVec2[0] = vec3d.add(new Vec3d(dir2.getOffsetX(), dir2.getOffsetY(), dir2.getOffsetZ()).multiply(0.5));
                return (int)(Math.sqrt(BurrowModule.mc.player.squaredDistanceTo(offVec1[0].x, BurrowModule.mc.player.getY(), offVec1[0].z)) - Math.sqrt(BurrowModule.mc.player.squaredDistanceTo(offVec2[0].x, BurrowModule.mc.player.getY(), offVec2[0].z)));
            });
            if (!facList.isEmpty()) {
                off = new Vec3d(facList.get(0).getOffsetX(), facList.get(0).getOffsetY(), facList.get(0).getOffsetZ());
            }
        }
        return off;
    }

    public double ez() {
        if (BurrowModule.mc.world.getBlockState(new BlockPosX(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 3.0, BurrowModule.mc.player.getZ())).getBlock() != Blocks.AIR) {
            return 1.2;
        }
        double lol = 2.2;
        for (int i = 4; i < 13; ++i) {
            if (BurrowModule.mc.world.getBlockState(new BlockPosX(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + (double)i, BurrowModule.mc.player.getZ())).getBlock() == Blocks.AIR) continue;
            return lol + (double)i - 4.0;
        }
        return 10.0;
    }

    private void doLagBack() {
        if (PlayerUtil.isInWeb(BurrowModule.mc.player)) {
            return;
        }
        assert (BurrowModule.mc.player != null && BurrowModule.mc.world != null);
        BlockPos selfPos = PlayerUtil.playerPos(BurrowModule.mc.player);
        switch (this.lagBackMode.getValue()) {
            case Rotate: {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(-BurrowModule.mc.player.getYaw(), -BurrowModule.mc.player.getPitch(), Managers.POSITION.isOnGround()));
                break;
            }
            case Troll: {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 1.9400880035762786, BurrowModule.mc.player.getZ(), Managers.POSITION.isOnGround()));
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() - 1.0, BurrowModule.mc.player.getZ(), Managers.POSITION.isOnGround()));
                break;
            }
            case OBS: {
                for (int i = 10; i > 0; --i) {
                    if (!BlockUtil.isAir(selfPos.add(0, i, 0)) || !BlockUtil.isAir(selfPos.add(0, i, 0).up())) continue;
                    BlockPos lagPos = selfPos.add(0, i, 0);
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(lagPos.getX(), lagPos.getY(), lagPos.getZ(), Managers.POSITION.isOnGround()));
                }
                break;
            }
            case XIN: {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + this.ez(), BurrowModule.mc.player.getZ(), Managers.POSITION.isOnGround()));
                break;
            }
            case OLD: {
                double distance = 0.0;
                BlockPos bestPos = null;
                for (int i = 0; i < 10; ++i) {
                    BlockPos pos = PlayerUtil.playerPos(BurrowModule.mc.player).up(i);
                    if (!this.canGoto(pos) || MathHelper.sqrt((float)BurrowModule.mc.player.squaredDistanceTo(pos.toCenterPos())) < 2.0f || bestPos != null && !(BurrowModule.mc.player.squaredDistanceTo(pos.toCenterPos()) < distance)) continue;
                    bestPos = pos;
                    distance = BurrowModule.mc.player.squaredDistanceTo(pos.toCenterPos());
                }
                if (bestPos == null) break;
                BurrowModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround((double)bestPos.getX() + 0.5, bestPos.getY(), (double)bestPos.getZ() + 0.5, false));
            }
        }
    }

    private int getBlock() {
        return switch (this.swapMode.getValue()) {
            default -> throw new IncompatibleClassChangeError();
            case OFF -> -1;
            case Pick -> this.slot = InventoryUtil.findInventorySlot(Items.OBSIDIAN, true) != -1 ? InventoryUtil.findInventorySlot(Items.OBSIDIAN, true) : InventoryUtil.findInventorySlot(Items.ENDER_CHEST, true);
            case Inventory -> this.slot = InventoryUtil.findInventorySlot(Items.OBSIDIAN, false) != -1 ? InventoryUtil.findInventorySlot(Items.OBSIDIAN, false) : InventoryUtil.findInventorySlot(Items.ENDER_CHEST, false);
            case SILENT, NORMAL -> this.slot = InventoryUtil.findItem(Items.OBSIDIAN) != -1 ? InventoryUtil.findItem(Items.OBSIDIAN) : InventoryUtil.findItem(Items.ENDER_CHEST);
        };
    }

    private boolean canPlace(BlockPos pos) {
        if (Managers.INTERACT.getPlaceDirection(pos) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !this.hasEntity(pos);
    }

    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : BurrowModule.mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
            if (entity == BurrowModule.mc.player || !entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity) continue;
            return true;
        }
        return false;
    }

    private void doPlace(BlockPos pos) {
        if (this.noSelfPos.getValue() && pos.equals(BurrowModule.mc.player.getBlockPos())) {
            return;
        }
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        if (pos.equals(Modules.PACKET_DIGGING.breakPos)) {
            Modules.PACKET_DIGGING.breakPos = null;
            Modules.PACKET_DIGGING.startMine = false;
            Modules.PACKET_DIGGING.breakNumber = 0;
        }
        Managers.INTERACT.placeBlock(pos, this.rotateMode.getValue() == RotateMode.Normal, this.swing.getValue());
        ++this.progress;
    }

    private boolean Trapped(BlockPos pos) {
        return !BlockUtil.isAir(pos) && this.checkSelf(pos.down(2));
    }

    private boolean checkSelf(BlockPos pos) {
        return BurrowModule.mc.player.getBoundingBox().intersects(new Box(pos));
    }

    private void gotoPos(BlockPos offPos) {
        if (Math.abs((double)offPos.getX() + 0.5 - BurrowModule.mc.player.getX()) < Math.abs((double)offPos.getZ() + 0.5 - BurrowModule.mc.player.getZ())) {
            BurrowModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX(), BurrowModule.mc.player.getY() + 0.2, BurrowModule.mc.player.getZ() + ((double)offPos.getZ() + 0.5 - BurrowModule.mc.player.getZ()), true));
        } else {
            BurrowModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(BurrowModule.mc.player.getX() + ((double)offPos.getX() + 0.2 - BurrowModule.mc.player.getX()), BurrowModule.mc.player.getY() + 0.2, BurrowModule.mc.player.getZ(), true));
        }
    }

    private boolean canGoto(BlockPos pos) {
        return !BlockUtil.getState(pos).blocksMovement() && !BlockUtil.getState(pos.up()).blocksMovement();
    }

    public enum RotateMode {
        Test,
        Down,
        Normal,
        None

    }

    public enum LagBackMode {
        OBS,
        XIN,
        OLD,
        Troll,
        Rotate,
        None

    }
}
