package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.impl.event.world.AddEntityEvent;
import dev.realme.ash.impl.event.world.RemoveEntityEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.player.RotationUtil;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ClickCrystalModule
extends RotationModule {
    Config<Float> breakDelayConfig = new NumberConfig<Float>("SpawnDelay", "Speed to break crystals after spawning", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(20.0f));
    Config<Float> randomDelayConfig = new NumberConfig<Float>("RandomDelay", "Randomized break delay", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(5.0f));
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotate before breaking", false);
    Config<Boolean> randomRotateConfig = new BooleanConfig("Rotate-Random", "Slightly randomizes rotations", false, () -> this.rotateConfig.getValue());
    private final Set<BlockPos> placedCrystals = new HashSet<BlockPos>();
    private final Map<EndCrystalEntity, Long> spawnedCrystals = new LinkedHashMap<EndCrystalEntity, Long>();
    private float randomDelay = -1.0f;

    public ClickCrystalModule() {
        super("ClickCrystal", "Automatically breaks placed crystals", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (this.spawnedCrystals.isEmpty()) {
            return;
        }
        Map.Entry<EndCrystalEntity, Long> e = this.spawnedCrystals.entrySet().iterator().next();
        EndCrystalEntity crystalEntity = e.getKey();
        Long time = e.getValue();
        if (this.randomDelay == -1.0f) {
            this.randomDelay = this.randomDelayConfig.getValue().floatValue() == 0.0f ? 0.0f : RANDOM.nextFloat(this.randomDelayConfig.getValue().floatValue() * 25.0f);
        }
        float breakDelay = this.breakDelayConfig.getValue().floatValue() * 50.0f + this.randomDelay;
        if (ClickCrystalModule.mc.player.getEyePos().squaredDistanceTo(crystalEntity.getPos()) <= 12.25 && (float)(System.currentTimeMillis() - time) >= breakDelay) {
            if (this.rotateConfig.getValue().booleanValue()) {
                Vec3d rotatePos = crystalEntity.getPos();
                if (this.randomRotateConfig.getValue().booleanValue()) {
                    Box bb = crystalEntity.getBoundingBox();
                    rotatePos = new Vec3d(RANDOM.nextDouble(bb.minX, bb.maxX), RANDOM.nextDouble(bb.minY, bb.maxY), RANDOM.nextDouble(bb.minZ, bb.maxZ));
                }
                float[] rotations = RotationUtil.getRotationsTo(ClickCrystalModule.mc.player.getEyePos(), rotatePos);
                this.setRotation(rotations[0], rotations[1]);
            }
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, ClickCrystalModule.mc.player.isSneaking()));
            ClickCrystalModule.mc.player.swingHand(Hand.MAIN_HAND);
            this.randomDelay = -1.0f;
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerInteractBlockC2SPacket) {
            PlayerInteractBlockC2SPacket packet2 = (PlayerInteractBlockC2SPacket)packet;
            if (!event.isClientPacket() && ClickCrystalModule.mc.player.getStackInHand(packet2.getHand()).getItem() instanceof EndCrystalItem) {
                this.placedCrystals.add(packet2.getBlockHitResult().getBlockPos());
            }
        }
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event) {
        EndCrystalEntity crystalEntity;
        BlockPos base;
        Entity entity = event.getEntity();
        if (entity instanceof EndCrystalEntity && this.placedCrystals.contains(base = (crystalEntity = (EndCrystalEntity)entity).getBlockPos().down())) {
            this.spawnedCrystals.put(crystalEntity, System.currentTimeMillis());
            this.placedCrystals.remove(base);
        }
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EndCrystalEntity) {
            EndCrystalEntity crystalEntity = (EndCrystalEntity)entity;
            this.spawnedCrystals.remove(crystalEntity);
        }
    }
}
