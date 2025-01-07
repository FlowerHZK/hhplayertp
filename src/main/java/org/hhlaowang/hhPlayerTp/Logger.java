package org.hhlaowang.hhPlayerTp;

import org.bukkit.Bukkit;

public class Logger {
    private static String ThisPrefix = "";
    public static void Init(String prefix) {
        ThisPrefix = prefix;
    }
    public static void info(String msg) {
        Bukkit.getLogger().info(ThisPrefix + " " + msg);
    }
    public static void error(String msg) {
        Bukkit.getLogger().severe(ThisPrefix + " " + msg);
    }
    public static void warning(String msg) {
        Bukkit.getLogger().warning(ThisPrefix + " " + msg);
    }
}
