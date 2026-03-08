package net.dahicksfamily.sgt.background;

import java.util.ArrayList;
import java.util.List;

public class StarFieldData {
    public final List<BackgroundObject> simpleStars   = new ArrayList<>(); 
    public final List<BackgroundObject> exoticObjects = new ArrayList<>(); 
    public final List<BackgroundObject> nebulae       = new ArrayList<>();
    public final List<BackgroundObject> galaxies      = new ArrayList<>();
    public long seed;
}