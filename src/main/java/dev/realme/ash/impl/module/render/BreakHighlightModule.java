package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.world.EntityUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class BreakHighlightModule
extends ToggleModule {
    final Config<Boolean> text = new BooleanConfig("Text", "", true);
    final Config<Color> color = new ColorConfig("Color", "", new Color(255, 255, 255), false, false);
    final Config<Boolean> box = new BooleanConfig("Box", "", true);
    final Config<Integer> boxAlpha = new NumberConfig<>("BoxAlpha", "", 0, 80, 255);
    final Config<Boolean> line = new BooleanConfig("lines", "", true);
    final Config<Integer> olAlpha = new NumberConfig<>("OLAlpha", "", 0, 255, 255);
    final Config<Float> olWidth = new NumberConfig<>("OLWidth", "", 0.1f, 1.5f, 5.0f);
    final Config<Color> friend_color = new ColorConfig("Friend_Color", "", new Color(0, 150, 255), false, false);
    final Config<Boolean> friend_box = new BooleanConfig("Friend_Box", "", true);
    final Config<Integer> friend_boxAlpha = new NumberConfig<>("Friend_BoxAlpha", "", 0, 80, 255);
    final Config<Boolean> friend_line = new BooleanConfig("Friend_lines", "", true);
    final Config<Integer> friend_olAlpha = new NumberConfig<>("Friend_OLAlpha", "", 0, 255, 255);
    final Config<Float> friend_olWidth = new NumberConfig<>("Friend_OLWidth", "", 0.1f, 1.5f, 5.0f);
    final Config<Float> range = new NumberConfig<>("Range", "", 5.0f, 20.0f, 50.0f);
    Render render = null;
    private final List<Render> renders = new ArrayList<>();

    public BreakHighlightModule() {
        super("BreakHighlight", "Highlights blocks that are being broken", ModuleCategory.RENDER);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        if (BreakHighlightModule.mc.player == null || BreakHighlightModule.mc.world == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof BlockBreakingProgressS2CPacket) {
            BlockBreakingProgressS2CPacket packet2 = (BlockBreakingProgressS2CPacket) packet;
            this.render = new Render(packet2.getPos(), packet2.getEntityId());
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (BreakHighlightModule.mc.player == null || BreakHighlightModule.mc.world == null) {
            return;
        }
        if (this.render != null && this.contains()) {
            this.render = null;
        }
        this.renders.removeIf(r -> this.render != null && r.id == this.render.id);
        if (this.render != null) {
            this.renders.add(this.render);
            this.render = null;
        }
        this.renders.forEach(r -> {
            PlayerEntity player;
            if (MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(r.pos.toCenterPos())) > this.range.getValue()) {
                return;
            }
            Entity entity = BreakHighlightModule.mc.world.getEntityById(r.id);
            PlayerEntity playerEntity = player = entity == null ? null : (PlayerEntity)entity;
            if (player == null) {
                return;
            }
            String name = player.getGameProfile().getName();
            if (Managers.SOCIAL.isFriend(player.getName())) {
                ColorConfig friend = (ColorConfig)this.friend_color;
                if (this.text.getValue()) {
                    RenderManager.post(() -> RenderManager.renderSign(Formatting.AQUA + name, r.pos.toCenterPos()));
                }
                if (this.friend_box.getValue()) {
                    RenderManager.renderBox(event.getMatrices(), r.pos, friend.getRgb(this.friend_boxAlpha.getValue()));
                }
                if (this.friend_line.getValue()) {
                    RenderManager.renderBoundingBox(event.getMatrices(), r.pos, this.friend_olWidth.getValue(), friend.getRgb(this.friend_olAlpha.getValue()));
                }
            } else {
                ColorConfig sb = (ColorConfig)this.color;
                if (this.text.getValue()) {
                    RenderManager.post(() -> RenderManager.renderSign(name, r.pos.toCenterPos()));
                }
                if (this.box.getValue()) {
                    RenderManager.renderBox(event.getMatrices(), r.pos, sb.getRgb(this.boxAlpha.getValue()));
                }
                if (this.line.getValue()) {
                    RenderManager.renderBoundingBox(event.getMatrices(), r.pos, this.olWidth.getValue(), sb.getRgb(this.olAlpha.getValue()));
                }
            }
        });
    }

    private boolean contains() {
        for (Render r : this.renders) {
            if (r.id != this.render.id || !r.pos.equals(this.render.pos)) continue;
            return true;
        }
        return false;
    }

    private record Render(BlockPos pos, int id) {
    }
}
