package net.dahicksfamily.sgt.time;

public class GlobalTime {

    private static GlobalTime instance;

    private double totalSeconds;
    private double timeScale = 1.0;
    private long lastRealTime;

    private static final double SECONDS_PER_DAY = 86400.0;
    private static final double DAYS_PER_YEAR = 365.25;
    private static final double EPOCH_OFFSET = 0.0;

    private GlobalTime() {
        this.totalSeconds = 0.0;
        this.lastRealTime = System.currentTimeMillis();
    }

    public static GlobalTime getInstance() {
        if (instance == null) {
            instance = new GlobalTime();
        }
        return instance;
    }

    public void tick() {
        long currentRealTime = System.currentTimeMillis();
        double deltaSeconds = (currentRealTime - lastRealTime) / 1000.0;
        this.totalSeconds += deltaSeconds * timeScale;
        this.lastRealTime = currentRealTime;
    }

    public void setTimeScale(double scale) {
        this.timeScale = Math.max(0.0, scale);
    }

    public double getTimeScale() {
        return this.timeScale;
    }

    public double getTotalSeconds() {
        return this.totalSeconds;
    }

    public double getTotalDays() {
        return this.totalSeconds / SECONDS_PER_DAY;
    }

    public double getTotalYears() {
        return getTotalDays() / DAYS_PER_YEAR;
    }

    public int getYear() {
        return (int) Math.floor(getTotalYears());
    }

    public int getDayOfYear() {
        double totalDays = getTotalDays();
        double yearStart = Math.floor(totalDays / DAYS_PER_YEAR) * DAYS_PER_YEAR;
        return (int) Math.floor(totalDays - yearStart);
    }

    public int getHour() {
        double dayProgress = (totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_DAY;
        return (int) Math.floor(dayProgress * 24.0);
    }

    public int getMinute() {
        double dayProgress = (totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_DAY;
        double hourProgress = (dayProgress * 24.0) % 1.0;
        return (int) Math.floor(hourProgress * 60.0);
    }

    public int getSecond() {
        double dayProgress = (totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_DAY;
        double minuteProgress = (dayProgress * 24.0 * 60.0) % 1.0;
        return (int) Math.floor(minuteProgress * 60.0);
    }

    public double getDayProgress() {
        return (totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_DAY;
    }

    public float getTimeOfDay() {
        return (float) getDayProgress();
    }

    public float getSunAngle() {
        double dayProgress = getDayProgress();

        if (dayProgress < 0.25) {
            return (float) (dayProgress / 0.25 * 0.25);
        } else if (dayProgress < 0.5) {
            return (float) (0.25 + (dayProgress - 0.25) / 0.25 * 0.25);
        } else if (dayProgress < 0.75) {
            return (float) (0.5 + (dayProgress - 0.5) / 0.25 * 0.25);
        } else {
            return (float) (0.75 + (dayProgress - 0.75) / 0.25 * 0.25);
        }
    }

    public long getMinecraftTicks() {
        return (long) (getTotalDays() * 24000L);
    }

    public void setFromMinecraftTicks(long ticks) {
        double days = ticks / 24000.0;
        this.totalSeconds = days * SECONDS_PER_DAY;
    }

    public void setTotalDays(double days) {
        this.totalSeconds = days * SECONDS_PER_DAY;
    }

    public String getFormattedTime() {
        return String.format("Year %d, Day %d - %02d:%02d:%02d",
                getYear(), getDayOfYear(), getHour(), getMinute(), getSecond());
    }

    public String getFormattedDate() {
        return String.format("Year %d, Day %d", getYear(), getDayOfYear());
    }

    public String getFormattedClock() {
        return String.format("%02d:%02d:%02d", getHour(), getMinute(), getSecond());
    }
}