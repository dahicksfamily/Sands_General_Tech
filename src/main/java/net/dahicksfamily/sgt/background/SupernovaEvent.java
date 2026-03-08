package net.dahicksfamily.sgt.background;

public class SupernovaEvent {
    public float x, y, z; 
    public float r, g, b; 
    public long  startTick; 
    public long  durationTicks; 
    public float peakSize; 

     
    public float progress(long currentTick) {
        return Math.min(1f, (float)(currentTick - startTick) / durationTicks);
    }

     
    public float brightness(long currentTick) {
        float p = progress(currentTick);
        if (p < 0.05f) return p / 0.05f; 
        return (float)Math.pow(1f - (p - 0.05f) / 0.95f, 1.5f); 
    }

    public boolean isDone(long currentTick) { return progress(currentTick) >= 1f; }
}