package com.narrowtux.fmm.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

public class WindowsUtil {
    /**
     * Returns the path to the root Steam installation directory (typically C:\Program Files\Steam),
     * or null if Steam is not installed.
     *
     * Call only on Windows.
     */
    public static Path getSteamPathFromRegistry() {
        try {
            String pathStringFromRegistry = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Valve\\Steam", "SteamPath");
            return Paths.get(pathStringFromRegistry);
        } catch(Win32Exception e) {
            return null;
        }
    }
}
