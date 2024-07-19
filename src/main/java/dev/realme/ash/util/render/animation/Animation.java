package dev.realme.ash.util.render.animation;

public class Animation {
   private final Easing easing;
   private float length;
   private long last;
   private boolean state;

   public Animation(float length) {
      this(false, length);
   }

   public Animation(boolean initial, float length) {
      this(initial, length, Easing.LINEAR);
   }

   public Animation(boolean initial, float length, Easing easing) {
      this.last = 0L;
      this.length = length;
      this.state = initial;
      this.easing = easing;
   }

   public void setState(boolean state) {
      this.last = (long)(!state ? (double)System.currentTimeMillis() - (1.0 - this.getFactor()) * (double)this.length : (double)System.currentTimeMillis() - this.getFactor() * (double)this.length);
      this.state = state;
   }

   public boolean getState() {
      return this.state;
   }

   public double getFactor() {
      return this.easing.ease(this.getLinearFactor());
   }

   public double getLinearFactor() {
      return this.state ? this.clamp((float)(System.currentTimeMillis() - this.last) / this.length) : this.clamp(1.0F - (float)(System.currentTimeMillis() - this.last) / this.length);
   }

   public double getCurrent() {
      return 1.0 + this.getFactor();
   }

   private double clamp(double in) {
      return in < 0.0 ? 0.0 : Math.min(in, 1.0);
   }

   public double getLength() {
      return this.length;
   }

   public void setLength(float length) {
      this.length = length;
   }

   public boolean isFinished() {
      return !this.getState() && this.getFactor() == 0.0 || this.getState() && this.getFactor() == 1.0;
   }
}
