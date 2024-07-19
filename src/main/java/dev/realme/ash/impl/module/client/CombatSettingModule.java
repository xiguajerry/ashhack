package dev.realme.ash.impl.module.client;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.module.ConcurrentModule;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;

public class CombatSettingModule
extends ConcurrentModule {
    public Config<Priority> priority = new EnumConfig("Priority", "", Priority.HEALTH, Priority.values());
    public Config<MaxHeight> maxHeight = new EnumConfig("MaxHeight", "", MaxHeight.New, MaxHeight.values());
    public Config<Placement> placement = new EnumConfig("Placement", "", Placement.Vanilla, Placement.values());
    public Config<Float> placeRange = new NumberConfig<Float>("PlaceRange", "", 0.0f, 5.2f, 6.0f);
    public Config<Boolean> packetPlace = new BooleanConfig("PacketPlace", "", true);
    public Config<Float> attackDelay = new NumberConfig<Float>("AttackDelay", "", 0.0f, 0.2f, 5.0f);
    public Config<Float> rotateTime = new NumberConfig<Float>("RotateTime", "", 0.0f, 0.29f, 2.0f);
    public Config<Boolean> movementFix = new BooleanConfig("MovementFix", "", false);
    public Config<SwingMode> swingMode = new EnumConfig("SwingMode", "", SwingMode.Normal, SwingMode.values());
    public Config<Boolean> oldVersion = new BooleanConfig("OldVersion", "", false);
    public final Timer attackTimer = new CacheTimer();

    public CombatSettingModule() {
        super("CombatSetting", "Manages combat setting.", ModuleCategory.CLIENT);
    }

    public enum Priority {
        FOV,
        HEALTH,
        DISTANCE,
        ARMOR

    }

    public enum MaxHeight {
        Old,
        New,
        Disabled

    }

    public enum Placement {
        Vanilla,
        Strict,
        AirPlace

    }

    public enum SwingMode {
        Normal,
        Client,
        Server,
        None

    }
}
