package dev.realme.ash.util;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

public class StreamUtils {
   public static <T, C extends Comparable<C>> Stream sortCached(Stream<T> stream, Function<T, C> keyExtractor) {
      return stream.map((t) -> {
         C key = keyExtractor.apply(t);
         return new Intermediary<>(t, key);
      }).sorted(Comparator.comparing(Intermediary::key)).map(Intermediary::value);
   }

   private record Intermediary<T, C extends Comparable<C>>(T value, C key) {
      private Intermediary(T value, C key) {
         this.value = value;
         this.key = key;
      }

      public T value() {
         return this.value;
      }

      public C key() {
         return this.key;
      }
   }
}
