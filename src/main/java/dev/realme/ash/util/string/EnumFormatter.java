package dev.realme.ash.util.string;

import net.minecraft.util.math.Direction;

public class EnumFormatter {
   public static String formatEnum(Enum in) {
      String name = in.name();
      if (!name.contains("_")) {
         char firstChar = name.charAt(0);
         String suffixChars = name.split(String.valueOf(firstChar), 2)[1];
         String var10000 = String.valueOf(firstChar).toUpperCase();
         return var10000 + suffixChars.toLowerCase();
      } else {
         String[] names = name.split("_");
         StringBuilder nameToReturn = new StringBuilder();
         String[] var4 = names;
         int var5 = names.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String n = var4[var6];
            char firstChar = n.charAt(0);
            String suffixChars = n.split(String.valueOf(firstChar), 2)[1];
            nameToReturn.append(String.valueOf(firstChar).toUpperCase()).append(suffixChars.toLowerCase());
         }

         return nameToReturn.toString();
      }
   }

   public static String formatDirection(Direction direction) {
      String var10000;
      switch (direction) {
         case UP -> var10000 = "Up";
         case DOWN -> var10000 = "Down";
         case NORTH -> var10000 = "North";
         case SOUTH -> var10000 = "South";
         case EAST -> var10000 = "East";
         case WEST -> var10000 = "West";
         default -> throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static String formatAxis(Direction.Axis axis) {
      String var10000;
      switch (axis) {
         case X -> var10000 = "X";
         case Y -> var10000 = "Y";
         case Z -> var10000 = "Z";
         default -> throw new IncompatibleClassChangeError();
      }

      return var10000;
   }
}
