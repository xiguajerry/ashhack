package dev.realme.ash.impl.manager;

import dev.realme.ash.Ash;
import dev.realme.ash.AshMod;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.impl.module.client.BaritoneModule;
import dev.realme.ash.impl.module.client.CapesModule;
import dev.realme.ash.impl.module.client.ClickGuiModule;
import dev.realme.ash.impl.module.client.ClientSettingModule;
import dev.realme.ash.impl.module.client.CombatSettingModule;
import dev.realme.ash.impl.module.client.HUDModule;
import dev.realme.ash.impl.module.client.ServerModule;
import dev.realme.ash.impl.module.combat.AuraModule;
import dev.realme.ash.impl.module.combat.AutoAnchorModule;
import dev.realme.ash.impl.module.combat.AutoArmorModule;
import dev.realme.ash.impl.module.combat.AutoBowReleaseModule;
import dev.realme.ash.impl.module.combat.AutoCrystalModule;
import dev.realme.ash.impl.module.combat.AutoLogModule;
import dev.realme.ash.impl.module.combat.AutoMineModule;
import dev.realme.ash.impl.module.combat.AutoPotModule;
import dev.realme.ash.impl.module.combat.AutoTotemModule;
import dev.realme.ash.impl.module.combat.AutoWebModule;
import dev.realme.ash.impl.module.combat.AutoXPModule;
import dev.realme.ash.impl.module.combat.BowAimModule;
import dev.realme.ash.impl.module.combat.BurrowModule;
import dev.realme.ash.impl.module.combat.CevBreakerModule;
import dev.realme.ash.impl.module.combat.ClickCrystalModule;
import dev.realme.ash.impl.module.combat.CriticalsModule;
import dev.realme.ash.impl.module.combat.FlattenModule;
import dev.realme.ash.impl.module.combat.HeadTrapModule;
import dev.realme.ash.impl.module.combat.HolePushModule;
import dev.realme.ash.impl.module.combat.OffhandModule;
import dev.realme.ash.impl.module.combat.ReplenishModule;
import dev.realme.ash.impl.module.combat.SelfBounceModule;
import dev.realme.ash.impl.module.combat.SelfBowModule;
import dev.realme.ash.impl.module.combat.SurroundModule;
import dev.realme.ash.impl.module.combat.TriggerModule;
import dev.realme.ash.impl.module.exploit.AntiHungerModule;
import dev.realme.ash.impl.module.exploit.ChorusControlModule;
import dev.realme.ash.impl.module.exploit.ClientSpoofModule;
import dev.realme.ash.impl.module.exploit.CrasherModule;
import dev.realme.ash.impl.module.exploit.DisablerModule;
import dev.realme.ash.impl.module.exploit.ExtendedFireworkModule;
import dev.realme.ash.impl.module.exploit.FakeLatencyModule;
import dev.realme.ash.impl.module.exploit.FastLatencyModule;
import dev.realme.ash.impl.module.exploit.FastProjectileModule;
import dev.realme.ash.impl.module.exploit.PacketCancelerModule;
import dev.realme.ash.impl.module.exploit.PacketFlyModule;
import dev.realme.ash.impl.module.exploit.PhaseModule;
import dev.realme.ash.impl.module.exploit.PortalGodModeModule;
import dev.realme.ash.impl.module.exploit.ReachModule;
import dev.realme.ash.impl.module.misc.AntiAimModule;
import dev.realme.ash.impl.module.misc.AntiSpamModule;
import dev.realme.ash.impl.module.misc.AutoAcceptModule;
import dev.realme.ash.impl.module.misc.AutoEatModule;
import dev.realme.ash.impl.module.misc.AutoFishModule;
import dev.realme.ash.impl.module.misc.AutoReconnectModule;
import dev.realme.ash.impl.module.misc.AutoRespawnModule;
import dev.realme.ash.impl.module.misc.AutoTauntModule;
import dev.realme.ash.impl.module.misc.BeaconSelectorModule;
import dev.realme.ash.impl.module.misc.ChatNotifierModule;
import dev.realme.ash.impl.module.misc.ChestStealerModule;
import dev.realme.ash.impl.module.misc.ChestSwapModule;
import dev.realme.ash.impl.module.misc.FakePlayerModule;
import dev.realme.ash.impl.module.misc.InvCleanerModule;
import dev.realme.ash.impl.module.misc.MiddleClickModule;
import dev.realme.ash.impl.module.misc.NoPacketKickModule;
import dev.realme.ash.impl.module.misc.NoSoundLagModule;
import dev.realme.ash.impl.module.misc.NukerModule;
import dev.realme.ash.impl.module.misc.PacketEatModule;
import dev.realme.ash.impl.module.misc.PacketLoggerModule;
import dev.realme.ash.impl.module.misc.SpammerModule;
import dev.realme.ash.impl.module.misc.TimerModule;
import dev.realme.ash.impl.module.misc.TrueDurabilityModule;
import dev.realme.ash.impl.module.misc.UnfocusedFPSModule;
import dev.realme.ash.impl.module.misc.XCarryModule;
import dev.realme.ash.impl.module.movement.AntiLevitationModule;
import dev.realme.ash.impl.module.movement.AntiWebModule;
import dev.realme.ash.impl.module.movement.AutoWalkModule;
import dev.realme.ash.impl.module.movement.BurrowStrafeModule;
import dev.realme.ash.impl.module.movement.ElytraFlyModule;
import dev.realme.ash.impl.module.movement.EntityControlModule;
import dev.realme.ash.impl.module.movement.EntitySpeedModule;
import dev.realme.ash.impl.module.movement.FakeLagModule;
import dev.realme.ash.impl.module.movement.FastFallModule;
import dev.realme.ash.impl.module.movement.FlightModule;
import dev.realme.ash.impl.module.movement.IceSpeedModule;
import dev.realme.ash.impl.module.movement.JesusModule;
import dev.realme.ash.impl.module.movement.LongJumpModule;
import dev.realme.ash.impl.module.movement.NoFallModule;
import dev.realme.ash.impl.module.movement.NoJumpDelayModule;
import dev.realme.ash.impl.module.movement.NoSlowModule;
import dev.realme.ash.impl.module.movement.ParkourModule;
import dev.realme.ash.impl.module.movement.SpeedModule;
import dev.realme.ash.impl.module.movement.SprintModule;
import dev.realme.ash.impl.module.movement.StepModule;
import dev.realme.ash.impl.module.movement.StrafeModule;
import dev.realme.ash.impl.module.movement.TickShiftModule;
import dev.realme.ash.impl.module.movement.TridentFlyModule;
import dev.realme.ash.impl.module.movement.VelocityModule;
import dev.realme.ash.impl.module.movement.YawModule;
import dev.realme.ash.impl.module.render.BlockHighlightModule;
import dev.realme.ash.impl.module.render.BreakHighlightModule;
import dev.realme.ash.impl.module.render.ChamsModule;
import dev.realme.ash.impl.module.render.ESPModule;
import dev.realme.ash.impl.module.render.ExtraTabModule;
import dev.realme.ash.impl.module.render.FreecamModule;
import dev.realme.ash.impl.module.render.FullbrightModule;
import dev.realme.ash.impl.module.render.HoleESPModule;
import dev.realme.ash.impl.module.render.NameProtectModule;
import dev.realme.ash.impl.module.render.NametagsModule;
import dev.realme.ash.impl.module.render.NoRenderModule;
import dev.realme.ash.impl.module.render.NoRotateModule;
import dev.realme.ash.impl.module.render.NoWeatherModule;
import dev.realme.ash.impl.module.render.ParticlesModule;
import dev.realme.ash.impl.module.render.PhaseESPModule;
import dev.realme.ash.impl.module.render.PlaceRenderModule;
import dev.realme.ash.impl.module.render.PopChamsModule;
import dev.realme.ash.impl.module.render.SkeletonModule;
import dev.realme.ash.impl.module.render.SkyboxModule;
import dev.realme.ash.impl.module.render.TooltipsModule;
import dev.realme.ash.impl.module.render.TracersModule;
import dev.realme.ash.impl.module.render.TrueSightModule;
import dev.realme.ash.impl.module.render.ViewClipModule;
import dev.realme.ash.impl.module.render.ViewModelModule;
import dev.realme.ash.impl.module.render.WaypointsModule;
import dev.realme.ash.impl.module.world.AntiInteractModule;
import dev.realme.ash.impl.module.world.AutoToolModule;
import dev.realme.ash.impl.module.world.AvoidModule;
import dev.realme.ash.impl.module.world.BlockInteractModule;
import dev.realme.ash.impl.module.world.FastDropModule;
import dev.realme.ash.impl.module.world.FastPlaceModule;
import dev.realme.ash.impl.module.world.MultitaskModule;
import dev.realme.ash.impl.module.world.NoGlitchBlocksModule;
import dev.realme.ash.impl.module.world.PacketDiggingModule;
import dev.realme.ash.impl.module.world.ScaffoldModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
   private final Map modules = Collections.synchronizedMap(new LinkedHashMap());

   public ModuleManager() {
      if (AshMod.isBaritonePresent()) {
         this.register((Module)(new BaritoneModule()));
      }

      this.register(new CapesModule(), new ClickGuiModule(), new ClientSettingModule(), new CombatSettingModule(), new HUDModule(), new ServerModule(), new AuraModule(), new AutoAnchorModule(), new AutoArmorModule(), new AutoBowReleaseModule(), new AutoCrystalModule(), new AutoLogModule(), new AutoMineModule(), new AutoPotModule(), new AutoTotemModule(), new AutoWebModule(), new AutoXPModule(), new BowAimModule(), new BurrowModule(), new CevBreakerModule(), new ClickCrystalModule(), new CriticalsModule(), new FlattenModule(), new HeadTrapModule(), new HolePushModule(), new OffhandModule(), new ReplenishModule(), new SelfBounceModule(), new SelfBowModule(), new SurroundModule(), new TriggerModule(), new AntiHungerModule(), new ChorusControlModule(), new ClientSpoofModule(), new CrasherModule(), new DisablerModule(), new ExtendedFireworkModule(), new FakeLatencyModule(), new FastLatencyModule(), new FastProjectileModule(), new PacketCancelerModule(), new PacketFlyModule(), new PhaseModule(), new PortalGodModeModule(), new ReachModule(), new AntiAimModule(), new AntiSpamModule(), new AutoAcceptModule(), new AutoEatModule(), new AutoFishModule(), new AutoReconnectModule(), new AutoRespawnModule(), new AutoTauntModule(), new BeaconSelectorModule(), new ChatNotifierModule(), new ChestStealerModule(), new ChestSwapModule(), new FakePlayerModule(), new InvCleanerModule(), new MiddleClickModule(), new NoPacketKickModule(), new NoSoundLagModule(), new NukerModule(), new PacketEatModule(), new PacketLoggerModule(), new SpammerModule(), new TimerModule(), new TrueDurabilityModule(), new UnfocusedFPSModule(), new XCarryModule(), new AntiLevitationModule(), new AntiWebModule(), new AutoWalkModule(), new BurrowStrafeModule(), new ElytraFlyModule(), new EntityControlModule(), new EntitySpeedModule(), new FakeLagModule(), new FastFallModule(), new FlightModule(), new IceSpeedModule(), new JesusModule(), new LongJumpModule(), new NoFallModule(), new NoJumpDelayModule(), new NoSlowModule(), new ParkourModule(), new SpeedModule(), new SprintModule(), new StepModule(), new StrafeModule(), new TickShiftModule(), new TridentFlyModule(), new VelocityModule(), new YawModule(), new BlockHighlightModule(), new BreakHighlightModule(), new ChamsModule(), new ESPModule(), new ExtraTabModule(), new FreecamModule(), new FullbrightModule(), new HoleESPModule(), new NameProtectModule(), new NametagsModule(), new NoRenderModule(), new NoRotateModule(), new NoWeatherModule(), new ParticlesModule(), new PhaseESPModule(), new PlaceRenderModule(), new PopChamsModule(), new SkeletonModule(), new SkyboxModule(), new TooltipsModule(), new TracersModule(), new TrueSightModule(), new ViewClipModule(), new ViewModelModule(), new WaypointsModule(), new AntiInteractModule(), new AutoToolModule(), new AvoidModule(), new BlockInteractModule(), new FastDropModule(), new FastPlaceModule(), new MultitaskModule(), new NoGlitchBlocksModule(), new PacketDiggingModule(), new ScaffoldModule());
      Ash.info("Registered {} modules!", this.modules.size());
   }

   public void postInit() {
   }

   private void register(Module... modules) {
      Module[] var2 = modules;
      int var3 = modules.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Module module = var2[var4];
         this.register(module);
      }

   }

   private void register(Module module) {
      this.modules.put(module.getId(), module);
   }

   public Module getModule(String id) {
      return (Module)this.modules.get(id);
   }

   public List<Module> getModules() {
      return new ArrayList(this.modules.values());
   }
}
