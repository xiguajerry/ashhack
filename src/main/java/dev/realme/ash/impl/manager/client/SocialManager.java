package dev.realme.ash.impl.manager.client;

import dev.realme.ash.api.social.SocialRelation;
import dev.realme.ash.util.Globals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.text.Text;

public class SocialManager implements Globals {
   private final ConcurrentMap<String, SocialRelation> relationships = new ConcurrentHashMap<>();

   public boolean isRelation(String name, SocialRelation relation) {
      return this.relationships.get(name) == relation;
   }

   public boolean isFriend(String name) {
      return this.isRelation(name, SocialRelation.FRIEND);
   }

   public boolean isFriend(Text name) {
      return name != null && this.isRelation(name.getString(), SocialRelation.FRIEND);
   }

   public void addRelation(String name, SocialRelation relation) {
      if (mc.player == null || !name.equals(mc.player.getDisplayName().getString())) {
         SocialRelation relationship = this.relationships.get(name);
         if (relationship != null) {
            this.relationships.replace(name, relation);
         } else {
            this.relationships.put(name, relation);
         }
      }
   }

   public void addFriend(String name) {
      this.addRelation(name, SocialRelation.FRIEND);
   }

   public void addFriend(Text name) {
      this.addRelation(name.getString(), SocialRelation.FRIEND);
   }

   public SocialRelation remove(String playerName) {
      return this.relationships.remove(playerName);
   }

   public SocialRelation remove(Text playerName) {
      return this.relationships.remove(playerName.getString());
   }

   public Collection<String> getRelations(SocialRelation relation) {
      List<String> friends = new ArrayList<>();
       for (Map.Entry<String, SocialRelation> o : this.relationships.entrySet()) {
           if (o.getValue() == relation) {
               friends.add(o.getKey());
           }
       }

      return friends;
   }

   public Collection<String> getFriends() {
      return this.getRelations(SocialRelation.FRIEND);
   }
}
