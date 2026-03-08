package net.dahicksfamily.sgt.SkyRendering;

import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

 
public class ObjMeshCache {

     
    public static final Set<String> IRREGULAR_BODIES = Set.of(
            "Phobos",
            "Deimos",
            "Amalthea", "Thebe",
            "Proteus",
            "Haumea",
            "Hyperion"
 
    );

    private static final Map<String, ObjMesh> cache = new HashMap<>();
     
    private static final ObjMesh MISSING = new ObjMesh();

    public static boolean isIrregular(String bodyName) {
        return IRREGULAR_BODIES.contains(bodyName);
    }

     
    public static ObjMesh get(String bodyName, ResourceLocation fallbackTexture) {
        if (!IRREGULAR_BODIES.contains(bodyName)) return null;

        ObjMesh cached = cache.get(bodyName);
        if (cached == MISSING) return null;
        if (cached != null) return cached;

        ObjMesh loaded = ObjMesh.load(bodyName, fallbackTexture);
        cache.put(bodyName, loaded != null ? loaded : MISSING);
        return loaded;
    }

    public static void clearAll() {
        cache.forEach((k, v) -> { if (v != MISSING) v.close(); });
        cache.clear();
    }
}