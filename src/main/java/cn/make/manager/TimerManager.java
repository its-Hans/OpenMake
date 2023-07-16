package cn.make.manager;


import chad.phobos.api.center.Feature;

public class TimerManager extends Feature {

    public float timer = 1;

    public void set(float factor) {

        if (factor < 0.1f) factor = 0.1f;

        timer = factor;
    }

    public void reset() {
        timer = 1;
    }

    public float get() {
        return timer;
    }
}

