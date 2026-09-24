package com.animon.fix;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AmbientResourcePolicyTest {
    @Test void neoforgeDefaultResourcesPermitDefaultVoiceFallback() {
        assertTrue(AmbientResourcePolicy.isBuiltIn("mod_resources"));
        assertTrue(AmbientResourcePolicy.isBuiltIn("mod/cobblemon"));
    }
    @Test void resourcePacksKeepTheirOwnAmbientVoice() {
        assertFalse(AmbientResourcePolicy.isBuiltIn("file/Animon Ambient.zip"));
        assertFalse(AmbientResourcePolicy.isBuiltIn("file/Cobblemon Animon Ambient.zip"));
        assertFalse(AmbientResourcePolicy.isBuiltIn("server/00000000"));
        assertFalse(AmbientResourcePolicy.isBuiltIn("mod/cobblethemes"));
    }
}
