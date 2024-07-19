package dev.realme.ash.impl.imixin;

import dev.realme.ash.util.network.InteractType;
import net.minecraft.entity.Entity;

public interface IPlayerInteractEntityC2SPacket {
   Entity getEntity();

   InteractType getType();
}
