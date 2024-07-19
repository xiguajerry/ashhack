package dev.realme.ash.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;

public interface Globals {
   MinecraftClient mc = MinecraftClient.getInstance();
   Random RANDOM = ThreadLocalRandom.current();
}
