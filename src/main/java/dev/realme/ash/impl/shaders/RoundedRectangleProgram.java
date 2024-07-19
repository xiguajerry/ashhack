package dev.realme.ash.impl.shaders;

import dev.realme.ash.api.render.shader.Program;
import dev.realme.ash.api.render.shader.Shader;
import dev.realme.ash.api.render.shader.Uniform;
import dev.realme.ash.util.Globals;
import java.awt.Color;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.opengl.GL20;

public final class RoundedRectangleProgram extends Program implements Globals {
   final Uniform radius = new Uniform("radius");
   final Uniform softness = new Uniform("softness");
   final Uniform size = new Uniform("size");
   final Uniform color = new Uniform("color");

   public RoundedRectangleProgram() {
      super(new Shader("roundedrect.frag", 35632));
   }

   public void initUniforms() {
      this.radius.init(this.id);
      this.softness.init(this.id);
      this.size.init(this.id);
      this.color.init(this.id);
   }

   public void updateUniforms() {
      float SCALE_FACTOR = (float)mc.getWindow().getScaleFactor();
      GL20.glUniform2f(this.size.getId(), ((Vec2f)this.size.get()).x * SCALE_FACTOR, ((Vec2f)this.size.get()).y * SCALE_FACTOR);
      GL20.glUniform4f(this.color.getId(), (float)((Color)this.color.get()).getRed() / 255.0F, (float)((Color)this.color.get()).getGreen() / 255.0F, (float)((Color)this.color.get()).getBlue() / 255.0F, (float)((Color)this.color.get()).getAlpha() / 255.0F);
      GL20.glUniform1f(this.radius.getId(), (Float)this.radius.get());
      GL20.glUniform1f(this.softness.getId(), (Float)this.softness.get());
   }

   public void setDimensions(float width, float height) {
      this.size.set(new Vec2f(width, height));
   }

   public void setColor(Color rectColor) {
      this.color.set(rectColor);
   }

   public void setRadius(float rectRadius) {
      this.radius.set(rectRadius);
   }

   public void setSoftness(float edgeSoftness) {
      this.softness.set(edgeSoftness);
   }
}
