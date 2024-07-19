package dev.realme.ash.util;

import org.lwjgl.glfw.GLFW;

public class KeyboardUtil {
   public static String getKeyName(int keycode, int scancode) {
      String var10000;
      switch (keycode) {
         case 32 -> var10000 = "SPACE";
         case 257 -> var10000 = "ENTER";
         case 258 -> var10000 = "TAB";
         case 259 -> var10000 = "BACKSPACE";
         case 260 -> var10000 = "INSERT";
         case 261 -> var10000 = "DELETE";
         case 262 -> var10000 = "RIGHT";
         case 263 -> var10000 = "LEFT";
         case 264 -> var10000 = "DOWN";
         case 265 -> var10000 = "UP";
         case 266 -> var10000 = "PAGE_UP";
         case 267 -> var10000 = "PAGE_DOWN";
         case 268 -> var10000 = "HOME";
         case 269 -> var10000 = "END";
         case 280 -> var10000 = "CAPS_LOCK";
         case 340 -> var10000 = "LSHIFT";
         case 341 -> var10000 = "LCONTROL";
         case 342 -> var10000 = "LALT";
         case 344 -> var10000 = "RSHIFT";
         case 345 -> var10000 = "RCONTROL";
         case 346 -> var10000 = "RALT";
         case 1000 -> var10000 = "MOUSE0";
         case 1001 -> var10000 = "MOUSE1";
         case 1002 -> var10000 = "MOUSE2";
         case 1003 -> var10000 = "MOUSE3";
         case 1004 -> var10000 = "MOUSE4";
         case 1005 -> var10000 = "MOUSE5";
         case 1006 -> var10000 = "MOUSE6";
         case 1007 -> var10000 = "MOUSE7";
         default -> var10000 = GLFW.glfwGetKeyName(keycode, scancode);
      }

      return var10000;
   }

   public static String getKeyName(int keycode) {
      return getKeyName(keycode, keycode < 1000 ? GLFW.glfwGetKeyScancode(keycode) : 0);
   }

   public static int getKeyCode(String key) {
      if (key.equalsIgnoreCase("NONE")) {
         return -1;
      } else {
         int i;
         for(i = 32; i < 97; ++i) {
            if (key.equalsIgnoreCase(getKeyName(i, GLFW.glfwGetKeyScancode(i)))) {
               return i;
            }
         }

         for(i = 256; i < 349; ++i) {
            if (key.equalsIgnoreCase(getKeyName(i, GLFW.glfwGetKeyScancode(i)))) {
               return i;
            }
         }

         for(i = 1000; i < 1010; ++i) {
            if (key.equalsIgnoreCase(getKeyName(i, GLFW.glfwGetKeyScancode(i)))) {
               return i;
            }
         }

         return -1;
      }
   }
}
