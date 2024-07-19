package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.init.Managers;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SelfBowModule
extends RotationModule {
    private final Set<StatusEffectInstance> arrows = new HashSet<StatusEffectInstance>();

    public SelfBowModule() {
        super("SelfBow", "Shoots player with beneficial tipped arrows", ModuleCategory.COMBAT);
    }

    @Override
    public void onDisable() {
        SelfBowModule.mc.options.useKey.setPressed(false);
        this.arrows.clear();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        int arrowSlot = -1;
        StatusEffectInstance statusEffect = null;
        for (int i = 9; i < 36; ++i) {
            ItemStack stack = SelfBowModule.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof TippedArrowItem)) continue;
            Potion p = PotionUtil.getPotion(stack);
            for (StatusEffectInstance effect : p.getEffects()) {
                StatusEffect type = effect.getEffectType();
                if (!type.isBeneficial() || this.arrows.contains(effect)) continue;
                arrowSlot = i;
                statusEffect = effect;
                break;
            }
            if (arrowSlot != -1) break;
        }
        int bowSlot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = SelfBowModule.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || stack.getItem() != Items.BOW) continue;
            bowSlot = i;
            break;
        }
        if (SelfBowModule.mc.player.getMainHandStack().getItem() != Items.BOW || bowSlot == -1 || arrowSlot == -1) {
            this.disable();
            return;
        }
        this.setRotation(SelfBowModule.mc.player.getYaw(), -90.0f);
        SelfBowModule.mc.interactionManager.clickSlot(0, arrowSlot, 9, SlotActionType.SWAP, SelfBowModule.mc.player);
        float pullTime = BowItem.getPullProgress(SelfBowModule.mc.player.getItemUseTime());
        if (pullTime >= 0.15f) {
            this.arrows.add(statusEffect);
            SelfBowModule.mc.options.useKey.setPressed(false);
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            SelfBowModule.mc.player.stopUsingItem();
        } else {
            SelfBowModule.mc.options.useKey.setPressed(true);
        }
    }
}
