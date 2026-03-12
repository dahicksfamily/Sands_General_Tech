package net.dahicksfamily.sgt.background;

public class BackgroundObject {
    public enum Type {
 
        STAR_O, STAR_B, STAR_A, STAR_F, STAR_G, STAR_K, STAR_M,
 
        CEPHEID_VARIABLE, WOLF_RAYET, NEUTRON_STAR,
 
        BLACK_HOLE, QUASAR,
 
        NEBULA, GALAXY
    }

    public float x, y, z; 
    public float r, g, b; 
    public float size; 
    public float brightness; 
    public Type  type;
    public long  seed; 

 
    public float variablePeriod    = 0f; 
    public float variableAmplitude = 0.03f;
    public float variablePhase     = 0f; 

 
    public float jetAngle    = 0f;
    public float pulsePeriod = 600f;

 
    public int   nebulaLayers = 6;
    public float nebulaSpread = 0.5f;
    public float[] nebulaLayerR, nebulaLayerG, nebulaLayerB;
    public float[] nebulaLayerSize, nebulaLayerRot;

 
    public float   galaxyAspect   = 0.35f;
    public float   galaxyRotAngle = 0f;
    public boolean galaxyHasBar       = false;
    public float   galaxyBarLength    = 0.25f;
    public float   galaxyBarWidth     = 0.06f;
    public int     galaxySpiralArms   = 2;
    public float   galaxyArmTightness = 0.8f;
    public float   galaxyArmOffset    = 0.0f;
    public float   galaxyBulgeSize    = 0.22f;
    public float   galaxyBulgeColor   = 0.0f;
    public float   galaxyHaloSize     = 0.85f;
    public float   galaxyHaloOpacity  = 0.12f;
    public boolean galaxyHasAGN       = false;
    public float   galaxyAGNStrength  = 0.0f;
    public float   galaxyDustLane     = 0.0f;
    public float   galaxyDiscOpacity  = 1.0f;

}