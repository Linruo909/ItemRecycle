package com.linruo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;


import java.util.*;
import java.util.logging.Logger;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ItemRecycle extends JavaPlugin {
    static ItemRecycle main;
    public static Economy econ = null;
    public static Map<Material, Double> itemValues = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        //生成配置文件
        saveDefaultConfig();
        loadConfigValues();
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginCommand("itemrecycle").setExecutor(new irCommands());
        Bukkit.getPluginManager().registerEvents(new irListener(),this);

        Logger logger = this.getLogger();
        logger.info("ItemRecycle.version(1.0.1) loaded.");

        main=this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Logger logger = this.getLogger();
        logger.info("ItemRecycle unloaded.");

    }

    private void loadConfigValues() {
        itemValues = new HashMap<>();
        FileConfiguration config = getConfig();
        if (config.contains("item_values")) {
            for (String key : config.getConfigurationSection("item_values").getKeys(false)) {
                Material itemType = Material.matchMaterial(key);
                double value = config.getDouble("item_values." + key);
                itemValues.put(itemType, value);
            }
        }
    }
    public void saveConfigValues() {
        FileConfiguration config = getConfig();
        itemValues=sortMap(itemValues);
        config.set("item_values", null); // 清除之前的配置
        for (Map.Entry<Material, Double> entry : itemValues.entrySet()) {
            config.set("item_values." + entry.getKey().name(), entry.getValue());
        }
        saveConfig();
    }
    public static Map<Material, Double> getItemValues() {
        Map<Material, Double> ivs = itemValues;
        return  ivs;
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static ItemStack createItem(String itemName){

        String typed= ItemRecycle.main.getConfig().getString("menuItem." + itemName + ".type");
        int amountd= ItemRecycle.main.getConfig().getInt("menuItem." + itemName + ".amount");
        int exIDd= ItemRecycle.main.getConfig().getInt("menuItem." + itemName + ".exID");
        ItemStack item = new ItemStack(Material.getMaterial(typed),amountd,(short) exIDd);

        ItemMeta meta = item.getItemMeta();
        String dn= ItemRecycle.main.getConfig().getString("menuItem." + itemName + ".displayName");
        meta.setDisplayName(dn);

        item.setItemMeta(meta);
        return item;

    }
    public Map<Material, Double> sortMap(Map<Material, Double> map){
        Map<Material, Double> sortMap = new HashMap<>();
//        List<Map.Entry<Material, Double>> list = new ArrayList<>(map.entrySet());
//        //然后通过比较器来实现排序
//        Collections.sort(list,new Comparator<Map.Entry<Material, Double>>() {
//            //升序排序
//            public int compare(Map.Entry<Material, Double> o1, Map.Entry<Material, Double> o2) {
//                return o1.getValue().compareTo(o2.getValue());
//            }
//
//        });
//        for(Map.Entry<Material, Double> mapping:list){
//            sortMap.put(mapping.getKey(),mapping.getValue());
//        }
        ArrayList<Map.Entry<Material, Double>> entries1 = new ArrayList<>(map.entrySet());
//        entries1.sort(new Comparator<Map.Entry<Material, Double>>() {
//            @Override
//            public double compare(Map.Entry<Material, Double> o1, Map.Entry<Material, Double> o2) {
//                return o2.getValue() - o1.getValue();
//            }
//        });
        //lambda表达式写法
        entries1.sort((o1, o2) -> (int) (o2.getValue()-o1.getValue()));
        for (Map.Entry<Material, Double> entry : entries1) {
            sortMap.put(entry.getKey(), entry.getValue());
        }
        return sortMap;
    }
}
