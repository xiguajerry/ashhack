package dev.realme.ash.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MathUtil {
   private static final int EXP_INT_TABLE_MAX_INDEX = 750;
   private static final int EXP_INT_TABLE_LEN = 1500;
   private static final int EXP_FRAC_TABLE_LEN = 1025;
   private static final double[] FACT = new double[]{1.0, 1.0, 2.0, 6.0, 24.0, 120.0, 720.0, 5040.0, 40320.0, 362880.0, 3628800.0, 3.99168E7, 4.790016E8, 6.2270208E9, 8.71782912E10, 1.307674368E12, 2.0922789888E13, 3.55687428096E14, 6.402373705728E15, 1.21645100408832E17};

   public static float[] calcAngle(Vec3d from, Vec3d to) {
      double difX = to.x - from.x;
      double difY = (to.y - from.y) * -1.0;
      double difZ = to.z - from.z;
      double dist = MathHelper.sqrt((float)(difX * difX + difZ * difZ));
      return new float[]{(float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))};
   }

   public static float clamp(float num, float min, float max) {
      return num < min ? min : Math.min(num, max);
   }

   public static int clamp(int num, int min, int max) {
      return num < min ? min : Math.min(num, max);
   }

   public static double clamp(double value, double min, double max) {
      return value < min ? min : Math.min(value, max);
   }

   public static double square(double input) {
      return input * input;
   }

   public static float random(float min, float max) {
      return (float)(Math.random() * (double)(max - min) + (double)min);
   }

   public static double random(double min, double max) {
      return (float)(Math.random() * (max - min) + min);
   }

   public static float rad(float angle) {
      return (float)((double)angle * Math.PI / 180.0);
   }

   public static double round(double value, int places) {
      BigDecimal bd = new BigDecimal(value);
      bd = bd.setScale(places, RoundingMode.HALF_UP);
      return bd.doubleValue();
   }

   private static double expint(int p, double[] result) {
      double[] xs = new double[2];
      double[] as = new double[2];
      double[] ys = new double[2];
      xs[0] = Math.E;
      xs[1] = 1.4456468917292502E-16;
      split(1.0, ys);

      while(p > 0) {
         if ((p & 1) != 0) {
            quadMult(ys, xs, as);
            ys[0] = as[0];
            ys[1] = as[1];
         }

         quadMult(xs, xs, as);
         xs[0] = as[0];
         xs[1] = as[1];
         p >>= 1;
      }

      if (result != null) {
         result[0] = ys[0];
         result[1] = ys[1];
         resplit(result);
      }

      return ys[0] + ys[1];
   }

   public static double slowexp(double x, double[] result) {
      double[] xs = new double[2];
      double[] ys = new double[2];
      double[] facts = new double[2];
      double[] as = new double[2];
      split(x, xs);
      ys[0] = ys[1] = 0.0;

      for(int i = FACT.length - 1; i >= 0; --i) {
         splitMult(xs, ys, as);
         ys[0] = as[0];
         ys[1] = as[1];
         split(FACT[i], as);
         splitReciprocal(as, facts);
         splitAdd(ys, facts, as);
         ys[0] = as[0];
         ys[1] = as[1];
      }

      if (result != null) {
         result[0] = ys[0];
         result[1] = ys[1];
      }

      return ys[0] + ys[1];
   }

   private static void quadMult(double[] a, double[] b, double[] result) {
      double[] xs = new double[2];
      double[] ys = new double[2];
      double[] zs = new double[2];
      split(a[0], xs);
      split(b[0], ys);
      splitMult(xs, ys, zs);
      result[0] = zs[0];
      result[1] = zs[1];
      split(b[1], ys);
      splitMult(xs, ys, zs);
      double tmp = result[0] + zs[0];
      result[1] -= tmp - result[0] - zs[0];
      result[0] = tmp;
      tmp = result[0] + zs[1];
      result[1] -= tmp - result[0] - zs[1];
      result[0] = tmp;
      split(a[1], xs);
      split(b[0], ys);
      splitMult(xs, ys, zs);
      tmp = result[0] + zs[0];
      result[1] -= tmp - result[0] - zs[0];
      result[0] = tmp;
      tmp = result[0] + zs[1];
      result[1] -= tmp - result[0] - zs[1];
      result[0] = tmp;
      split(a[1], xs);
      split(b[1], ys);
      splitMult(xs, ys, zs);
      tmp = result[0] + zs[0];
      result[1] -= tmp - result[0] - zs[0];
      result[0] = tmp;
      tmp = result[0] + zs[1];
      result[1] -= tmp - result[0] - zs[1];
      result[0] = tmp;
   }

   private static void splitMult(double[] a, double[] b, double[] ans) {
      ans[0] = a[0] * b[0];
      ans[1] = a[0] * b[1] + a[1] * b[0] + a[1] * b[1];
      resplit(ans);
   }

   private static void split(double d, double[] split) {
      double a;
      if (d < 8.0E298 && d > -8.0E298) {
         a = d * 1.073741824E9;
         split[0] = d + a - a;
         split[1] = d - split[0];
      } else {
         a = d * 9.313225746154785E-10;
         split[0] = (d + a - d) * 1.073741824E9;
         split[1] = d - split[0];
      }

   }

   private static void resplit(double[] a) {
      double c = a[0] + a[1];
      double d = -(c - a[0] - a[1]);
      double z;
      if (c < 8.0E298 && c > -8.0E298) {
         z = c * 1.073741824E9;
         a[0] = c + z - z;
         a[1] = c - a[0] + d;
      } else {
         z = c * 9.313225746154785E-10;
         a[0] = (c + z - c) * 1.073741824E9;
         a[1] = c - a[0] + d;
      }

   }

   private static void splitAdd(double[] a, double[] b, double[] ans) {
      ans[0] = a[0] + b[0];
      ans[1] = a[1] + b[1];
      resplit(ans);
   }

   private static void splitReciprocal(double[] in, double[] result) {
      double b = 2.384185791015625E-7;
      double a = 0.9999997615814209;
      if (in[0] == 0.0) {
         in[0] = in[1];
         in[1] = 0.0;
      }

      result[0] = 0.9999997615814209 / in[0];
      result[1] = (2.384185791015625E-7 * in[0] - 0.9999997615814209 * in[1]) / (in[0] * in[0] + in[0] * in[1]);
      if (result[1] != result[1]) {
         result[1] = 0.0;
      }

      resplit(result);

      for(int i = 0; i < 2; ++i) {
         double err = 1.0 - result[0] * in[0] - result[0] * in[1] - result[1] * in[0] - result[1] * in[1];
         err *= result[0] + result[1];
         result[1] += err;
      }

   }

   private static double exp(double x) {
      int intVal = (int)x;
      if (x < 0.0) {
         if (x < -746.0) {
            return 0.0;
         }

         if (intVal < -709) {
            return exp(x + 40.19140625) / 2.85040095144011776E17;
         }

         if (intVal == -709) {
            return exp(x + 1.494140625) / 4.455505956692757;
         }

         --intVal;
      } else if (intVal > 709) {
         return Double.POSITIVE_INFINITY;
      }

      double intPartA = MathUtil.ExpIntTable.EXP_INT_TABLE_A[750 + intVal];
      double intPartB = MathUtil.ExpIntTable.EXP_INT_TABLE_B[750 + intVal];
      int intFrac = (int)((x - (double)intVal) * 1024.0);
      double fracPartA = MathUtil.ExpFracTable.EXP_FRAC_TABLE_A[intFrac];
      double fracPartB = MathUtil.ExpFracTable.EXP_FRAC_TABLE_B[intFrac];
      double epsilon = x - ((double)intVal + (double)intFrac / 1024.0);
      double z = 0.04168701738764507;
      z = z * epsilon + 0.1666666505023083;
      z = z * epsilon + 0.5000000000042687;
      z = z * epsilon + 1.0;
      z = z * epsilon + -3.940510424527919E-20;
      double tempA = intPartA * fracPartA;
      double tempB = intPartA * fracPartB + intPartB * fracPartA + intPartB * fracPartB;
      double tempC = tempB + tempA;
      return tempC == Double.POSITIVE_INFINITY ? Double.POSITIVE_INFINITY : tempC * z + tempB + tempA;
   }

   private static class ExpIntTable {
      private static final double[] EXP_INT_TABLE_A = new double[1500];
      private static final double[] EXP_INT_TABLE_B = new double[1500];

      static {
         double[] tmp = new double[2];
         double[] recip = new double[2];

         for(int i = 0; i < 750; ++i) {
            MathUtil.expint(i, tmp);
            EXP_INT_TABLE_A[i + 750] = tmp[0];
            EXP_INT_TABLE_B[i + 750] = tmp[1];
            if (i != 0) {
               MathUtil.splitReciprocal(tmp, recip);
               EXP_INT_TABLE_A[750 - i] = recip[0];
               EXP_INT_TABLE_B[750 - i] = recip[1];
            }
         }

      }
   }

   private static class ExpFracTable {
      private static final double[] EXP_FRAC_TABLE_A = new double[1025];
      private static final double[] EXP_FRAC_TABLE_B = new double[1025];

      static {
         double[] tmp = new double[2];
         double factor = 9.765625E-4;

         for(int i = 0; i < EXP_FRAC_TABLE_A.length; ++i) {
            MathUtil.slowexp((double)i * 9.765625E-4, tmp);
            EXP_FRAC_TABLE_A[i] = tmp[0];
            EXP_FRAC_TABLE_B[i] = tmp[1];
         }

      }
   }
}
