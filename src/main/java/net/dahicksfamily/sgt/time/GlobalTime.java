package net.dahicksfamily.sgt.time;

import net.dahicksfamily.sgt.space.CelestialBody;
import net.dahicksfamily.sgt.space.SolarSystem;
import net.dahicksfamily.sgt.space.Star;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class GlobalTime {

    private static GlobalTime instance;

    private double totalSeconds;
    private double timeScale = 1.0;
    private long lastRealTime;

    private CelestialBody currentObserver;

    private static final double SECONDS_PER_DAY = 86400.0;

    private GlobalTime() {
        this.totalSeconds = 0.0;
        this.lastRealTime = System.currentTimeMillis();
        this.currentObserver = null;
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

    public void setCurrentObserver(CelestialBody observer) {
        this.currentObserver = observer;
    }

    public CelestialBody getCurrentObserver() {
        return this.currentObserver;
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
        if (currentObserver == null) {
            return getTotalDays() / 365.25;
        }
        return getTotalDays() / currentObserver.period;
    }

    public int getYear() {
        return (int) Math.floor(getTotalYears());
    }

    public int getDayOfYear() {
        if (currentObserver == null) {
            double totalDays = getTotalDays();
            double yearStart = Math.floor(totalDays / 365.25) * 365.25;
            return (int) Math.floor(totalDays - yearStart);
        }

        double totalDays = getTotalDays();
        double yearStart = Math.floor(totalDays / currentObserver.period) * currentObserver.period;
        return (int) Math.floor(totalDays - yearStart);
    }

    public int getHour() {
        if (currentObserver == null) {
            double dayProgress = (totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_DAY;
            return (int) Math.floor(dayProgress * 24.0);
        }

        double rotationPeriodSeconds = currentObserver.rotationPeriod * 3600.0;
        double dayProgress = (totalSeconds % rotationPeriodSeconds) / rotationPeriodSeconds;
        return (int) Math.floor(dayProgress * 24.0);
    }

    public int getMinute() {
        if (currentObserver == null) {
            double dayProgress = (totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_DAY;
            double hourProgress = (dayProgress * 24.0) % 1.0;
            return (int) Math.floor(hourProgress * 60.0);
        }

        double rotationPeriodSeconds = currentObserver.rotationPeriod * 3600.0;
        double dayProgress = (totalSeconds % rotationPeriodSeconds) / rotationPeriodSeconds;
        double hourProgress = (dayProgress * 24.0) % 1.0;
        return (int) Math.floor(hourProgress * 60.0);
    }

    public int getSecond() {
        if (currentObserver == null) {
            double dayProgress = (totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_DAY;
            double minuteProgress = (dayProgress * 24.0 * 60.0) % 1.0;
            return (int) Math.floor(minuteProgress * 60.0);
        }

        double rotationPeriodSeconds = currentObserver.rotationPeriod * 3600.0;
        double dayProgress = (totalSeconds % rotationPeriodSeconds) / rotationPeriodSeconds;
        double minuteProgress = (dayProgress * 24.0 * 60.0) % 1.0;
        return (int) Math.floor(minuteProgress * 60.0);
    }

    public double getDayProgress() {
        if (currentObserver == null) {
            return (totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_DAY;
        }

        double rotationPeriodSeconds = currentObserver.rotationPeriod * 3600.0;
        return (totalSeconds % rotationPeriodSeconds) / rotationPeriodSeconds;
    }

    public float getTimeOfDay() {
        return (float) getDayProgress();
    }

    public float getSunAngle() {
        if (currentObserver == null) return 0;

        SolarSystem solarSystem = SolarSystem.getInstance();
        var stars = solarSystem.getStars();
        if (stars.isEmpty()) return 0;

        Star sun = stars.get(0);

        Vec3 toSun = solarSystem.getAbsolutePosition(sun)
                .subtract(solarSystem.getAbsolutePosition(currentObserver))
                .normalize();

        double tilt = currentObserver.axialTilt;
        double sx = toSun.x;
        double sy = toSun.y * Math.cos(tilt) - toSun.z * Math.sin(tilt);

        double timeInHours = getTotalDays() * 24.0;
        double skyRot = (timeInHours % currentObserver.rotationPeriod)
                / currentObserver.rotationPeriod * 2.0 * Math.PI;

        double elevation = sx * Math.cos(skyRot) + sy * Math.sin(skyRot);  // +1=noon, -1=midnight
        double east      = sx * -Math.sin(skyRot) + sy * Math.cos(skyRot); // +1=rising, -1=setting

        double angle = Math.atan2(elevation, -east) / (2.0 * Math.PI);
        if (angle < 0) angle += 1.0;

        return (float) angle;
    }


    public long getMinecraftTicks() {
        if (currentObserver == null) {
            return (long) (getTotalDays() * 24000L);
        }

        double timeInHours = getTotalDays() * 24.0;
        double dayProgress = (timeInHours % currentObserver.rotationPeriod) / currentObserver.rotationPeriod;
        return (long) (dayProgress * 24000L);
    }

    public void setFromMinecraftTicks(long ticks) {
        double days = ticks / 24000.0;
        this.totalSeconds = days * SECONDS_PER_DAY;
    }

    public void setTotalDays(double days) {
        this.totalSeconds = days * SECONDS_PER_DAY;
    }

    public double getYearProgress() {
        if (currentObserver == null) {
            double totalDays = getTotalDays();
            double daysIntoYear = totalDays % 365.25;
            if (daysIntoYear < 0) daysIntoYear += 365.25;
            return daysIntoYear / 365.25;
        }

        double totalDays = getTotalDays();
        double daysIntoYear = totalDays % currentObserver.period;
        if (daysIntoYear < 0) daysIntoYear += currentObserver.period;
        return daysIntoYear / currentObserver.period;
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