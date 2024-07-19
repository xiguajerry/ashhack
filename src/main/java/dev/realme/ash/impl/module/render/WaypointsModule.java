package dev.realme.ash.impl.module.render;

import com.google.common.collect.Maps;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.IEntity;
import dev.realme.ash.util.chat.ChatUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;

public class WaypointsModule
extends ToggleModule {
    final Config<Integer> boxAlpha = new NumberConfig<>("BoxAlpha", "", 0, 80, 255);
    final Config<Integer> olAlpha = new NumberConfig<>("OLAlpha", "", 0, 0, 255);
    final Config<Float> olWidth = new NumberConfig<>("OLWidth", "", 0.1f, 1.5f, 5.0f);
    private final Map<UUID, PlayerEntity> playerCache = Maps.newConcurrentMap();
    private final Map<UUID, PlayerEntity> logoutCache = Maps.newConcurrentMap();

    public WaypointsModule() {
        super("Waypoints", "Renders a waypoint at marked locations", ModuleCategory.RENDER);
    }

    @Override
    public void onDisable() {
        this.playerCache.clear();
        this.logoutCache.clear();
    }

    @EventListener
    public void onTick(TickEvent event) {
        for (PlayerEntity player : WaypointsModule.mc.world.getPlayers()) {
            if (player == null || player.equals(WaypointsModule.mc.player)) continue;
            this.playerCache.put(player.getGameProfile().getId(), player);
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        PlayerEntity player;
        Object packet;
        Object object = event.getPacket();
        if (object instanceof PlayerListS2CPacket) {
            packet = object;
            if (((PlayerListS2CPacket)packet).getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                object = ((PlayerListS2CPacket)packet).getPlayerAdditionEntries().iterator();
                while (((Iterator) object).hasNext()) {
                    PlayerListS2CPacket.Entry addedPlayer = (PlayerListS2CPacket.Entry)((Iterator) object).next();
                    for (UUID uuid : this.logoutCache.keySet()) {
                        if (!uuid.equals(addedPlayer.profile().getId())) continue;
                        player = this.logoutCache.get(uuid);
                        ChatUtil.sendChatMessageWidthId(player.getName().getString() + " §alogged back at X: " + (int)player.getX() + " Y: " + (int)player.getY() + " Z: " + (int)player.getZ(), player.getId() - 999);
                        this.logoutCache.remove(uuid);
                    }
                }
            }
            this.playerCache.clear();
        }
        if ((object = event.getPacket()) instanceof PlayerRemoveS2CPacket) {
            packet = object;
            for (UUID uuid2 : ((PlayerRemoveS2CPacket)packet).profileIds()) {
                for (UUID uuid : this.playerCache.keySet()) {
                    if (!uuid.equals(uuid2)) continue;
                    player = this.playerCache.get(uuid);
                    if (this.logoutCache.containsKey(uuid)) continue;
                    ChatUtil.sendChatMessageWidthId(player.getName().getString() + " §clogged out at X: " + (int)player.getX() + " Y: " + (int)player.getY() + " Z: " + (int)player.getZ(), player.getId() - 999);
                    this.logoutCache.put(uuid, player);
                }
            }
            this.playerCache.clear();
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (WaypointsModule.mc.player == null) {
            return;
        }
        for (UUID uuid : this.logoutCache.keySet()) {
            PlayerEntity data = this.logoutCache.get(uuid);
            if (data == null) continue;
            RenderManager.post(() -> RenderManager.renderSign(data.getName().getString(), data.getPos().add(0.0, data.getBoundingBox().getLengthY() + 0.4, 0.0)));
            RenderManager.renderBox(event.getMatrices(), ((IEntity) data).getDimensions().getBoxAt(data.getPos()), Modules.CLIENT_SETTING.getRGB(this.boxAlpha.getValue()));
            RenderManager.renderBoundingBox(event.getMatrices(), ((IEntity) data).getDimensions().getBoxAt(data.getPos()), this.olWidth.getValue(), Modules.CLIENT_SETTING.getRGB(this.olAlpha.getValue()));
        }
    }
}
