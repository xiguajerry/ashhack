package dev.realme.ash.impl.module.client;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ConcurrentModule;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.impl.event.gui.chat.SendMessageEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.util.chat.ChatUtil;
import dev.realme.ash.util.player.InventoryUtil;
import java.awt.Color;
import java.util.HashMap;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class ClientSettingModule
extends ConcurrentModule {
    final Config<SuffixMode> mode = new EnumConfig<>("ChatSuffix", "", SuffixMode.None, SuffixMode.values());
    public final Config<Boolean> autoAnswer = new BooleanConfig("AutoQueue", "", false);
    public final Config<Boolean> animation = new BooleanConfig("ChatAnimation", "Animates the chat", false);
    public final Config<Integer> animationTime = new NumberConfig<>("AnimationTime", "Time for the animation", 0, 200, 1000);
    public final Config<Integer> animationOffset = new NumberConfig<>("AnimationOffset", "", -500, 100, 500);
    public final Config<Boolean> shadow = new BooleanConfig("TextShadow", "", true);
    public final Config<Boolean> aspectRatio = new BooleanConfig("AspectRatio", "", false);
    public final Config<Float> ratio = new NumberConfig<>("Ratio", "", 0.1f, 1.78f, 5.0f);
    final Config<Color> colorConfig = new ColorConfig("Color", "The primary client color", new Color(150, 0, 255), false, false);
    boolean ycsm = false;
    final HashMap<String, String> dawd = new HashMap<>() {
        {
            this.put("红石火把", "15");
            this.put("猪被闪电", "僵尸猪人");
            this.put("小箱子", "27");
            this.put("开服年份", "2020");
            this.put("定位末地遗迹", "intermediary");
            this.put("爬行者被闪电", "高压爬行者");
            this.put("大箱子能", "54");
            this.put("羊驼会主动", "不会");
            this.put("无限水", "3");
            this.put("挖掘速度最快", "金镐");
            this.put("凋零死后", "下界之星");
            this.put("苦力怕的官方", "爬行者");
            this.put("南瓜的生长", "不需要");
        }
    };

    public ClientSettingModule() {
        super("ClientSetting", "Client setting.", ModuleCategory.CLIENT);
    }

    @EventListener
    public void ZJMISGAY(PacketEvent.Receive event) {
        Object object;
        if (ClientSettingModule.nullCheck()) {
            return;
        }
        if (this.autoAnswer.getValue() && InventoryUtil.findItem(Items.COMPASS) != -1 && (object = event.getPacket()) instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket gameMessageS2CPacket = (GameMessageS2CPacket)object;
            for (String key : this.dawd.keySet()) {
                if (!gameMessageS2CPacket.content().getString().contains(key)) continue;
                String zjm = gameMessageS2CPacket.content().getString();
                String zjm2 = this.dawd.get(key);
                this.ycsm = true;
                if (zjm.contains("A." + zjm2)) {
                    ChatUtil.serverSendMessage("A.");
                }
                if (zjm.contains("B." + zjm2)) {
                    ChatUtil.serverSendMessage("B.");
                }
                if (zjm.contains("C." + zjm2)) {
                    ChatUtil.serverSendMessage("C.");
                }
                this.ycsm = false;
            }
        }
    }

    @EventListener
    public void onSendMessage(SendMessageEvent event) {
        if (ClientSettingModule.nullCheck() || event.isCanceled()) {
            return;
        }
        Object message = event.message;
        if (((String)message).startsWith("/") && !((String)message).startsWith("/msg") || ((String)message).startsWith("!") || ((String)message).startsWith(".")) {
            return;
        }
        if (this.ycsm) {
            return;
        }
        String suffix = this.getSuffix();
        if (suffix == null) {
            return;
        }
        event.message = (String) (message = message + suffix);
    }

    private String getSuffix() {
        return switch (this.mode.getValue()) {
            default -> throw new IncompatibleClassChangeError();
            case None -> null;
            case Ash -> " \ud83d\udd25ₐₛₕ";
            case NullPoint -> " \ud835\udd2b\ud835\udd32\ud835\udd29\ud835\udd29\ud835\udd2d\ud835\udd2c\ud835\udd26\ud835\udd2b\ud835\udd31";
            case Eclipse -> " ☽ₗᵤₙₐᵣₑ\ud835\udcecₗᵢₚₛₑ";
            case Active -> " \ud83d\udd2f\ud835\udc9c\ud835\udcb8\ud835\udcc9\ud835\udcbe\ud835\udccbℯ";
            case MoonGod -> " \ud835\ude7c\ud835\ude98\ud835\ude98\ud835\ude97\ud835\ude76\ud835\ude98\ud835\ude8d";
            case Troll -> " ＴＲＯＬＬ\u3000ＨＡＣＫ";
            case Melon -> " Ⲙⲉ\ud835\udcf5ⲟⲛ";
            case OnePlusOne -> " ☣1+1☣";
            case Penis -> " ⲟ\ud835\udcf5ⲟ";
            case MelonBeta -> " \ud835\udd10\ud835\udd22\ud835\udd29\ud835\udd2c\ud835\udd2b\ud835\udd05\ud835\udd22\ud835\udd31\ud835\udd1e";
        };
    }

    public Color getColor() {
        return this.colorConfig.getValue();
    }

    public Color getColor(float alpha) {
        ColorConfig config = (ColorConfig)this.colorConfig;
        return new Color((float)config.getRed() / 255.0f, (float)config.getGreen() / 255.0f, (float)config.getBlue() / 255.0f, alpha);
    }

    public Color getColor(int alpha) {
        ColorConfig config = (ColorConfig)this.colorConfig;
        return new Color(config.getRed(), config.getGreen(), config.getBlue(), alpha);
    }

    public Integer getRGB() {
        return this.getColor().getRGB();
    }

    public int getRGB(int a) {
        return this.getColor(a).getRGB();
    }

    public enum SuffixMode {
        None,
        Ash,
        NullPoint,
        Eclipse,
        Active,
        Melon,
        MelonBeta,
        Troll,
        MoonGod,
        OnePlusOne,
        Penis

    }
}
