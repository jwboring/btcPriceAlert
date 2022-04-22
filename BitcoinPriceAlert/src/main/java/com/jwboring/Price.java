package com.jwboring;

public class Price {

  enum Type {
    UP() {
      @Override
      public boolean reached(float current, float target) {
        return target < current;
      }

      @Override
      public String msg(float current, float target) {
        return "BTC has rised beyond " + target + " with price : " + current;
      }
    },
    DOWN {
      @Override
      public boolean reached(float current, float target) {
        return current < target;
      }

      @Override
      public String msg(float current, float target) {
        return "BTC has fallen below " + target + " with price : " + current;
      }
    };

    public abstract boolean reached(float current, float target);
    public abstract String msg(float current, float target);
  }

  public float target;
  public Type type;

  public Price(float current, float target) {
    this.target = target;
    type = Float.compare(current, target) < 0 ? Type.UP : Type.DOWN;
  }

}