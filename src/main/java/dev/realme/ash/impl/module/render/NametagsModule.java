package dev.realme.ash.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.Interpolation;
import dev.realme.ash.api.render.RenderLayersClient;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.impl.event.render.entity.RenderLabelEvent;
import dev.realme.ash.init.Fonts;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.AccessorItemRenderer;
import dev.realme.ash.util.render.ColorUtil;
import dev.realme.ash.util.world.FakePlayerEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class NametagsModule
        extends ToggleModule {
    Config<Boolean> armorConfig = new BooleanConfig("Armor", "Displays the player's armor", true);
    Config<Boolean> enchantmentsConfig = new BooleanConfig("Enchantments", "Displays a list of the item's enchantments", true);
    Config<Boolean> durabilityConfig = new BooleanConfig("Durability", "Displays item durability", true);
    Config<Boolean> itemNameConfig = new BooleanConfig("ItemName", "Displays the player's current held item name", false);
    Config<Boolean> entityIdConfig = new BooleanConfig("EntityId", "Displays the player's entity id", false);
    Config<Boolean> gamemodeConfig = new BooleanConfig("Gamemode", "Displays the player's gamemode", false);
    Config<Boolean> pingConfig = new BooleanConfig("Ping", "Displays the player's server connection ping", true);
    Config<Boolean> healthConfig = new BooleanConfig("Health", "Displays the player's current health", true);
    Config<Boolean> totemsConfig = new BooleanConfig("Totems", "Displays the player's popped totem count", false);
    Config<Boolean> turtle = new BooleanConfig("Turtle", "", true);
    Config<Float> scalingConfig = new NumberConfig<Float>("Scaling", "The nametag label scale", Float.valueOf(0.001f), Float.valueOf(0.003f), Float.valueOf(0.01f));
    Config<Boolean> borderedConfig = new BooleanConfig("TextBorder", "Renders a border behind the nametag", true);

    public NametagsModule() {
        super("Nametags", "Renders info on player nametags", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (NametagsModule.mc.gameRenderer == null || mc.getCameraEntity() == null) {
            return;
        }
        Vec3d interpolate = Interpolation.getRenderPosition(mc.getCameraEntity(), mc.getTickDelta());
        Camera camera = NametagsModule.mc.gameRenderer.getCamera();
        Vec3d pos = camera.getPos();
        for (Entity entity : NametagsModule.mc.world.getEntities()) {
            double dz;
            double dy;
            PlayerEntity player;
            if (!(entity instanceof PlayerEntity) || (player = (PlayerEntity) entity) == NametagsModule.mc.player && NametagsModule.mc.options.getPerspective().isFirstPerson() && !Modules.FREECAM.isEnabled())
                continue;
            String info = this.getNametagInfo(player);
            Vec3d pinterpolate = Interpolation.getRenderPosition(player, mc.getTickDelta());
            double rx = player.getX() - pinterpolate.getX();
            double ry = player.getY() - pinterpolate.getY();
            double rz = player.getZ() - pinterpolate.getZ();
            int width = RenderManager.textWidth(info);
            float hwidth = (float) width / 2.0f;
            double dx = pos.getX() - interpolate.getX() - rx;
            double dist = Math.sqrt(dx * dx + (dy = pos.getY() - interpolate.getY() - ry) * dy + (dz = pos.getZ() - interpolate.getZ() - rz) * dz);
            if (dist > 4096.0) continue;
            float scaling = 0.0018f + this.scalingConfig.getValue().floatValue() * (float) dist;
            if (dist <= 8.0) {
                scaling = 0.0245f;
            }
            this.renderInfo(info, hwidth, player, rx, ry, rz, camera, scaling);
        }
        RenderSystem.enableBlend();
    }

    @EventListener
    public void onRenderLabel(RenderLabelEvent event) {
        if (event.getEntity() instanceof PlayerEntity && event.getEntity() != NametagsModule.mc.player) {
            event.cancel();
        }
    }

    private void renderInfo(String info, float width, PlayerEntity entity, double x, double y, double z, Camera camera, float scaling) {
        Vec3d pos = camera.getPos();
        MatrixStack matrices = new MatrixStack();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrices.translate(x - pos.getX(), y + (double) entity.getHeight() + (double) (entity.isSneaking() ? 0.4f : 0.43f) - pos.getY(), z - pos.getZ());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.scale(-scaling, -scaling, -1.0f);
        if (this.borderedConfig.getValue().booleanValue()) {
            double d = -width - 1.0f;
            double d2 = width * 2.0f + 2.0f;
            Objects.requireNonNull(NametagsModule.mc.textRenderer);
            RenderManager.rect(matrices, d, -1.0, d2, 9.0f + 1.0f, 0.0, 0x55000400);
        }
        int color = this.getNametagColor(entity);
        RenderManager.post(() -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GL11.glDepthFunc(519);
            Fonts.VANILLA.drawWithShadow(matrices, info, -width, 0.0f, color);
            if (this.armorConfig.getValue().booleanValue()) {
                this.renderItems(matrices, entity);
            }
            GL11.glDepthFunc(515);
            RenderSystem.disableBlend();
        });
    }

    private void renderItems(MatrixStack matrixStack, PlayerEntity player) {
        CopyOnWriteArrayList<ItemStack> displayItems = new CopyOnWriteArrayList<ItemStack>();
        if (!player.getOffHandStack().isEmpty()) {
            displayItems.add(player.getOffHandStack());
        }
        player.getInventory().armor.forEach(armorStack -> {
            if (!armorStack.isEmpty()) {
                displayItems.add((ItemStack) armorStack);
            }
        });
        if (!player.getMainHandStack().isEmpty()) {
            displayItems.add(player.getMainHandStack());
        }
        Collections.reverse(displayItems);
        float n10 = 0.0f;
        int n11 = 0;
        for (ItemStack itemStack : displayItems) {
            n10 -= 8.0f;
            if (itemStack.getEnchantments().size() <= n11) continue;
            n11 = itemStack.getEnchantments().size();
        }
        float m2 = this.enchantOffset(n11);
        for (ItemStack stack : displayItems) {
            matrixStack.push();
            matrixStack.translate(n10, m2, 0.0f);
            matrixStack.translate(8.0f, 8.0f, 0.0f);
            matrixStack.scale(16.0f, 16.0f, 0.0f);
            matrixStack.multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 0.0f));
            this.renderItem(stack, ModelTransformationMode.GUI, 0xFFFFFF, 10, matrixStack, mc.getBufferBuilders().getEntityVertexConsumers(), NametagsModule.mc.world, 0);
            mc.getBufferBuilders().getEntityVertexConsumers().draw();
            matrixStack.pop();
            this.renderItemOverlay(matrixStack, stack, (int) n10, (int) m2);
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            if (this.durabilityConfig.getValue().booleanValue()) {
                this.renderDurability(matrixStack, stack, n10 + 2.0f, m2 - 4.5f);
            }
            if (this.enchantmentsConfig.getValue().booleanValue()) {
                this.renderEnchants(matrixStack, stack, n10 + 2.0f, m2);
            }
            matrixStack.scale(2.0f, 2.0f, 2.0f);
            n10 += 16.0f;
        }
        ItemStack itemStack = player.getMainHandStack();
        if (itemStack.isEmpty()) {
            return;
        }
        matrixStack.scale(0.5f, 0.5f, 0.5f);
        if (this.itemNameConfig.getValue().booleanValue()) {
            this.renderItemName(matrixStack, itemStack, 0.0f, this.durabilityConfig.getValue() != false ? m2 - 9.0f : m2 - 4.5f);
        }
        matrixStack.scale(2.0f, 2.0f, 2.0f);
    }

    private void renderItem(ItemStack stack, ModelTransformationMode renderMode, int light, int overlay, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int seed) {
        boolean bl;
        BakedModel bakedModel = mc.getItemRenderer().getModel(stack, world, null, seed);
        if (stack.isEmpty()) {
            return;
        }
        boolean bl2 = bl = renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED;
        if (bl) {
            if (stack.isOf(Items.TRIDENT)) {
                bakedModel = mc.getItemRenderer().getModels().getModelManager().getModel(ModelIdentifier.ofVanilla("trident", "inventory"));
            } else if (stack.isOf(Items.SPYGLASS)) {
                bakedModel = mc.getItemRenderer().getModels().getModelManager().getModel(ModelIdentifier.ofVanilla("spyglass", "inventory"));
            }
        }
        bakedModel.getTransformation().getTransformation(renderMode).apply(false, matrices);
        matrices.translate(-0.5f, -0.5f, -0.5f);
        if (bakedModel.isBuiltin() || stack.isOf(Items.TRIDENT) && !bl) {
            ((AccessorItemRenderer) ((Object) mc.getItemRenderer())).hookGetBuiltinModelItemRenderer().render(stack, renderMode, matrices, vertexConsumers, light, overlay);
        } else {
            ((AccessorItemRenderer) ((Object) mc.getItemRenderer())).hookRenderBakedItemModel(bakedModel, stack, light, overlay, matrices, NametagsModule.getItemGlintConsumer(vertexConsumers, RenderLayersClient.ITEM_ENTITY_TRANSLUCENT_CULL, stack.hasGlint()));
        }
    }

    public static VertexConsumer getItemGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean glint) {
        if (glint) {
            return VertexConsumers.union(vertexConsumers.getBuffer(RenderLayersClient.GLINT), vertexConsumers.getBuffer(layer));
        }
        return vertexConsumers.getBuffer(layer);
    }

    private void renderItemOverlay(MatrixStack matrixStack, ItemStack stack, int x, int y) {
        matrixStack.push();
        if (stack.getCount() != 1) {
            String string = String.valueOf(stack.getCount());
            Fonts.VANILLA.drawWithShadow(matrixStack, string, x + 17 - NametagsModule.mc.textRenderer.getWidth(string), (float) y + 9.0f, -1);
        }
        if (stack.isItemBarVisible() && this.durabilityConfig.getValue().booleanValue()) {
            int i = stack.getItemBarStep();
            int j = stack.getItemBarColor();
            int k = x + 2;
            int l = y + 13;
            RenderManager.rect(matrixStack, k, l, 13.0, 1.0, -16777216);
            RenderManager.rect(matrixStack, k, l, i, 1.0, j | 0xFF000000);
        }
        matrixStack.pop();
    }

    private void renderDurability(MatrixStack matrixStack, ItemStack itemStack, float x, float y) {
        if (!itemStack.isDamageable()) {
            return;
        }
        int n = itemStack.getMaxDamage();
        int n2 = itemStack.getDamage();
        int durability = (int) ((float) (n - n2) / (float) n * 100.0f);
        Fonts.VANILLA.drawWithShadow(matrixStack, durability + "%", x * 2.0f, y * 2.0f, ColorUtil.hslToColor((float) (n - n2) / (float) n * 120.0f, 100.0f, 50.0f, 1.0f).getRGB());
    }

    private void renderEnchants(MatrixStack matrixStack, ItemStack itemStack, float x, float y) {
        if (itemStack.getItem() instanceof EnchantedGoldenAppleItem) {
            Fonts.VANILLA.drawWithShadow(matrixStack, "God", x * 2.0f, y * 2.0f, -3977663);
            return;
        }
        if (!itemStack.hasEnchantments()) {
            return;
        }
        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(itemStack);
        float n2 = 0.0f;
        for (Enchantment enchantment : enchants.keySet()) {
            int lvl = enchants.get(enchantment);
            StringBuilder enchantString = new StringBuilder();
            String translatedName = enchantment.getName(lvl).getString();
            if (translatedName.contains("Vanish")) {
                enchantString.append("Van");
            } else if (translatedName.contains("Bind")) {
                enchantString.append("Bind");
            } else {
                int maxLen;
                int n = maxLen = lvl > 1 ? 2 : 3;
                if (translatedName.length() > maxLen) {
                    translatedName = translatedName.substring(0, maxLen);
                }
                enchantString.append(translatedName);
                enchantString.append(lvl);
            }
            Fonts.VANILLA.drawWithShadow(matrixStack, enchantString.toString(), x * 2.0f, (y + n2) * 2.0f, -1);
            n2 += 4.5f;
        }
    }

    private float enchantOffset(int n) {
        if (!this.enchantmentsConfig.getValue().booleanValue() || n <= 3) {
            return -18.0f;
        }
        float n2 = -14.0f;
        return n2 -= (float) (n - 3) * 4.5f;
    }

    private void renderItemName(MatrixStack matrixStack, ItemStack itemStack, float x, float y) {
        String itemName = itemStack.getName().getString();
        float width = (float) NametagsModule.mc.textRenderer.getWidth(itemName) / 4.0f;
        Fonts.VANILLA.drawWithShadow(matrixStack, itemName, (x - width) * 2.0f, y * 2.0f, -1);
    }

    private String getNametagInfo(PlayerEntity player) {
        int totems;
        PlayerListEntry playerEntry;
        StringBuilder info = new StringBuilder();
        if (this.turtle.getValue().booleanValue()) {
            for (StatusEffectInstance e : player.getStatusEffects()) {
                int duration;
                if (!e.getEffectType().equals(StatusEffects.RESISTANCE) || e.getAmplifier() <= 2 || (duration = e.getDuration()) < 0)
                    continue;
                info.append("God ");
            }
        }
        info.append(player.getName().getString());
        info.append(" ");
        if (this.entityIdConfig.getValue().booleanValue()) {
            info.append("ID: ");
            info.append(player.getId());
            info.append(" ");
        }
        if (this.gamemodeConfig.getValue().booleanValue()) {
            if (player.isCreative()) {
                info.append("[C] ");
            } else if (player.isSpectator()) {
                info.append("[I] ");
            } else {
                info.append("[S] ");
            }
        }
        if (this.pingConfig.getValue().booleanValue() && mc.getNetworkHandler() != null && (playerEntry = mc.getNetworkHandler().getPlayerListEntry(player.getGameProfile().getId())) != null) {
            info.append(playerEntry.getLatency());
            info.append("ms ");
        }
        if (this.healthConfig.getValue().booleanValue()) {
            double health = Math.ceil(player.getHealth() + player.getAbsorptionAmount());
            Formatting hcolor = health > 18.0 ? Formatting.GREEN : (health > 16.0 ? Formatting.DARK_GREEN : (health > 12.0 ? Formatting.YELLOW : (health > 8.0 ? Formatting.GOLD : (health > 4.0 ? Formatting.RED : Formatting.DARK_RED))));
            int phealth = (int) health;
            info.append(hcolor);
            info.append(phealth);
            info.append(" ");
        }
        if (this.totemsConfig.getValue().booleanValue() && player != NametagsModule.mc.player && (totems = Managers.TOTEM.getTotems(player)) > 0) {
            Formatting pcolor = Formatting.GREEN;
            if (totems > 1) {
                pcolor = Formatting.DARK_GREEN;
            }
            if (totems > 2) {
                pcolor = Formatting.YELLOW;
            }
            if (totems > 3) {
                pcolor = Formatting.GOLD;
            }
            if (totems > 4) {
                pcolor = Formatting.RED;
            }
            if (totems > 5) {
                pcolor = Formatting.DARK_RED;
            }
            info.append(pcolor);
            info.append(-totems);
            info.append(" ");
        }
        return info.toString().trim();
    }

    private int getNametagColor(PlayerEntity player) {
        if (player == NametagsModule.mc.player) {
            return Modules.CLIENT_SETTING.getRGB(255);
        }
        if (Managers.SOCIAL.isFriend(player.getName())) {
            return -10027009;
        }
        if (player.isInvisible()) {
            return -56064;
        }
        if (player instanceof FakePlayerEntity) {
            return -1113785;
        }
        if (player.isSneaking()) {
            return -26368;
        }
        return -1;
    }

    public float getScaling() {
        return this.scalingConfig.getValue().floatValue();
    }
}
