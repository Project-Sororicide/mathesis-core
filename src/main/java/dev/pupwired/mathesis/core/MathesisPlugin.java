package dev.pupwired.mathesis.core;

import org.bukkit.plugin.java.JavaPlugin;

public class MathesisPlugin extends JavaPlugin {
    public static MathesisPlugin instance;

    public MathesisPlugin() {
        instance = this;
    }

    @Override
    public void onLoad() {}

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {}

    public static MathesisPlugin getInstance() {
        return instance;
    }
}
