package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.config.setting.StringConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.entity.EntityDeathEvent;
import dev.realme.ash.init.Managers;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;

public class AutoTauntModule
extends ToggleModule {
    Config<Mode> mode = new EnumConfig<>("Mode", "", Mode.Custom, Mode.values());
    Config<Float> range = new NumberConfig<>("Range", "", 0.0f, 13.0f, 100.0f);
    StringConfig text = new StringConfig("Text", "", "bye");
    public List<String> list = List.of(
            "鼠标明天到，触摸板打的", "领养", "收徒", "不收徒", "有真人吗",
            "墨镜上车", "素材局", "不接单", "接单", "征婚", "4399?", "暂时不考虑打职业",
            "bot?", "叫你家大人来打", "假肢上门安装", "浪费我的网费", "不收残疾人", "下课",
            "自己找差距", "不接代", "代+", "这样的治好了也流口水", "人机", "人机怎么调难度啊",
            "只收不被0封的", "Bot吗这是", "领养", "纳亲", "正视差距", "近亲繁殖?",
            "我玩的是新手教程?", "来调灵敏度的", "来调参数的", "小号", "不是本人别加",
            "下次记得晚点玩", "随便玩玩,不带妹", "扣1上车");
    Random random = new Random();

    public AutoTauntModule() {
        super("AutoTaunt", "zjm.mp3", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onDeath(EntityDeathEvent event) {
        if (AutoTauntModule.nullCheck()) {
            return;
        }
        PlayerEntity player = (PlayerEntity)event.getEntity();
        if (player != AutoTauntModule.mc.player && !Managers.SOCIAL.isFriend(player.getName())) {
            if (this.range.getValue() > 0.0f && AutoTauntModule.mc.player.distanceTo(player) > this.range.getValue()) {
                return;
            }
            switch (this.mode.getValue()) {
                case EZ: {
                    AutoTauntModule.mc.player.networkHandler.sendChatMessage(this.list.get(this.random.nextInt(this.list.size() - 1)) + " " + player.getName().getString());
                    break;
                }
                case Custom: {
                    AutoTauntModule.mc.player.networkHandler.sendChatMessage(this.text.getValue() + " " + player.getName().getString());
                }
            }
        }
    }

    public enum Mode {
        Custom,
        EZ

    }
}
