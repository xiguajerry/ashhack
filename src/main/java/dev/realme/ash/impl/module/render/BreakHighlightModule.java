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
    Config<Boolean> text = new BooleanConfig("Text", "", true);
    Config<Color> color = new ColorConfig("Color", "", new Color(255, 255, 255), false, false);
    Config<Boolean> box = new BooleanConfig("Box", "", true);
    Config<Integer> boxAlpha = new NumberConfig<Integer>("BoxAlpha", "", 0, 80, 255);
    Config<Boolean> line = new BooleanConfig("lines", "", true);
    Config<Integer> olAlpha = new NumberConfig<Integer>("OLAlpha", "", 0, 255, 255);
    Config<Float> olWidth = new NumberConfig<Float>("OLWidth", "", 0.1f, 1.5f, 5.0f);
    Config<Color> friend_color = new ColorConfig("Friend_Color", "", new Color(0, 150, 255), false, false);
    Config<Boolean> friend_box = new BooleanConfig("Friend_Box", "", true);
    Config<Integer> friend_boxAlpha = new NumberConfig<Integer>("Friend_BoxAlpha", "", 0, 80, 255);
    Config<Boolean> friend_line = new BooleanConfig("Friend_lines", "", true);
    Config<Integer> friend_olAlpha = new NumberConfig<Integer>("Friend_OLAlpha", "", 0, 255, 255);
    Config<Float> friend_olWidth = new NumberConfig<Float>("Friend_OLWidth", "", 0.1f, 1.5f, 5.0f);
    Config<Float> range = new NumberConfig<Float>("Range", "", 5.0f, 20.0f, 50.0f);
    Render render = null;
    private final List<Render> renders = new ArrayList<Render>();

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
            if (MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(r.pos.toCenterPos())) > this.range.getValue().floatValue()) {
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
                if (this.text.getValue().booleanValue()) {
                    RenderManager.post(() -> RenderManager.renderSign(Formatting.AQUA + name, r.pos.toCenterPos()));
                }
                if (this.friend_box.getValue().booleanValue()) {
                    RenderManager.renderBox(event.getMatrices(), r.pos, friend.getRgb(this.friend_boxAlpha.getValue()));
                }
                if (this.friend_line.getValue().booleanValue()) {
                    RenderManager.renderBoundingBox(event.getMatrices(), r.pos, this.friend_olWidth.getValue().floatValue(), friend.getRgb(this.friend_olAlpha.getValue()));
                }
            } else {
                ColorConfig sb = (ColorConfig)this.color;
                if (this.text.getValue().booleanValue()) {
                    RenderManager.post(() -> RenderManager.renderSign(name, r.pos.toCenterPos()));
                }
                if (this.box.getValue().booleanValue()) {
                    RenderManager.renderBox(event.getMatrices(), r.pos, sb.getRgb(this.boxAlpha.getValue()));
                }
                if (this.line.getValue().booleanValue()) {
                    RenderManager.renderBoundingBox(event.getMatrices(), r.pos, this.olWidth.getValue().floatValue(), sb.getRgb(this.olAlpha.getValue()));
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
