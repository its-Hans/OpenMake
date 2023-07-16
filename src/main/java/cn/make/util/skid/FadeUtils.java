package cn.make.util.skid;

public class FadeUtils {
   protected long start;
   protected long length;

   public FadeUtils(long var1) {
      this.length = var1;
      this.reset();
   }

   public void reset() {
      this.start = System.currentTimeMillis();
   }

   private double getFadeOne() {
      double var10000;
      if (this.isEnd()) {
         var10000 = 1.0;
      } else {
         var10000 = (double)this.getTime() / (double)this.length;
      }
      return var10000;
   }

   public double easeOutQuad() {
      return this.length == 0L ? 1.0 : 1.0 - (1.0 - this.getFadeOne()) * (1.0 - this.getFadeOne());
   }

   public boolean isEnd() {
      return this.getTime() >= this.length;
   }

   public void setLength(long var1) {
      this.length = var1;
   }

   public double easeInQuad() {
      return this.getFadeOne() * this.getFadeOne();
   }

   protected long getTime() {
      return System.currentTimeMillis() - this.start;
   }
}
