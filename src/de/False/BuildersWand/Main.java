package de.False.BuildersWand;

import de.False.BuildersWand.ConfigurationFiles.Config;
import de.False.BuildersWand.ConfigurationFiles.Locales;
import de.False.BuildersWand.Inventories.CraftingMenu;
import de.False.BuildersWand.Inventories.EditorMenu;
import de.False.BuildersWand.Inventories.MainMenu;
import de.False.BuildersWand.NMS.NMS;
import de.False.BuildersWand.NMS.v_1_10.v_1_10_R1;
import de.False.BuildersWand.NMS.v_1_11.v_1_11_R1;
import de.False.BuildersWand.NMS.v_1_12.v_1_12_R1;
import de.False.BuildersWand.NMS.v_1_8.v_1_8_R1;
import de.False.BuildersWand.NMS.v_1_8.v_1_8_R2;
import de.False.BuildersWand.NMS.v_1_8.v_1_8_R3;
import de.False.BuildersWand.NMS.v_1_9.v_1_9_R1;
import de.False.BuildersWand.NMS.v_1_9.v_1_9_R2;
import de.False.BuildersWand.Updater.UpdateNotification;
import de.False.BuildersWand.Updater.SpigotUpdater;
import de.False.BuildersWand.events.WandEvents;
import de.False.BuildersWand.events.WandStorageEvents;
import de.False.BuildersWand.manager.InventoryManager;
import de.False.BuildersWand.manager.WandManager;
import de.False.BuildersWand.utilities.InventoryBuilder;
import de.False.BuildersWand.utilities.Metrics;
import de.False.BuildersWand.utilities.ParticleUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Main extends JavaPlugin
{
    private Locales locales = new Locales(this);
    private Config config;
    private ParticleUtil particleUtil;
    private NMS nms;
    private WandManager wandManager;
    private InventoryManager inventoryManager;
    private InventoryBuilder inventoryBuilder;

    @Override
    public void onEnable()
    {
        setupNMS();
        wandManager = new WandManager(this, nms);
        inventoryManager = new InventoryManager(this, nms);
        inventoryBuilder = new InventoryBuilder(wandManager);

        loadConfigFiles();
        particleUtil = new ParticleUtil(nms, config);
        registerEvents();
        registerCommands();
        loadMetrics();
        checkForUpdate(this);
    }

    private void loadMetrics()
    {
        Metrics metrics = new Metrics(this);

        metrics.addCustomChart(new Metrics.MultiLineChart("players_and_servers", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() {
                Map<String, Integer> valueMap = new HashMap<>();
                valueMap.put("servers", 1);
                valueMap.put("players", Bukkit.getOnlinePlayers().size());
                return valueMap;
            }
        }));
    }

    private void registerCommands()
    {
        getCommand("builderswand").setExecutor(new Commands(config, wandManager, nms, inventoryBuilder));
    }

    private void registerEvents()
    {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new WandEvents(this, config, particleUtil, nms, wandManager, inventoryManager), this);
        pluginManager.registerEvents(new WandStorageEvents(this, config, nms, wandManager, inventoryManager), this);
        pluginManager.registerEvents(new UpdateNotification(this, config), this);
        pluginManager.registerEvents(new MainMenu(inventoryBuilder), this);
        pluginManager.registerEvents(new EditorMenu(inventoryBuilder), this);
        pluginManager.registerEvents(new CraftingMenu(inventoryBuilder), this);
    }

    private void loadConfigFiles()
    {
        config = new Config(this);
        locales.load();
        config.load();
        wandManager.load();
        inventoryManager.load();
    }

    private void setupNMS() {
        String version;
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            switch (version)
            {
                case "v1_8_R1":
                    nms = new v_1_8_R1();
                    break;
                case "v1_8_R2":
                    nms = new v_1_8_R2();
                    break;
                case "v1_8_R3":
                    nms = new v_1_8_R3();
                    break;
                case "v1_9_R1":
                    nms = new v_1_9_R1();
                    break;
                case "v1_9_R2":
                    nms = new v_1_9_R2();
                    break;
                case "v1_10_R1":
                    nms = new v_1_10_R1();
                    break;
                case "v1_11_R1":
                    nms = new v_1_11_R1();
                    break;
                case "v1_12_R1":
                    nms = new v_1_12_R1(this);
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException exn) {
            exn.printStackTrace();
        }
    }

    private void checkForUpdate(Main plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    new SpigotUpdater(plugin, 51577, config.getAutoDownload(), config);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(this, 20L, 72000L);
    }
}
