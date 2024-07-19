package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.world.FakePlayerEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.text.Text;

public class AutoLogModule
extends ToggleModule {
    Config<Float> healthConfig = new NumberConfig<Float>("Health", "Disconnects when player reaches this health", Float.valueOf(0.1f), Float.valueOf(5.0f), Float.valueOf(19.0f));
    Config<Boolean> healthTotemConfig = new BooleanConfig("HealthTotems", "Totem check for health config", true);
    Config<Boolean> onRenderConfig = new BooleanConfig("OnRender", "Disconnects when a player enters render distance", false);
    Config<Boolean> noTotemConfig = new BooleanConfig("NoTotems", "Disconnects when player has no totems in the inventory", false);
    Config<Integer> totemsConfig = new NumberConfig<Integer>("Totems", "The number of totems before disconnecting", 0, 1, 5);
    Config<Boolean> illegalDisconnectConfig = new BooleanConfig("IllegalDisconnect", "Disconnects from the server using invalid packets", false);

    public AutoLogModule() {
        super("AutoLog", "Automatically disconnects from server during combat", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        boolean b2;
        AbstractClientPlayerEntity player;
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (this.onRenderConfig.getValue().booleanValue() && (player = (AbstractClientPlayerEntity)AutoLogModule.mc.world.getPlayers().stream().filter(p -> this.checkEnemy((AbstractClientPlayerEntity)p)).findFirst().orElse(null)) != null) {
            this.playerDisconnect("[AutoLog] %s came into render distance.", player.getName().getString());
            return;
        }
        float health = AutoLogModule.mc.player.getHealth() + AutoLogModule.mc.player.getAbsorptionAmount();
        int totems = InventoryUtil.count(Items.TOTEM_OF_UNDYING);
        boolean bl = b2 = totems <= this.totemsConfig.getValue();
        if (health <= this.healthConfig.getValue().floatValue()) {
            if (!this.healthTotemConfig.getValue().booleanValue()) {
                this.playerDisconnect("[AutoLog] logged out with %d hearts remaining.", (int)health);
                return;
            }
            if (b2) {
                this.playerDisconnect("[AutoLog] logged out with %d totems and %d hearts remaining.", totems, (int)health);
                return;
            }
        }
        if (b2 && this.noTotemConfig.getValue().booleanValue()) {
            this.playerDisconnect("[AutoLog] logged out with %d totems remaining.", totems);
        }
    }

    private void playerDisconnect(String disconnectReason, Object ... args) {
        if (this.illegalDisconnectConfig.getValue().booleanValue()) {
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(AutoLogModule.mc.player, false));
            this.disable();
            return;
        }
        if (mc.getNetworkHandler() == null) {
            AutoLogModule.mc.world.disconnect();
            this.disable();
            return;
        }
        disconnectReason = String.format(disconnectReason, args);
        mc.getNetworkHandler().getConnection().disconnect(Text.of(disconnectReason));
        this.disable();
    }

    private boolean checkEnemy(AbstractClientPlayerEntity player) {
        return !Managers.SOCIAL.isFriend(player.getName()) && !(player instanceof FakePlayerEntity);
    }
}
