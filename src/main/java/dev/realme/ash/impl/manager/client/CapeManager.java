package dev.realme.ash.impl.manager.client;

import com.mojang.authlib.GameProfile;
import dev.realme.ash.util.Globals;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class CapeManager implements Globals {
   private static final Map CAPE_TEXTURE_CACHE = new HashMap();

   public void loadPlayerCape(GameProfile profile, CapeTexture texture) {
      if (CAPE_TEXTURE_CACHE.containsKey(profile.getId())) {
         texture.callback((Identifier)CAPE_TEXTURE_CACHE.get(profile.getId()));
      } else {
         Util.getMainWorkerExecutor().execute(() -> {
            String uuid = profile.getId().toString();
            String url = String.format("http://s.optifine.net/capes/%s.png", profile.getName());

            try {
               URL optifineUrl = new URL(url);
               InputStream stream = optifineUrl.openStream();
               NativeImage cape = NativeImage.read(stream);
               NativeImage nativeImage = this.imageFromStream(cape);
               NativeImageBackedTexture t = new NativeImageBackedTexture(nativeImage);
               Identifier identifier = mc.getTextureManager().registerDynamicTexture("of-capes-" + uuid, t);
               texture.callback(identifier);
               stream.close();
               CAPE_TEXTURE_CACHE.put(profile.getId(), identifier);
            } catch (Exception var11) {
            }

         });
      }
   }

   private NativeImage imageFromStream(NativeImage image) {
      int imageWidth = 64;
      int imageHeight = 32;
      int imageSrcWidth = image.getWidth();
      int srcHeight = image.getHeight();

      for(int imageSrcHeight = image.getHeight(); imageWidth < imageSrcWidth || imageHeight < imageSrcHeight; imageHeight *= 2) {
         imageWidth *= 2;
      }

      NativeImage img = new NativeImage(imageWidth, imageHeight, true);

      for(int x = 0; x < imageSrcWidth; ++x) {
         for(int y = 0; y < srcHeight; ++y) {
            img.setColor(x, y, image.getColor(x, y));
         }
      }

      image.close();
      return img;
   }

   public interface CapeTexture {
      void callback(Identifier var1);
   }
}
