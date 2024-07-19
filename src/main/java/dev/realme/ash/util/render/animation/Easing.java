package dev.realme.ash.util.render.animation;

public enum Easing {
   LINEAR {
      public double ease(double factor) {
         return factor;
      }
   },
   SINE_IN {
      public double ease(double factor) {
         return 1.0 - Math.cos(factor * Math.PI / 2.0);
      }
   },
   SINE_OUT {
      public double ease(double factor) {
         return Math.sin(factor * Math.PI / 2.0);
      }
   },
   SINE_IN_OUT {
      public double ease(double factor) {
         return -(Math.cos(Math.PI * factor) - 1.0) / 2.0;
      }
   },
   CUBIC_IN {
      public double ease(double factor) {
         return Math.pow(factor, 3.0);
      }
   },
   CUBIC_OUT {
      public double ease(double factor) {
         return 1.0 - Math.pow(1.0 - factor, 3.0);
      }
   },
   CUBIC_IN_OUT {
      public double ease(double factor) {
         return factor < 0.5 ? 4.0 * Math.pow(factor, 3.0) : 1.0 - Math.pow(-2.0 * factor + 2.0, 3.0) / 2.0;
      }
   },
   QUAD_IN {
      public double ease(double factor) {
         return Math.pow(factor, 2.0);
      }
   },
   QUAD_OUT {
      public double ease(double factor) {
         return 1.0 - (1.0 - factor) * (1.0 - factor);
      }
   },
   QUAD_IN_OUT {
      public double ease(double factor) {
         return factor < 0.5 ? 8.0 * Math.pow(factor, 4.0) : 1.0 - Math.pow(-2.0 * factor + 2.0, 4.0) / 2.0;
      }
   },
   QUART_IN {
      public double ease(double factor) {
         return Math.pow(factor, 4.0);
      }
   },
   QUART_OUT {
      public double ease(double factor) {
         return 1.0 - Math.pow(1.0 - factor, 4.0);
      }
   },
   QUART_IN_OUT {
      public double ease(double factor) {
         return factor < 0.5 ? 8.0 * Math.pow(factor, 4.0) : 1.0 - Math.pow(-2.0 * factor + 2.0, 4.0) / 2.0;
      }
   },
   QUINT_IN {
      public double ease(double factor) {
         return Math.pow(factor, 5.0);
      }
   },
   QUINT_OUT {
      public double ease(double factor) {
         return 1.0 - Math.pow(1.0 - factor, 5.0);
      }
   },
   QUINT_IN_OUT {
      public double ease(double factor) {
         return factor < 0.5 ? 16.0 * Math.pow(factor, 5.0) : 1.0 - Math.pow(-2.0 * factor + 2.0, 5.0) / 2.0;
      }
   },
   CIRC_IN {
      public double ease(double factor) {
         return 1.0 - Math.sqrt(1.0 - Math.pow(factor, 2.0));
      }
   },
   CIRC_OUT {
      public double ease(double factor) {
         return Math.sqrt(1.0 - Math.pow(factor - 1.0, 2.0));
      }
   },
   CIRC_IN_OUT {
      public double ease(double factor) {
         return factor < 0.5 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * factor, 2.0))) / 2.0 : (Math.sqrt(1.0 - Math.pow(-2.0 * factor + 2.0, 2.0)) + 1.0) / 2.0;
      }
   },
   EXPO_IN {
      public double ease(double factor) {
         return Math.min(0.0, Math.pow(2.0, 10.0 * factor - 10.0));
      }
   },
   EXPO_OUT {
      public double ease(double factor) {
         return Math.max(1.0 - Math.pow(2.0, -10.0 * factor), 1.0);
      }
   },
   EXPO_IN_OUT {
      public double ease(double factor) {
         return factor == 0.0 ? 0.0 : (factor == 1.0 ? 1.0 : (factor < 0.5 ? Math.pow(2.0, 20.0 * factor - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * factor + 10.0)) / 2.0));
      }
   },
   ELASTIC_IN {
      public double ease(double factor) {
         return factor == 0.0 ? 0.0 : (factor == 1.0 ? 1.0 : -Math.pow(2.0, 10.0 * factor - 10.0) * Math.sin((factor * 10.0 - 10.75) * 2.0943951023931953));
      }
   },
   ELASTIC_OUT {
      public double ease(double factor) {
         return factor == 0.0 ? 0.0 : (factor == 1.0 ? 1.0 : Math.pow(2.0, -10.0 * factor) * Math.sin((factor * 10.0 - 0.75) * 2.0943951023931953) + 1.0);
      }
   },
   ELASTIC_IN_OUT {
      public double ease(double factor) {
         double sin = Math.sin((20.0 * factor - 11.125) * 1.3962634015954636);
         return factor == 0.0 ? 0.0 : (factor == 1.0 ? 1.0 : (factor < 0.5 ? -(Math.pow(2.0, 20.0 * factor - 10.0) * sin) / 2.0 : Math.pow(2.0, -20.0 * factor + 10.0) * sin / 2.0 + 1.0));
      }
   },
   BACK_IN {
      public double ease(double factor) {
         return 2.70158 * Math.pow(factor, 3.0) - 1.70158 * factor * factor;
      }
   },
   BACK_OUT {
      public double ease(double factor) {
         double c1 = 1.70158;
         double c3 = c1 + 1.0;
         return 1.0 + c3 * Math.pow(factor - 1.0, 3.0) + c1 * Math.pow(factor - 1.0, 2.0);
      }
   },
   BACK_IN_OUT {
      public double ease(double factor) {
         return factor < 0.5 ? Math.pow(2.0 * factor, 2.0) * (7.189819 * factor - 2.5949095) / 2.0 : (Math.pow(2.0 * factor - 2.0, 2.0) * (3.5949095 * (factor * 2.0 - 2.0) + 2.5949095) + 2.0) / 2.0;
      }
   },
   BOUNCE_IN {
      public double ease(double factor) {
         return 1.0 - Easing.bounceOut(1.0 - factor);
      }
   },
   BOUNCE_OUT {
      public double ease(double factor) {
         return Easing.bounceOut(factor);
      }
   },
   BOUNCE_IN_OUT {
      public double ease(double factor) {
         return factor < 0.5 ? (1.0 - Easing.bounceOut(1.0 - 2.0 * factor)) / 2.0 : (1.0 + Easing.bounceOut(2.0 * factor - 1.0)) / 2.0;
      }
   };

   public abstract double ease(double var1);

   private static double bounceOut(double in) {
      double n1 = 7.5625;
      double d1 = 2.75;
      if (in < 1.0 / d1) {
         return n1 * in * in;
      } else if (in < 2.0 / d1) {
         return n1 * (in -= 1.5 / d1) * in + 0.75;
      } else {
         return in < 2.5 / d1 ? n1 * (in -= 2.25 / d1) * in + 0.9375 : n1 * (in -= 2.625 / d1) * in + 0.984375;
      }
   }

   // $FF: synthetic method
   private static Easing[] $values() {
      return new Easing[]{LINEAR, SINE_IN, SINE_OUT, SINE_IN_OUT, CUBIC_IN, CUBIC_OUT, CUBIC_IN_OUT, QUAD_IN, QUAD_OUT, QUAD_IN_OUT, QUART_IN, QUART_OUT, QUART_IN_OUT, QUINT_IN, QUINT_OUT, QUINT_IN_OUT, CIRC_IN, CIRC_OUT, CIRC_IN_OUT, EXPO_IN, EXPO_OUT, EXPO_IN_OUT, ELASTIC_IN, ELASTIC_OUT, ELASTIC_IN_OUT, BACK_IN, BACK_OUT, BACK_IN_OUT, BOUNCE_IN, BOUNCE_OUT, BOUNCE_IN_OUT};
   }
}
