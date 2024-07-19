package dev.realme.ash.impl.module.client;

//import baritone.api.BaritoneAPI;
//import dev.realme.ash.api.config.Config;
//import dev.realme.ash.api.config.setting.BooleanConfig;
//import dev.realme.ash.api.config.setting.NumberConfig;
//import dev.realme.ash.api.event.EventStage;
//import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ConcurrentModule;
import dev.realme.ash.api.module.ModuleCategory;
//import dev.realme.ash.impl.event.TickEvent;

public class BaritoneModule
extends ConcurrentModule {
//    Config<Float> rangeConfig = new NumberConfig<Float>("Range", "Baritone block reach distance", Float.valueOf(1.0f), Float.valueOf(4.0f), Float.valueOf(5.0f));
//    Config<Boolean> placeConfig = new BooleanConfig("Place", "Allow baritone to place blocks", true);
//    Config<Boolean> breakConfig = new BooleanConfig("Break", "Allow baritone to break blocks", true);
//    Config<Boolean> sprintConfig = new BooleanConfig("Sprint", "Allow baritone to sprint", true);
//    Config<Boolean> inventoryConfig = new BooleanConfig("UseInventory", "Allow baritone to use player inventory", false);
//    Config<Boolean> vinesConfig = new BooleanConfig("Vines", "Allow baritone to climb vines", true);
//    Config<Boolean> jump256Config = new BooleanConfig("JumpAt256", "Allow baritone to jump at 256 blocks", false);
//    Config<Boolean> waterBucketFallConfig = new BooleanConfig("WaterBucketFall", "Allow baritone to use waterbuckets when falling", false);
//    Config<Boolean> parkourConfig = new BooleanConfig("Parkour", "Allow baritone to jump between blocks", true);
//    Config<Boolean> parkourPlaceConfig = new BooleanConfig("ParkourPlace", "Allow baritone to jump and place blocks", false);
//    Config<Boolean> parkourAscendConfig = new BooleanConfig("ParkourAscend", "Allow baritone to jump up blocks", true);
//    Config<Boolean> diagonalAscendConfig = new BooleanConfig("DiagonalAscend", "Allow baritone to jump up blocks diagonally", false);
//    Config<Boolean> diagonalDescendConfig = new BooleanConfig("DiagonalDescend", "Allow baritone to move down blocks diagonally", false);
//    Config<Boolean> mineDownConfig = new BooleanConfig("MineDownward", "Allow baritone to mine down", true);
//    Config<Boolean> legitMineConfig = new BooleanConfig("LegitMine", "Uses baritone legit mine", false);
//    Config<Boolean> logOnArrivalConfig = new BooleanConfig("LogOnArrival", "Logout when you arrive at destination", false);
//    Config<Boolean> freeLookConfig = new BooleanConfig("FreeLook", "Allows you to look around freely while using baritone", true);
//    Config<Boolean> antiCheatConfig = new BooleanConfig("AntiCheat", "Uses NCP placements and breaks", false);
//    Config<Boolean> strictLiquidConfig = new BooleanConfig("Strict-Liquid", "Uses strick liquid checks", false);
//    Config<Boolean> censorCoordsConfig = new BooleanConfig("CensorCoords", "Censors goal coordinates in chat", false);
//    Config<Boolean> censorCommandsConfig = new BooleanConfig("CensorCommands", "Censors baritone commands in chat", false);
//    Config<Boolean> debugConfig = new BooleanConfig("Debug", "Debugs in the chat", false);
//
    public BaritoneModule() {
        super("Baritone", "Configure baritone", ModuleCategory.CLIENT);
    }
//
//    @EventListener
//    public void onTick(TickEvent event) {
//        if (event.getStage() != EventStage.POST) {
//            return;
//        }
//        BaritoneAPI.getSettings().blockReachDistance.value = this.rangeConfig.getValue();
//        BaritoneAPI.getSettings().allowPlace.value = this.placeConfig.getValue();
//        BaritoneAPI.getSettings().allowBreak.value = this.breakConfig.getValue();
//        BaritoneAPI.getSettings().allowSprint.value = this.sprintConfig.getValue();
//        BaritoneAPI.getSettings().allowInventory.value = this.inventoryConfig.getValue();
//        BaritoneAPI.getSettings().allowVines.value = this.vinesConfig.getValue();
//        BaritoneAPI.getSettings().allowJumpAt256.value = this.jump256Config.getValue();
//        BaritoneAPI.getSettings().allowWaterBucketFall.value = this.waterBucketFallConfig.getValue();
//        BaritoneAPI.getSettings().allowParkour.value = this.parkourConfig.getValue();
//        BaritoneAPI.getSettings().allowParkourAscend.value = this.parkourAscendConfig.getValue();
//        BaritoneAPI.getSettings().allowParkourPlace.value = this.parkourPlaceConfig.getValue();
//        BaritoneAPI.getSettings().allowDiagonalAscend.value = this.diagonalAscendConfig.getValue();
//        BaritoneAPI.getSettings().allowDiagonalDescend.value = this.diagonalDescendConfig.getValue();
//        BaritoneAPI.getSettings().allowDownward.value = this.mineDownConfig.getValue();
//        BaritoneAPI.getSettings().legitMine.value = this.legitMineConfig.getValue();
//        BaritoneAPI.getSettings().disconnectOnArrival.value = this.logOnArrivalConfig.getValue();
//        BaritoneAPI.getSettings().freeLook.value = this.freeLookConfig.getValue();
//        BaritoneAPI.getSettings().antiCheatCompatibility.value = this.antiCheatConfig.getValue();
//        BaritoneAPI.getSettings().strictLiquidCheck.value = this.strictLiquidConfig.getValue();
//        BaritoneAPI.getSettings().censorCoordinates.value = this.censorCoordsConfig.getValue();
//        BaritoneAPI.getSettings().censorRanCommands.value = this.censorCommandsConfig.getValue();
//        BaritoneAPI.getSettings().chatDebug.value = this.debugConfig.getValue();
//    }
}
