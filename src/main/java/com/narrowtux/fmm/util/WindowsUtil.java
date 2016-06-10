package com.narrowtux.fmm.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class WindowsUtil {
    public static Path TryGetSteamPathFromRegistry() {
        String pathStringFromRegistry = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Valve\\Steam", "SteamPath");
        return Paths.get(pathStringFromRegistry);
    }
}
