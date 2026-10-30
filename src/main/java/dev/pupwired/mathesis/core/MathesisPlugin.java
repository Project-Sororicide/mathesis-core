package dev.pupwired.mathesis.core;

import dev.pupwired.mathesis.core.command.MathesisCommand;
import dev.pupwired.mathesis.core.damage.DamageListener;
import dev.pupwired.mathesis.core.damage.DamageSettings;
import dev.pupwired.mathesis.core.damage.DamageType;
import dev.pupwired.mathesis.core.damage.DamageTypeRegistry;
import dev.pupwired.mathesis.core.dev.ArmorSpikeListener;
import dev.pupwired.mathesis.core.stats.BuiltinStats;
import dev.pupwired.mathesis.core.stats.DamageTypeStats;
import dev.pupwired.mathesis.core.stats.StatDefinition;
import dev.pupwired.mathesis.core.stats.StatRegistry;
import dev.pupwired.mathesis.core.stats.bukkit.PlayerStatManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MathesisPlugin extends JavaPlugin {
    public static MathesisPlugin instance;

    // Registries
    private final StatRegistry statRegistry = new StatRegistry();
    private final DamageSettings damageSettings = new DamageSettings();
    private DamageTypeRegistry damageTypes;
    private PlayerStatManager playerStats;
    private ArmorSpikeListener armorSpike;

    public MathesisPlugin() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("damage_types.yml");

        damageTypes = new DamageTypeRegistry(getLogger());
        playerStats = new PlayerStatManager(this, statRegistry);
        armorSpike = new ArmorSpikeListener();
        getServer().getPluginManager().registerEvents(playerStats, this);
        getServer().getPluginManager().registerEvents(new DamageListener(damageSettings, playerStats), this);
        getServer().getPluginManager().registerEvents(armorSpike, this);

        var command = getCommand("mathesis");
        if (command != null) {
            var handler = new MathesisCommand(this);
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }

        reload();
        playerStats.attachOnline();
    }

    @Override
    public void onDisable() {
        if (playerStats != null) playerStats.detachAll();
    }

    public static MathesisPlugin getInstance() {
        return instance;
    }

    /** Re-reads every config-driven registry. Each registry swaps its contents only after a full load. */
    public void reload() {
        reloadConfig();
        damageTypes.load(YamlConfiguration.loadConfiguration(new File(getDataFolder(), "damage_types.yml")));
        getLogger().info("Loaded " + damageTypes.all().size() + " damage type(s).");
        damageSettings.reload(getConfig().getConfigurationSection("damage"), damageTypes, getLogger());
        rebuildStatRegistry();
        playerStats.refreshAll();
    }

    /** Built-in stats plus two stats per damage type. On failure the previous registry stays in place. */
    private void rebuildStatRegistry() {
        try {
            double resistCap = getConfig().getDouble("stats.resist-cap", 90);
            List<StatDefinition> definitions = new ArrayList<>(BuiltinStats.all());
            for (DamageType type : damageTypes.all()) {
                definitions.addAll(DamageTypeStats.definitionsFor(type.id(), resistCap));
            }
            statRegistry.replaceAll(definitions);
            getLogger().info("Registered " + definitions.size() + " stat(s).");
        } catch (IllegalArgumentException e) {
            getLogger().severe("Stat registry not updated: " + e.getMessage());
        }
    }

    private void saveResourceIfMissing(String name) {
        if (!new File(getDataFolder(), name).exists()) saveResource(name, false);
    }

    public DamageTypeRegistry damageTypes() { return damageTypes; }

    public DamageSettings damageSettings() { return damageSettings; }

    public StatRegistry statRegistry() { return statRegistry; }

    public PlayerStatManager playerStats() { return playerStats; }

    public ArmorSpikeListener armorSpike() { return armorSpike; }
}
