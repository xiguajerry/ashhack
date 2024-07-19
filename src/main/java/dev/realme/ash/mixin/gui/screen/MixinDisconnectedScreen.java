package dev.realme.ash.mixin.gui.screen;

import dev.realme.ash.util.Globals;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({DisconnectedScreen.class})
public abstract class MixinDisconnectedScreen extends MixinScreen implements Globals {
}
