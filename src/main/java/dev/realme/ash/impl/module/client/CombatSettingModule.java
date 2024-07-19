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
    public Config<Priority> priority = new EnumConfig("Priority", "", (Enum)Priority.HEALTH, (Enum[])Priority.values());
    public Config<MaxHeight> maxHeight = new EnumConfig("MaxHeight", "", (Enum)MaxHeight.New, (Enum[])MaxHeight.values());
    public Config<Placement> placement = new EnumConfig("Placement", "", (Enum)Placement.Vanilla, (Enum[])Placement.values());
    public Config<Float> placeRange = new NumberConfig<Float>("PlaceRange", "", Float.valueOf(0.0f), Float.valueOf(5.2f), Float.valueOf(6.0f));
    public Config<Boolean> packetPlace = new BooleanConfig("PacketPlace", "", true);
    public Config<Float> attackDelay = new NumberConfig<Float>("AttackDelay", "", Float.valueOf(0.0f), Float.valueOf(0.2f), Float.valueOf(5.0f));
    public Config<Float> rotateTime = new NumberConfig<Float>("RotateTime", "", Float.valueOf(0.0f), Float.valueOf(0.29f), Float.valueOf(2.0f));
    public Config<Boolean> movementFix = new BooleanConfig("MovementFix", "", false);
    public Config<SwingMode> swingMode = new EnumConfig("SwingMode", "", (Enum)SwingMode.Normal, (Enum[])SwingMode.values());
    public Config<Boolean> oldVersion = new BooleanConfig("OldVersion", "", false);
    public final Timer attackTimer = new CacheTimer();

    public CombatSettingModule() {
        super("CombatSetting", "Manages combat setting.", ModuleCategory.CLIENT);
    }

    public static enum Priority {
        FOV,
        HEALTH,
        DISTANCE,
        ARMOR;

    }

    public static enum MaxHeight {
        Old,
        New,
        Disabled;

    }

    public static enum Placement {
        Vanilla,
        Strict,
        AirPlace;

    }

    public static enum SwingMode {
        Normal,
        Client,
        Server,
        None;

    }
}
