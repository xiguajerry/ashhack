package dev.realme.ash.impl.module.client;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.ConfigContainer;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.ScreenOpenEvent;
import dev.realme.ash.impl.event.gui.hud.RenderOverlayEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.StreamUtils;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.render.ColorUtil;
import dev.realme.ash.util.render.animation.Animation;
import dev.realme.ash.util.render.animation.Easing;
import dev.realme.ash.util.string.EnumFormatter;
import dev.realme.ash.util.world.EntityUtil;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HUDModule
extends ToggleModule {
    Config<Boolean> watermarkConfig = new BooleanConfig("Watermark", "Displays client name and version watermark", true);
    Config<Boolean> directionConfig = new BooleanConfig("Direction", "Displays facing direction", true);
    Config<Boolean> totemConfig = new BooleanConfig("Totem", "", false);
    Config<Boolean> armorConfig = new BooleanConfig("Armor", "Displays player equipped armor and durability", true);
    Config<VanillaHud> potionHudConfig = new EnumConfig("PotionHud", "Renders the Minecraft potion Hud", VanillaHud.HIDE, VanillaHud.values());
    Config<VanillaHud> itemNameConfig = new EnumConfig("ItemName", "Renders the Minecraft item name display", VanillaHud.HIDE, VanillaHud.values());
    Config<Boolean> potionEffectsConfig = new BooleanConfig("PotionEffects", "Displays active potion effects", true);
    Config<Boolean> potionColorsConfig = new BooleanConfig("PotionColors", "Displays active potion colors", true);
    Config<Boolean> durabilityConfig = new BooleanConfig("Durability", "Displays the current held items durability", false);
    Config<Boolean> coordsConfig = new BooleanConfig("Coords", "Displays world coordinates", true);
    Config<Boolean> netherCoordsConfig = new BooleanConfig("NetherCoords", "Displays nether coordinates", true, () -> this.coordsConfig.getValue());
    Config<Boolean> serverBrandConfig = new BooleanConfig("ServerBrand", "Displays the current server brand", false);
    Config<Boolean> speedConfig = new BooleanConfig("Speed", "Displays the current movement speed of the player in kmh", true);
    Config<Boolean> pingConfig = new BooleanConfig("Ping", "Display server response time in ms", true);
    Config<Boolean> tpsConfig = new BooleanConfig("TPS", "Displays server ticks per second", true);
    Config<Boolean> fpsConfig = new BooleanConfig("FPS", "Displays game FPS", true);
    Config<Boolean> arraylistConfig = new BooleanConfig("Arraylist", "Displays a list of all active modules", true);
    Config<Boolean> turtle = new BooleanConfig("Turtle", "", true);
    Config<Boolean> all = new BooleanConfig("All", "", true);
    Config<Float> xOffset1 = new NumberConfig<>("XOffset1", "", -500.0f, 0.0f, 500.0f);
    Config<Float> yOffset1 = new NumberConfig<>("YOffset1", "", -500.0f, 0.0f, 500.0f);
    Config<Float> xOffset2 = new NumberConfig<>("XOffset2", "", -500.0f, 7.0f, 500.0f);
    Config<Float> yOffset2 = new NumberConfig<>("YOffset2", "", -500.0f, 17.0f, 500.0f);
    Config<Ordering> orderingConfig = new EnumConfig<>("Ordering", "The ordering of the arraylist", Ordering.LENGTH, Ordering.values(), () -> this.arraylistConfig.getValue());
    Config<Rendering> renderingConfig = new EnumConfig<>("Rendering", "The rendering mode of the HUD", Rendering.UP, Rendering.values());
    Config<RainbowMode> rainbowModeConfig = new EnumConfig<>("Rainbow", "The rendering mode for rainbow", RainbowMode.OFF, RainbowMode.values());
    Config<Float> alphaSpeedConfig = new NumberConfig<>("Alpha-Speed", "", 0.1f, 50.0f, 1500.0f);
    Config<Float> rainbowSpeedConfig = new NumberConfig<>("Rainbow-Speed", "The speed for the rainbow color cycling", 0.1f, 50.0f, 100.0f);
    Config<Integer> rainbowSaturationConfig = new NumberConfig<>("Rainbow-Saturation", "The saturation of rainbow colors", 0, 35, 100);
    Config<Integer> rainbowBrightnessConfig = new NumberConfig<>("Rainbow-Brightness", "The brightness of rainbow colors", 0, 100, 100);
    Config<Float> rainbowDifferenceConfig = new NumberConfig<>("Rainbow-Difference", "The difference offset for rainbow colors", 0.1f, 40.0f, 100.0f);
    private final DecimalFormat decimal = new DecimalFormat("0.0");
    private int rainbowOffset;
    private float topLeft;
    private float topRight;
    private float bottomLeft;
    private float bottomRight;
    private boolean renderingUp;
    private final Animation chatOpenAnimation = new Animation(false, 200.0f, Easing.LINEAR);
    private static final ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);

    public HUDModule() {
        super("HUD", "Displays the HUD (heads up display) screen.", ModuleCategory.CLIENT);
    }

    private void arrayListRenderModule(RenderOverlayEvent.Post event, ToggleModule toggleModule) {
        Animation anim = toggleModule.getAnimation();
        float factor = (float)anim.getFactor();
        if (factor <= 0.01f || toggleModule.isHidden()) {
            return;
        }
        String text = this.getFormattedModule(toggleModule);
        int width = RenderManager.textWidth(text);
        RenderManager.renderText(event.getContext(), text, (float)mc.getWindow().getScaledWidth() - (float)width * factor - 1.0f, this.renderingUp ? this.topRight : this.bottomRight, this.getHudColor(this.rainbowOffset));
        if (this.renderingUp) {
            this.topRight += 9.0f;
        } else {
            this.bottomRight -= 9.0f;
        }
        ++this.rainbowOffset;
    }

    @EventListener
    public void onRenderOverlayPost(RenderOverlayEvent.Post event) {
        if (HUDModule.mc.player != null && HUDModule.mc.world != null) {
            int width;
            if (mc.getDebugHud().shouldShowDebugHud()) {
                return;
            }
            Window res = mc.getWindow();
            this.rainbowOffset = 0;
            this.topRight = this.topLeft = 2.0f;
            this.bottomRight = this.bottomLeft = (float)res.getScaledHeight() - 11.0f;
            this.renderingUp = this.renderingConfig.getValue() == Rendering.UP;
            this.bottomLeft -= (float)(14.0 * this.chatOpenAnimation.getFactor());
            this.bottomRight -= (float)(14.0 * this.chatOpenAnimation.getFactor());
            if (this.potionHudConfig.getValue() == VanillaHud.MOVE && !HUDModule.mc.player.getStatusEffects().isEmpty()) {
                this.topRight += 27.0f;
            }
            if (this.turtle.getValue().booleanValue()) {
                int count = InventoryUtil.count(StatusEffects.RESISTANCE);
                if (count > 0) {
                    int width2 = RenderManager.textWidth(String.valueOf(count));
                    RenderManager.renderText(event.getContext(), String.valueOf(count), (float)res.getScaledWidth() / 2.0f - (float)width2 / 2.0f + this.xOffset1.getValue().floatValue(), (float)res.getScaledHeight() / 2.0f + this.yOffset1.getValue().floatValue(), this.getHudColor(this.rainbowOffset));
                }
                for (StatusEffectInstance e : HUDModule.mc.player.getStatusEffects()) {
                    if (!e.getEffectType().equals(StatusEffects.RESISTANCE) || e.getAmplifier() <= 2) continue;
                    Text duration = StatusEffectUtil.getDurationText(e, 1.0f, HUDModule.mc.world.getTickManager().getTickRate());
                    int totalSeconds = MathHelper.floor((float)MathHelper.floor(e.getDuration()) / HUDModule.mc.world.getTickManager().getTickRate());
                    Object string = this.all.getValue() ? duration.getString() : totalSeconds + "s";
                    width = RenderManager.textWidth((String)string);
                    RenderManager.renderText(event.getContext(), (String)string, (float)res.getScaledWidth() / 2.0f - (float)width / 2.0f + this.xOffset2.getValue().floatValue(), (float)res.getScaledHeight() / 2.0f + this.yOffset2.getValue().floatValue(), this.getHudColor(this.rainbowOffset));
                }
            }
            if (this.watermarkConfig.getValue().booleanValue()) {
                RenderManager.renderText(event.getContext(), String.format("%s %s", "Ash", "3.0"), 2.0f, this.topLeft, this.getHudColor(this.rainbowOffset));
            }
            if (this.arraylistConfig.getValue().booleanValue()) {
                List<Module> modules = Managers.MODULE.getModules();
                Stream<ToggleModule> moduleStream = modules.stream().filter(ToggleModule.class::isInstance).map(ToggleModule.class::cast);
                moduleStream = switch (this.orderingConfig.getValue()) {
                    default -> throw new IncompatibleClassChangeError();
                    case ALPHABETICAL -> StreamUtils.sortCached(moduleStream, ConfigContainer::getName);
                    case LENGTH -> StreamUtils.sortCached(moduleStream, m -> -RenderManager.textWidth(this.getFormattedModule(m)));
                };
                moduleStream.forEach(t -> this.arrayListRenderModule(event, t));
            }
            if (this.potionEffectsConfig.getValue().booleanValue()) {
                for (StatusEffectInstance e : HUDModule.mc.player.getStatusEffects()) {
                    StatusEffect effect = e.getEffectType();
                    if (effect == StatusEffects.NIGHT_VISION) continue;
                    boolean amplifier = e.getAmplifier() + 1 > 1 && !e.isInfinite();
                    Text duration = StatusEffectUtil.getDurationText(e, 1.0f, HUDModule.mc.world.getTickManager().getTickRate());
                    String text = String.format("%s %s\u00a7f%s", effect.getName().getString(), amplifier ? e.getAmplifier() + 1 + " " : "", e.isInfinite() ? "" : duration.getString());
                    width = RenderManager.textWidth(text);
                    RenderManager.renderText(event.getContext(), text, (float)(res.getScaledWidth() - width) - 1.0f, this.renderingUp ? this.bottomRight : this.topRight, this.potionColorsConfig.getValue() ? effect.getColor() : this.getHudColor(this.rainbowOffset));
                    if (this.renderingUp) {
                        this.bottomRight -= 9.0f;
                    } else {
                        this.topRight += 9.0f;
                    }
                    ++this.rainbowOffset;
                }
            }
            if (this.serverBrandConfig.getValue().booleanValue() && mc.getServer() != null) {
                String brand = mc.getServer().getVersion();
                int width3 = RenderManager.textWidth(brand);
                RenderManager.renderText(event.getContext(), brand, (float)(res.getScaledWidth() - width3) - 1.0f, this.renderingUp ? this.bottomRight : this.topRight, this.getHudColor(this.rainbowOffset));
                if (this.renderingUp) {
                    this.bottomRight -= 9.0f;
                } else {
                    this.topRight += 9.0f;
                }
                ++this.rainbowOffset;
            }
            if (this.speedConfig.getValue().booleanValue()) {
                double x = HUDModule.mc.player.getX() - HUDModule.mc.player.prevX;
                double z = HUDModule.mc.player.getZ() - HUDModule.mc.player.prevZ;
                double dist = Math.sqrt(x * x + z * z) / 1000.0;
                double div = 1.388888888888889E-5;
                float timer = Modules.TIMER.isEnabled() ? Modules.TIMER.getTimer() : 1.0f;
                double speed = dist / div * (double)timer;
                String text = String.format("Speed \u00a7f%skm/h", this.decimal.format(speed));
                int width4 = RenderManager.textWidth(text);
                RenderManager.renderText(event.getContext(), text, (float)(res.getScaledWidth() - width4) - 1.0f, this.renderingUp ? this.bottomRight : this.topRight, this.getHudColor(this.rainbowOffset));
                if (this.renderingUp) {
                    this.bottomRight -= 9.0f;
                } else {
                    this.topRight += 9.0f;
                }
                ++this.rainbowOffset;
            }
            if (this.durabilityConfig.getValue().booleanValue() && HUDModule.mc.player.getMainHandStack().isDamageable()) {
                int n = HUDModule.mc.player.getMainHandStack().getMaxDamage();
                int n2 = HUDModule.mc.player.getMainHandStack().getDamage();
                String text1 = "Durability ";
                String text2 = String.valueOf(n - n2);
                int width5 = RenderManager.textWidth(text1);
                int width2 = RenderManager.textWidth(text2);
                Color color = ColorUtil.hslToColor((float)(n - n2) / (float)n * 120.0f, 100.0f, 50.0f, 1.0f);
                RenderManager.renderText(event.getContext(), text1, (float)(res.getScaledWidth() - width5 - width2) - 1.0f, this.renderingUp ? this.bottomRight : this.topRight, this.getHudColor(this.rainbowOffset));
                RenderManager.renderText(event.getContext(), text2, (float)(res.getScaledWidth() - width2) - 1.0f, this.renderingUp ? this.bottomRight : this.topRight, color.getRGB());
                if (this.renderingUp) {
                    this.bottomRight -= 9.0f;
                } else {
                    this.topRight += 9.0f;
                }
                ++this.rainbowOffset;
            }
            if (this.pingConfig.getValue().booleanValue() && !mc.isInSingleplayer()) {
                int latency = Modules.FAST_LATENCY.isEnabled() ? (int)Modules.FAST_LATENCY.getLatency() : Managers.NETWORK.getClientLatency();
                String text = String.format("Ping \u00a7f%dms", latency);
                int width6 = RenderManager.textWidth(text);
                RenderManager.renderText(event.getContext(), text, (float)(res.getScaledWidth() - width6) - 1.0f, this.renderingUp ? this.bottomRight : this.topRight, this.getHudColor(this.rainbowOffset));
                if (this.renderingUp) {
                    this.bottomRight -= 9.0f;
                } else {
                    this.topRight += 9.0f;
                }
                ++this.rainbowOffset;
            }
            if (this.tpsConfig.getValue().booleanValue()) {
                float curr = Managers.TICK.getTpsCurrent();
                float avg = Managers.TICK.getTpsAverage();
                String text = String.format("TPS \u00a7f%s \u00a77[\u00a7f%s\u00a77]", this.decimal.format(avg), this.decimal.format(curr));
                int width7 = RenderManager.textWidth(text);
                RenderManager.renderText(event.getContext(), text, (float)(res.getScaledWidth() - width7) - 1.0f, this.renderingUp ? this.bottomRight : this.topRight, this.getHudColor(this.rainbowOffset));
                if (this.renderingUp) {
                    this.bottomRight -= 9.0f;
                } else {
                    this.topRight += 9.0f;
                }
                ++this.rainbowOffset;
            }
            if (this.fpsConfig.getValue().booleanValue()) {
                String text = String.format("FPS \u00a7f%d", mc.getCurrentFps());
                int width8 = RenderManager.textWidth(text);
                RenderManager.renderText(event.getContext(), text, (float)(res.getScaledWidth() - width8) - 1.0f, this.renderingUp ? this.bottomRight : this.topRight, this.getHudColor(this.rainbowOffset));
                ++this.rainbowOffset;
            }
            if (this.coordsConfig.getValue().booleanValue()) {
                double x = HUDModule.mc.player.getX();
                double y = HUDModule.mc.player.getY();
                double z = HUDModule.mc.player.getZ();
                boolean nether = HUDModule.mc.world.getRegistryKey() == World.NETHER;
                RenderManager.renderText(event.getContext(), String.format("XYZ \u00a7f%s, %s, %s " + (this.netherCoordsConfig.getValue() ? "\u00a77[\u00a7f%s, %s\u00a77]" : ""), this.decimal.format(x), this.decimal.format(y), this.decimal.format(z), nether ? this.decimal.format(x * 8.0) : this.decimal.format(x / 8.0), nether ? this.decimal.format(z * 8.0) : this.decimal.format(z / 8.0)), 2.0f, this.bottomLeft, this.getHudColor(this.rainbowOffset));
                this.bottomLeft -= 9.0f;
                ++this.rainbowOffset;
            }
            if (this.directionConfig.getValue().booleanValue()) {
                Direction direction = HUDModule.mc.player.getHorizontalFacing();
                String dir = EnumFormatter.formatDirection(direction);
                String axis = EnumFormatter.formatAxis(direction.getAxis());
                boolean pos = direction.getDirection() == Direction.AxisDirection.POSITIVE;
                RenderManager.renderText(event.getContext(), String.format("%s \u00a77[\u00a7f%s%s\u00a77]", dir, axis, pos ? "+" : "-"), 2.0f, this.bottomLeft, this.getHudColor(this.rainbowOffset));
                ++this.rainbowOffset;
            }
            if (this.armorConfig.getValue().booleanValue()) {
                Entity riding = HUDModule.mc.player.getVehicle();
                int x = res.getScaledWidth() / 2 - 7;
                int y = res.getScaledHeight();
                int n1 = HUDModule.mc.player.getMaxAir();
                int n2 = Math.min(HUDModule.mc.player.getAir(), n1);
                if (HUDModule.mc.player.isSubmergedIn(FluidTags.WATER) || n2 < n1) {
                    y -= 65;
                } else if (riding instanceof LivingEntity entity) {
                    y -= 45 + (int)Math.ceil((entity.getMaxHealth() - 1.0f) / 20.0f) * 10;
                } else {
                    y = riding != null ? (y -= 45) : (y -= HUDModule.mc.player.isCreative() ? (HUDModule.mc.player.isRiding() ? 45 : 38) : 55);
                }
                for (int i = 3; i >= 0; --i) {
                    ItemStack armor = HUDModule.mc.player.getInventory().armor.get(i);
                    int damage = EntityUtil.getDamagePercent(armor);
                    x += 20;
                    if (damage <= 0) continue;
                    event.getContext().drawItem(armor, x, y);
                    event.getContext().drawItemInSlot(HUDModule.mc.textRenderer, armor, x, y);
                    RenderManager.renderText(event.getContext(), String.valueOf(damage), (float)(x + 8) - (float)RenderManager.textWidth(String.valueOf(damage)) / 2.0f, y - 10, new Color((int)(255.0f * (1.0f - (float)damage / 100.0f)), (int)(255.0f * ((float)damage / 100.0f)), 0).getRGB());
                }
            }
            if (this.totemConfig.getValue().booleanValue()) {
                Entity riding = HUDModule.mc.player.getVehicle();
                int x = res.getScaledWidth() / 2 - 189 + 180 + 2;
                int y = res.getScaledHeight();
                int n1 = HUDModule.mc.player.getMaxAir();
                int n2 = Math.min(HUDModule.mc.player.getAir(), n1);
                if (HUDModule.mc.player.isSubmergedIn(FluidTags.WATER) || n2 < n1) {
                    y -= 65;
                } else if (riding instanceof LivingEntity entity) {
                    y -= 45 + (int)Math.ceil((entity.getMaxHealth() - 1.0f) / 20.0f) * 10;
                } else {
                    y = riding != null ? (y -= 45) : (y -= HUDModule.mc.player.isCreative() ? (HUDModule.mc.player.isRiding() ? 45 : 38) : 55);
                }
                int totems = InventoryUtil.count(Items.TOTEM_OF_UNDYING);
                if (totems > 0) {
                    event.getContext().drawItem(totem, x, y);
                    event.getContext().drawItemInSlot(HUDModule.mc.textRenderer, totem, x, y, String.valueOf(totems));
                }
            }
        }
    }

    @EventListener
    public void onChatOpen(ScreenOpenEvent event) {
        if (event.getScreen() == null && this.chatOpenAnimation.getState()) {
            this.chatOpenAnimation.setState(false);
        } else if (event.getScreen() instanceof ChatScreen) {
            this.chatOpenAnimation.setState(true);
        }
    }

    @EventListener
    public void onRenderOverlayStatusEffect(RenderOverlayEvent.StatusEffect event) {
        if (this.potionHudConfig.getValue() == VanillaHud.HIDE) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayItemName(RenderOverlayEvent.ItemName event) {
        if (this.itemNameConfig.getValue() != VanillaHud.KEEP) {
            event.cancel();
        }
        if (this.itemNameConfig.getValue() == VanillaHud.MOVE) {
            boolean armor;
            Window window = mc.getWindow();
            int x = window.getScaledWidth() / 2 - 90;
            int y = window.getScaledHeight() - 49;
            boolean bl = armor = !HUDModule.mc.player.getInventory().armor.isEmpty();
            if (HUDModule.mc.player.getAbsorptionAmount() > 0.0f) {
                y -= 9;
            }
            if (armor) {
                y -= 9;
            }
            event.setX(x);
            event.setY(y);
        }
    }

    private int getHudColor(int rainbowOffset) {
        return switch (this.rainbowModeConfig.getValue()) {
            default -> throw new IncompatibleClassChangeError();
            case OFF -> Modules.CLIENT_SETTING.getRGB();
            case STATIC -> this.rainbow(1L);
            case GRADIENT -> this.rainbow(rainbowOffset);
            case ALPHA -> this.alpha(rainbowOffset);
        };
    }

    private String getFormattedModule(Module module) {
        String metadata = module.getModuleData();
        if (metadata != null && !metadata.equals("ARRAYLIST_INFO")) {
            return String.format("%s \u00a77[\u00a7f%s\u00a77]", module.getName(), module.getModuleData());
        }
        return module.getName();
    }

    private int rainbow(long offset) {
        float hue = (float)(((double)System.currentTimeMillis() * (double)(this.rainbowSpeedConfig.getValue().floatValue() / 10.0f) + (double)(offset * 500L)) % (double)(30000.0f / (this.rainbowDifferenceConfig.getValue().floatValue() / 100.0f)) / (double)(30000.0f / (this.rainbowDifferenceConfig.getValue().floatValue() / 20.0f)));
        return Color.HSBtoRGB(hue, (float)this.rainbowSaturationConfig.getValue().intValue() / 100.0f, (float)this.rainbowBrightnessConfig.getValue().intValue() / 100.0f);
    }

    public int alpha(long offset) {
        offset = offset * 2L + 10L;
        float[] hsb = new float[3];
        Color color = Modules.CLIENT_SETTING.getColor();
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs(((float)(System.currentTimeMillis() % 2000L) / this.alphaSpeedConfig.getValue().floatValue() + 50.0f / (float)offset * 2.0f) % 2.0f - 1.0f);
        brightness = 0.5f + 0.5f * brightness;
        hsb[2] = brightness % 2.0f;
        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

    public float getChatAnimation() {
        return (float)this.chatOpenAnimation.getFactor();
    }

    public enum VanillaHud {
        MOVE,
        HIDE,
        KEEP

    }

    public enum Ordering {
        LENGTH,
        ALPHABETICAL

    }

    public enum Rendering {
        UP,
        DOWN

    }

    public enum RainbowMode {
        OFF,
        GRADIENT,
        STATIC,
        ALPHA

    }
}
