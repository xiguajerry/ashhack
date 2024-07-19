package dev.realme.ash.impl.module.movement;

import com.mojang.authlib.GameProfile;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.Interpolation;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.DisconnectEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.chat.ChatUtil;
import dev.realme.ash.util.math.DamageUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockPosX;
import dev.realme.ash.util.world.EntityUtil;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class FakeLagModule
extends ToggleModule {
    Config<Boolean> checkVelocityUpdate = new BooleanConfig("CheckVelocityUpdate", "", false);
    Config<Boolean> checkExplosion = new BooleanConfig("CheckExplosion", "", false);
    Config<Float> range = new NumberConfig<Float>("Range", "", Float.valueOf(0.0f), Float.valueOf(6.0f), Float.valueOf(20.0f));
    Config<Boolean> checkDamage = new BooleanConfig("CheckDamage", "", false);
    Config<Double> maxSelfDamage = new NumberConfig<Double>("MaxSelfDamage", "", 0.0, 6.0, 36.0);
    Config<Boolean> pulse = new BooleanConfig("Pulse", "Releases packets at intervals", false);
    Config<Float> factor = new NumberConfig<Float>("Factor", "The factor for packet intervals", Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(10.0f), () -> this.pulse.getValue());
    Config<Boolean> render = new BooleanConfig("Render", "Renders the serverside player position", true);
    Config<Boolean> HandSwingC2SPacket = new BooleanConfig("HandSwingC2SPacket", "", true);
    Config<Boolean> PlayerActionC2SPacket = new BooleanConfig("PlayerActionC2SPacket", "", true);
    Config<Boolean> PlayerMoveC2SPacket = new BooleanConfig("PlayerMoveC2SPacket", "", true);
    Config<Boolean> ClientCommandC2SPacket = new BooleanConfig("ClientCommandC2SPacket", "", true);
    Config<Boolean> PlayerInteractEntityC2SPacket = new BooleanConfig("PlayerInteractEntityC2SPacket", "", true);
    Config<Boolean> PlayerInteractBlockC2SPacket = new BooleanConfig("PlayerInteractBlockC2SPacket", "", true);
    Config<Boolean> PlayerInteractItemC2SPacket = new BooleanConfig("PlayerInteractItemC2SPacket", "", true);
    Config<Integer> boxAlpha = new NumberConfig<Integer>("BoxAlpha", "", 0, 80, 255);
    Config<Integer> olAlpha = new NumberConfig<Integer>("OLAlpha", "", 0, 80, 255);
    Config<Float> olWidth = new NumberConfig<Float>("OLWidth", "", Float.valueOf(0.1f), Float.valueOf(1.5f), Float.valueOf(5.0f));
    Config<Boolean> debug = new BooleanConfig("debug", "", false);
    PlayerEntity lastEntity = null;
    BlockPos explosionPos = null;
    private boolean blinking;
    private final Queue<Packet<?>> packets = new LinkedBlockingQueue();

    public FakeLagModule() {
        super("FakeLag", "Withholds packets from the server, creating clientside lag", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable() {
        this.lastEntity = new PlayerEntity(FakeLagModule.mc.world, PlayerUtil.playerPos(FakeLagModule.mc.player).down(), FakeLagModule.mc.player.getYaw(), new GameProfile(UUID.fromString("66123666-6666-6666-6666-667563866600"), "zjmisgay")){

            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (this.render.getValue().booleanValue()) {
            if (this.lastEntity != null) {
                RenderManager.renderBox(event.getMatrices(), Interpolation.getInterpolatedEntityBox(this.lastEntity), Modules.CLIENT_SETTING.getRGB(this.boxAlpha.getValue()));
                RenderManager.renderBoundingBox(event.getMatrices(), Interpolation.getInterpolatedEntityBox(this.lastEntity), this.olWidth.getValue().floatValue(), Modules.CLIENT_SETTING.getRGB(this.olAlpha.getValue()));
            }
            if (this.explosionPos != null && this.debug.getValue().booleanValue()) {
                RenderManager.renderBox(event.getMatrices(), this.explosionPos, Modules.CLIENT_SETTING.getRGB(this.boxAlpha.getValue()));
                RenderManager.renderBoundingBox(event.getMatrices(), this.explosionPos, this.olWidth.getValue().floatValue(), Modules.CLIENT_SETTING.getRGB(this.olAlpha.getValue()));
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        Object packet;
        if (FakeLagModule.mc.player == null || FakeLagModule.mc.world == null) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ExplosionS2CPacket) {
            packet = (ExplosionS2CPacket)((Object)packet2);
            if (this.checkExplosion.getValue().booleanValue()) {
                this.explosionPos = new BlockPosX(((ExplosionS2CPacket)packet).getX(), ((ExplosionS2CPacket)packet).getY(), ((ExplosionS2CPacket)packet).getZ());
                if (MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(this.explosionPos.toCenterPos())) > this.range.getValue().floatValue()) {
                    return;
                }
                if (this.lastEntity == null) {
                    return;
                }
                float damage = DamageUtil.getCrystalDamage(this.explosionPos.toCenterPos(), this.lastEntity);
                if ((double)damage > this.maxSelfDamage.getValue() || !this.checkDamage.getValue().booleanValue()) {
                    this.blinking = true;
                    if (!this.packets.isEmpty()) {
                        for (Packet packet3 : this.packets) {
                            Managers.NETWORK.sendPacket(packet3);
                            this.lastEntity = new PlayerEntity(FakeLagModule.mc.world, PlayerUtil.playerPos(FakeLagModule.mc.player).down(), FakeLagModule.mc.player.getYaw(), new GameProfile(UUID.fromString("66123666-6666-6666-6666-667563866600"), "zjmisgay")){

                                @Override
                                public boolean isSpectator() {
                                    return false;
                                }

                                @Override
                                public boolean isCreative() {
                                    return false;
                                }
                            };
                        }
                        if (this.debug.getValue().booleanValue()) {
                            ChatUtil.clientSendMessage("Explosion " + damage);
                        }
                    }
                    this.packets.clear();
                    this.blinking = false;
                }
            }
        }
        if ((packet2 = event.getPacket()) instanceof EntityVelocityUpdateS2CPacket) {
            packet = (EntityVelocityUpdateS2CPacket)((Object)packet2);
            if (this.checkVelocityUpdate.getValue().booleanValue()) {
                if (((EntityVelocityUpdateS2CPacket)packet).getId() != FakeLagModule.mc.player.getId()) {
                    return;
                }
                this.blinking = true;
                if (!this.packets.isEmpty()) {
                    for (Packet packet4 : this.packets) {
                        Managers.NETWORK.sendPacket(packet4);
                        this.lastEntity = new PlayerEntity(FakeLagModule.mc.world, PlayerUtil.playerPos(FakeLagModule.mc.player).down(), FakeLagModule.mc.player.getYaw(), new GameProfile(UUID.fromString("66123666-6666-6666-6666-667563866600"), "zjmisgay")){

                            @Override
                            public boolean isSpectator() {
                                return false;
                            }

                            @Override
                            public boolean isCreative() {
                                return false;
                            }
                        };
                    }
                    if (this.debug.getValue().booleanValue()) {
                        ChatUtil.clientSendMessage("VelocityUpdate");
                    }
                }
                this.packets.clear();
                this.blinking = false;
            }
        }
    }

    @Override
    public void onDisable() {
        if (FakeLagModule.mc.player == null) {
            return;
        }
        if (!this.packets.isEmpty()) {
            for (Packet packet : this.packets) {
                Managers.NETWORK.sendPacket(packet);
            }
        }
        this.packets.clear();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE && this.pulse.getValue().booleanValue() && (float)this.packets.size() > this.factor.getValue().floatValue() * 10.0f) {
            this.blinking = true;
            if (!this.packets.isEmpty()) {
                for (Packet packet : this.packets) {
                    Managers.NETWORK.sendPacket(packet);
                    this.lastEntity = new PlayerEntity(FakeLagModule.mc.world, FakeLagModule.mc.player.getBlockPos().down(), FakeLagModule.mc.player.getYaw(), new GameProfile(UUID.fromString("66123666-6666-6666-6666-667563866600"), "zjmisgay")){

                        @Override
                        public boolean isSpectator() {
                            return false;
                        }

                        @Override
                        public boolean isCreative() {
                            return false;
                        }
                    };
                }
                if (this.debug.getValue().booleanValue()) {
                    ChatUtil.clientSendMessage("Pulse");
                }
            }
            this.packets.clear();
            this.blinking = false;
        }
    }

    @EventListener
    public void onDisconnectEvent(DisconnectEvent event) {
        this.disable();
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        if (FakeLagModule.mc.player == null || FakeLagModule.mc.player.isRiding() || this.blinking) {
            return;
        }
        if (event.getPacket() instanceof PlayerActionC2SPacket && this.PlayerActionC2SPacket.getValue() != false || event.getPacket() instanceof PlayerMoveC2SPacket && this.PlayerMoveC2SPacket.getValue() != false || event.getPacket() instanceof ClientCommandC2SPacket && this.ClientCommandC2SPacket.getValue() != false || event.getPacket() instanceof HandSwingC2SPacket && this.HandSwingC2SPacket.getValue() != false || event.getPacket() instanceof PlayerInteractEntityC2SPacket && this.PlayerInteractEntityC2SPacket.getValue() != false || event.getPacket() instanceof PlayerInteractBlockC2SPacket && this.PlayerInteractBlockC2SPacket.getValue() != false || event.getPacket() instanceof PlayerInteractItemC2SPacket && this.PlayerInteractItemC2SPacket.getValue().booleanValue()) {
            event.cancel();
            this.packets.add(event.getPacket());
        }
    }
}
