package dev.realme.ash.impl.shaders;

import dev.realme.ash.api.render.shader.Program;
import dev.realme.ash.api.render.shader.Shader;
import dev.realme.ash.api.render.shader.Uniform;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.opengl.GL20;

public class GradientProgram extends Program {
   final Uniform resolution = new Uniform("resolution");

   public GradientProgram() {
      super(new Shader("gradient.frag", 35632));
   }

   public void initUniforms() {
      this.resolution.init(this.id);
   }

   public void updateUniforms() {
      GL20.glUniform2f(this.resolution.getId(), ((Vec2f)this.resolution.get()).x, ((Vec2f)this.resolution.get()).y);
   }

   public void setUniforms(Vec2f resolution) {
      this.resolution.set(resolution);
   }
}
