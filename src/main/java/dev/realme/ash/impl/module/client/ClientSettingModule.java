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
    Config<SuffixMode> mode = new EnumConfig("ChatSuffix", "", (Enum)SuffixMode.None, (Enum[])SuffixMode.values());
    public Config<Boolean> autoAnswer = new BooleanConfig("AutoQueue", "", false);
    public Config<Boolean> animation = new BooleanConfig("ChatAnimation", "Animates the chat", false);
    public Config<Integer> animationTime = new NumberConfig<Integer>("AnimationTime", "Time for the animation", 0, 200, 1000);
    public Config<Integer> animationOffset = new NumberConfig<Integer>("AnimationOffset", "", -500, 100, 500);
    public Config<Boolean> shadow = new BooleanConfig("TextShadow", "", true);
    public Config<Boolean> aspectRatio = new BooleanConfig("AspectRatio", "", false);
    public Config<Float> ratio = new NumberConfig<Float>("Ratio", "", Float.valueOf(0.1f), Float.valueOf(1.78f), Float.valueOf(5.0f));
    Config<Color> colorConfig = new ColorConfig("Color", "The primary client color", new Color(150, 0, 255), false, false);
    boolean ycsm = false;
    HashMap<String, String> dawd = new HashMap<String, String>(){
        {
            this.put("\u7ea2\u77f3\u706b\u628a", "15");
            this.put("\u732a\u88ab\u95ea\u7535", "\u50f5\u5c38\u732a\u4eba");
            this.put("\u5c0f\u7bb1\u5b50", "27");
            this.put("\u5f00\u670d\u5e74\u4efd", "2020");
            this.put("\u5b9a\u4f4d\u672b\u5730\u9057\u8ff9", "intermediary");
            this.put("\u722c\u884c\u8005\u88ab\u95ea\u7535", "\u9ad8\u538b\u722c\u884c\u8005");
            this.put("\u5927\u7bb1\u5b50\u80fd", "54");
            this.put("\u7f8a\u9a7c\u4f1a\u4e3b\u52a8", "\u4e0d\u4f1a");
            this.put("\u65e0\u9650\u6c34", "3");
            this.put("\u6316\u6398\u901f\u5ea6\u6700\u5feb", "\u91d1\u9550");
            this.put("\u51cb\u96f6\u6b7b\u540e", "\u4e0b\u754c\u4e4b\u661f");
            this.put("\u82e6\u529b\u6015\u7684\u5b98\u65b9", "\u722c\u884c\u8005");
            this.put("\u5357\u74dc\u7684\u751f\u957f", "\u4e0d\u9700\u8981");
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
        if (this.autoAnswer.getValue().booleanValue() && InventoryUtil.findItem(Items.COMPASS) != -1 && (object = event.getPacket()) instanceof GameMessageS2CPacket) {
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
        event.message = (String) (message = (String)message + suffix);
    }

    private String getSuffix() {
        return switch (this.mode.getValue()) {
            default -> throw new IncompatibleClassChangeError();
            case None -> null;
            case Ash -> " \ud83d\udd25\u2090\u209b\u2095";
            case NullPoint -> " \ud835\udd2b\ud835\udd32\ud835\udd29\ud835\udd29\ud835\udd2d\ud835\udd2c\ud835\udd26\ud835\udd2b\ud835\udd31";
            case Eclipse -> " \u263d\u2097\u1d64\u2099\u2090\u1d63\u2091\ud835\udcec\u2097\u1d62\u209a\u209b\u2091";
            case Active -> " \ud83d\udd2f\ud835\udc9c\ud835\udcb8\ud835\udcc9\ud835\udcbe\ud835\udccb\u212f";
            case MoonGod -> " \ud835\ude7c\ud835\ude98\ud835\ude98\ud835\ude97\ud835\ude76\ud835\ude98\ud835\ude8d";
            case Troll -> " \uff34\uff32\uff2f\uff2c\uff2c\u3000\uff28\uff21\uff23\uff2b";
            case Melon -> " \u2c98\u2c89\ud835\udcf5\u2c9f\u2c9b";
            case OnePlusOne -> " \u26231+1\u2623";
            case Penis -> " \u2c9f\ud835\udcf5\u2c9f";
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

    public static enum SuffixMode {
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
        Penis;

    }
}
