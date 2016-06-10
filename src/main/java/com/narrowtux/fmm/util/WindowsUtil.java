package com.narrowtux.fmm.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

public class WindowsUtil {
    private static final Logger LOGGER = Logger.getLogger(WindowsUtil.class.getName());

    public static Path TryGetSteamPathFromRegistry() {
        try {
            String pathStringFromRegistry = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Valve\\Steam", "SteamPath");
            LOGGER.info("Found SteamPath from registry: " + pathStringFromRegistry);
            return Paths.get(pathStringFromRegistry);
        } catch(Win32Exception e) {
            LOGGER.warning("Could not find SteamPath in registry, Steam is probably not installed");
            return null;
        }
    }
}
