package dev.realme.ash.impl.module.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.math.MathUtil;
import java.awt.Color;
import java.lang.invoke.LambdaMetafactory;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class PopChamsModule
extends ToggleModule {
    final Config<Color> colorConfig = new ColorConfig("Color", "", new Color(255, 255, 255), true, false);
    final Config<Integer> aSpeed = new NumberConfig<>("ASpeed", "", 1, 5, 100);
    private final CopyOnWriteArrayList<Person> popList = new CopyOnWriteArrayList<>();

    public PopChamsModule() {
        super("PopChams", "Renders shaders.", ModuleCategory.RENDER);
    }

    @EventListener
    public void onTick(TickEvent event) {
        this.popList.forEach(person -> person.update(this.popList));
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        this.popList.forEach(person -> this.renderEntity(event.getMatrices(), person.player, person.modelPlayer, person.getAlpha()));
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        EntityStatusS2CPacket packet;
        Packet<?> packet2;
        if (PopChamsModule.mc.world != null && (packet2 = event.getPacket()) instanceof EntityStatusS2CPacket && (packet = (EntityStatusS2CPacket) packet2).getStatus() == 35) {
            PlayerEntity e = (PlayerEntity)packet.getEntity(PopChamsModule.mc.world);
            if (e == null) {
                return;
            }
            PlayerEntity entity = new PlayerEntity(PopChamsModule.mc.world, BlockPos.ORIGIN, e.bodyYaw, new GameProfile(e.getUuid(), e.getName().getString())){

                @Override
                public boolean isSpectator() {
                    return false;
                }

                @Override
                public boolean isCreative() {
                    return false;
                }
            };
            entity.copyPositionAndRotation(e);
            entity.bodyYaw = e.bodyYaw;
            entity.headYaw = e.headYaw;
            entity.handSwingProgress = e.handSwingProgress;
            entity.handSwingTicks = e.handSwingTicks;
            entity.setSneaking(e.isSneaking());
            entity.limbAnimator.setSpeed(e.limbAnimator.getSpeed());
            entity.limbAnimator.pos = e.limbAnimator.getPos();
            this.popList.add(new Person(entity));
        }
    }

    private void renderEntity(MatrixStack matrices, LivingEntity entity, BipedEntityModel<PlayerEntity> modelBase, int alpha) {
        double x = entity.getX() - PopChamsModule.mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = entity.getY() - PopChamsModule.mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = entity.getZ() - PopChamsModule.mc.getEntityRenderDispatcher().camera.getPos().getZ();
        matrices.push();
        matrices.translate((float)x, (float)y, (float)z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180.0f - entity.bodyYaw)));
        PopChamsModule.prepareScale(matrices);
        modelBase.animateModel((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), mc.getTickDelta());
        float limbSpeed = Math.min(entity.limbAnimator.getSpeed(), 1.0f);
        modelBase.setAngles((PlayerEntity) entity, entity.limbAnimator.getPos(), limbSpeed, (float)entity.age, entity.headYaw - entity.bodyYaw, entity.getPitch());
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        RenderSystem.setShaderColor(this.colorConfig.getValue().getRed(), this.colorConfig.getValue().getGreen(), this.colorConfig.getValue().getBlue(), (float)alpha / 255.0f);
        modelBase.render(matrices, buffer, 10, 0, (float)this.colorConfig.getValue().getRed() / 255.0f, (float)this.colorConfig.getValue().getGreen() / 255.0f, (float)this.colorConfig.getValue().getBlue() / 255.0f, (float)alpha / 255.0f);
        tessellator.draw();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        matrices.pop();
    }

    private static void prepareScale(@NotNull MatrixStack matrixStack) {
        matrixStack.scale(-1.0f, -1.0f, 1.0f);
        matrixStack.scale(1.6f, 1.8f, 1.6f);
        matrixStack.translate(0.0f, -1.501f, 0.0f);
    }

    public static double animate(double current, double endPoint, double speed) {
        if (speed >= 1.0) {
            return endPoint;
        }
        return speed == 0.0 ? current : PopChamsModule.thunder(current, endPoint, speed);
    }

    public static double thunder(double current, double endPoint, double speed) {
        boolean shouldContinueAnimation = endPoint > current;
        double dif = Math.max(endPoint, current) - Math.min(endPoint, current);
        if (Math.abs(dif) <= 0.001) {
            return endPoint;
        }
        double factor = dif * speed;
        return current + (shouldContinueAnimation ? factor : -factor);
    }

    private class Person {
        private final PlayerEntity player;
        private final PlayerEntityModel<PlayerEntity> modelPlayer;
        private int alpha;

        public Person(PlayerEntity player) {
            this.player = player;
            this.modelPlayer = new PlayerEntityModel<>(new EntityRendererFactory.Context(Globals.mc.getEntityRenderDispatcher(), Globals.mc.getItemRenderer(), Globals.mc.getBlockRenderManager(), Globals.mc.getEntityRenderDispatcher().getHeldItemRenderer(), Globals.mc.getResourceManager(), Globals.mc.getEntityModelLoader(), Globals.mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
            this.modelPlayer.getHead().scale(new Vector3f(-0.3f, -0.3f, -0.3f));
            this.alpha = PopChamsModule.this.colorConfig.getValue().getAlpha();
        }

        public void update(CopyOnWriteArrayList<Person> arrayList) {
            if (this.alpha <= 0) {
                arrayList.remove(this);
                this.player.kill();
                this.player.remove(Entity.RemovalReason.KILLED);
                this.player.onRemoved();
            } else {
                this.alpha = (int)(PopChamsModule.animate(this.alpha, 0.0, PopChamsModule.this.aSpeed.getValue()) - 0.2);
            }
        }

        public int getAlpha() {
            return (int)MathUtil.clamp((float)this.alpha, 0.0f, 255.0f);
        }
    }
}
