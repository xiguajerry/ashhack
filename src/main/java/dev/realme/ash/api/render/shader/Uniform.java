package dev.realme.ash.api.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;

public class Uniform {
   private final String name;
   private int id;
   private Object value;

   public Uniform(String name) {
      this.name = name;
   }

   public void init(int programId) {
      this.id = GlStateManager._glGetUniformLocation(programId, this.name);
   }

   public void set(Object value) {
      this.value = value;
   }

   public Object get() {
      return this.value;
   }

   public int getId() {
      return this.id;
   }
}
