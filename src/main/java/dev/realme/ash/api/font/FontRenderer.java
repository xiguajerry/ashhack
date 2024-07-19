package dev.realme.ash.api.font;

import dev.realme.ash.util.Globals;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

public interface FontRenderer extends Globals {
   GlyphVisitor DEFAULT_VISITOR = (renderer, c, x, y, color, shadow) -> {
      int endPoint = renderer.drawGlyph(renderer, c, x, y, color, shadow);
      int endPoint2 = endPoint;
      if (shadow) {
         endPoint2 = renderer.drawGlyph(renderer, c, x + 1.0, y + 1.0, color, false);
      }

      return Math.max(endPoint, endPoint2);
   };

   void draw(Matrix4f var1, String var2, double var3, double var5, int var7, boolean var8);

   void draw(Matrix4f var1, GlyphVisitor var2, String var3, double var4, double var6, int var8, boolean var9);

   void draw(Matrix4f var1, GlyphVisitor var2, VertexConsumerProvider var3, String var4, double var5, double var7, int var9, boolean var10);

   int drawGlyph(FontRenderer var1, char var2, double var3, double var5, int var7, boolean var8);

   int getStringWidth(String var1);

   Glyph getGlyph(char var1);

   Glyph[] getGlyphMap();
}
