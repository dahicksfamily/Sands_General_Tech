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

 
    public float galaxyAspect   = 0.35f; 
    public float galaxyRotAngle = 0f; 
}