package net.sabafly.slotmachine.v2.game.legacy;

import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Wheel {
    final WheelPattern[] wheelPatterns;
    boolean isRunning = false;
    @Getter
    int count;
    @Getter
    final Pos pos;

    public Wheel(WheelPattern[] wheelPatterns, Pos pos) {
        this.wheelPatterns = wheelPatterns;
        this.pos = pos;
        this.count = ThreadLocalRandom.current().nextInt(wheelPatterns.length);
    }

    public BufferedImage getImage(final int range) {
        return getPattern(range).getImage();
    }

    public WheelPattern[] getPatterns() {
        ArrayList<WheelPattern> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            list.add(getPattern(i));
        }
        return list.toArray(new WheelPattern[]{});
    }

    public WheelPattern[] getPatternSnapshot(int range) {
        ArrayList<WheelPattern> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(getPattern(i + range));
        }
        return list.toArray(new WheelPattern[]{});
    }

    public WheelPattern getPattern(int i) {
        return wheelPatterns[(count + i) % getLength()];
    }

    public int getLength() {
        return wheelPatterns.length;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isStopped() {
        return !isRunning();
    }

    public void step() {
        if (isRunning()) count++;
    }

    public void stop(int i) {
        isRunning = false;
        count += i;
    }

    public void start() {
        isRunning = true;
    }
}
