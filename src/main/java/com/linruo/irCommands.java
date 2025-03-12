package com.linruo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;


import java.util.Collections;
import java.util.Map;

import static com.linruo.ItemRecycle.*;

public class irCommands implements CommandExecutor {

    static Inventory[] inventories;
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        // ir help
        if(strings.length == 0 || (strings.length == 1 && strings[0].equals("help"))) {

            String help = ItemRecycle.main.getConfig().getString("Message.help");
            commandSender.sendMessage(help);
            return  true;
        }

        // ir open
        if(strings[0].equals("open")){
            //创建容器
            String title = ItemRecycle.main.getConfig().getString("menuItem.title");
            Inventory inv = Bukkit.createInventory(null, 6 * 9, title);
            //创建回收按钮
            ItemStack btn = createItem("Confirm");
            inv.setItem(49, btn);
            //创建刷新按钮
            ItemStack btn2 = createItem("Refresh");
            inv.setItem(48, btn2);
            inv.setItem(50, btn2);
            //创建玻璃板
            ItemStack fill = createItem("Fill");
            //填充特殊物品
            for (int i = 0; i < 9; i++) {
                inv.setItem(i, fill);
            }
            //填充特殊物品
            for (int i = 45; i < 54; i++) {
                if (i == 48 || i == 49 || i == 50) continue;
                inv.setItem(i, fill);
            }

            if(strings.length == 1) {
                Player p = (Player) commandSender;
                if (p == null) {
                    String ponly = ItemRecycle.main.getConfig().getString("Message.playerOnly");
                    commandSender.sendMessage(ponly);
                    return false;
                }
                //打开GUI
                p.openInventory(inv);
            }
            if(strings.length == 2){
                Player targetPlayer = Bukkit.getPlayer(strings[1]);
                if (targetPlayer == null || !targetPlayer.isOnline()) {
                    commandSender.sendMessage("玩家不在线或不存在！");
                    return false;
                }
                //打开GUI
                targetPlayer.openInventory(inv);

            }
            return  true;
        }

        // ir set
        if(strings.length == 2 && strings[0].equals("set")){
            if(commandSender == null) {
                String ponly= ItemRecycle.main.getConfig().getString("Message.playerOnly");
                commandSender.sendMessage(ponly);
                return false;
            }
            Player p = (Player) commandSender;
            if (p.hasPermission("itemrecycle.set.*")) {
                if(!(isNumeric(strings[1]))){
                    String error = ItemRecycle.main.getConfig().getString("Message.error");
                    p.sendMessage(error);
                    return false;
                }
                /*   载入一下配置先   */
                Map<Material, Double> itemValues = getItemValues();

                Material itemType = p.getInventory().getItemInMainHand().getType();
                double value = Double.parseDouble(strings[1]);
                itemValues.put(itemType, value);

                String setValue = ItemRecycle.main.getConfig().getString("Message.setValue");
                commandSender.sendMessage(itemType+setValue.replace("%value%",String.valueOf(value)));
                //调用方法保存到配置里面
                ItemRecycle.main.saveConfigValues();
                return true;
            } else {
                String ponly= ItemRecycle.main.getConfig().getString("Message.playerOnly");
                commandSender.sendMessage(ponly);
                return false;
            }
        }
        // ir remove
        if(strings.length == 1 && strings[0].equals("remove")){
            Player p = (Player) commandSender;
            if(commandSender == null) {
                String ponly= ItemRecycle.main.getConfig().getString("Message.playerOnly");
                commandSender.sendMessage(ponly);
                return false;
            }
            if (p.hasPermission("itemrecycle.remove")) {
                /*   载入一下配置先   */
                Map<Material, Double> itemValues = getItemValues();
                Material itemType = p.getInventory().getItemInMainHand().getType();
                if (!(itemValues.containsKey(itemType))) {
                    String unsigned = ItemRecycle.main.getConfig().getString("Message.unsigned");
                    p.sendMessage(unsigned);
                    return false;
                }
                itemValues.remove(itemType);
                String remove = ItemRecycle.main.getConfig().getString("Message.remove");
                commandSender.sendMessage(remove);
                //调用方法保存到配置里面
                ItemRecycle.main.saveConfigValues();
                return true;
            }
        }
        //ir show
        if(strings[0].equals("show")) {
            if(strings.length == 2){
                Player targetPlayer = Bukkit.getPlayer(strings[1]);
                if (targetPlayer == null || !targetPlayer.isOnline()) {
                    commandSender.sendMessage("玩家不在线或不存在！");
                    return false;
                }
                inventories=createInvs(targetPlayer);
                //打开GUI
                targetPlayer.openInventory(inventories[0]);
                return true;

            }
            Player p = (Player) commandSender;
            if(p == null) {
                String ponly= ItemRecycle.main.getConfig().getString("Message.playerOnly");
                commandSender.sendMessage(ponly);
                return false;
            }
            inventories=createInvs(p);
            //打开GUI
            if(strings.length == 1) p.openInventory(inventories[0]);

            return  true;
        }
        // ir see
        if(strings.length == 1 && strings[0].equals("see")) {
            Player p = (Player) commandSender;
            if(p == null) {
                String ponly= ItemRecycle.main.getConfig().getString("Message.playerOnly");
                commandSender.sendMessage(ponly);
                return false;
            }
            Map<Material, Double> itemValues = getItemValues();
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item != null && item.getType() != Material.AIR) {
                Material itemType = item.getType();
                if(itemValues.containsKey(itemType)){
                    double value = itemValues.get(itemType);
                    p.sendMessage("§a当前物品价值：" + value);
                    return true;
                }
            }
            p.sendMessage("§a该物品没有价值噢~");
            return false;
        }
        // ir reload
        if(strings.length == 1 && strings[0].equals("reload")){
            ItemRecycle.main.reloadConfig();
            String reload = ItemRecycle.main.getConfig().getString("Message.reload");
            commandSender.sendMessage(reload);
            return  true;
        }
        String unknowCommand = ItemRecycle.main.getConfig().getString("Message.unknownCommand");
        commandSender.sendMessage(unknowCommand);
        return false;
    }
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) { // 检查字符串是否为空
            return false;
        }
        try {
            Double.parseDouble(str); // 使用Double.parseDouble()方法尝试将字符串转换为double
            return true; // 转换成功，字符串是数字
        } catch (NumberFormatException e) {
            return false; // 转换失败，字符串不是数字
        }

    }
    public static Inventory[] createInvs(Player player){
        int size= itemValues.size();
        Inventory[] inventories = new Inventory[size/45+2];
        /*   总之载入一下配置先   */
        Map<Material, Double> itemValues = getItemValues();
        //创建容器
        String title = ItemRecycle.main.getConfig().getString("menuItem.title");
        String show = ItemRecycle.main.getConfig().getString("menuItem.Coll.show");

        player.sendMessage("可收购种数：" + size);
        for (int i=0;i<(size/45)+1;i++){
            inventories[i] = Bukkit.createInventory(player,6*9, title+show+"["+(i+1)+"/"+((size/45)+1)+"]");
        }

        //填充物品
        int i=0;
        for (Material material : itemValues.keySet()) {
            ItemStack itemStack = new ItemStack(material);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                double price = itemValues.get(material);
                meta.setLore(Collections.singletonList("§f价值: " + price));
                itemStack.setItemMeta(meta);
            }
            inventories[i/45].addItem(itemStack);
            i++;
        }
        for (int j=0;j<(size/45)+1;j++){
            if(j!=(size/45)) {
                //下一页
                ItemStack btn = createItem("ChangePage");
                inventories[j].setItem(50,btn);
            }
            if(j!=0) {
                //上一页
                ItemStack btn = createItem("ChangePage");
                ItemMeta meta2 = btn.getItemMeta();
                meta2.setDisplayName("§f上一页");
                btn.setItemMeta(meta2);
                inventories[j].setItem(48,btn);
            }}
        return inventories;
    }
}