package com.linruo;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Map;

import static com.linruo.ItemRecycle.*;


public class irListener implements Listener {
    boolean flag=false;
    //点击事件
    @EventHandler
    public void click(InventoryClickEvent event){
        Inventory inv = event.getInventory();
        Player p = (Player) event.getWhoClicked();
        Map<Material, Double> itemValues  = getItemValues();

        String title = ItemRecycle.main.getConfig().getString("menuItem.title");
        String show = ItemRecycle.main.getConfig().getString("menuItem.Coll.show");
        //展示页
        if(inv.getTitle().contains((title+show).replace("%player%",p.getName()))){
            Inventory[] inventories=irCommands.inventories;
            //菜单区域
            if(event.getRawSlot()>44 && event.getRawSlot()<=53){
                if(event.getRawSlot()==50){     //下一页
                    for (int i=0;i<itemValues.size()/45;i++){
                        if(inventories[i].getTitle().equals(event.getClickedInventory().getTitle())){
                            p.openInventory(inventories[i+1]);
                        }
                    }
                    return;
                }
                if(event.getRawSlot()==48){     //上一页
                    for (int i=0;i<=(itemValues.size()/45);i++){
                        if(inventories[i].getTitle().equals(event.getClickedInventory().getTitle())){
                            if(i>0) p.openInventory(inventories[i-1]);
                        }
                    }
                    return;
                }
                else {
                    //创建玻璃板
                    ItemStack fill = createItem("Fill");
                    ItemMeta meta = fill.getItemMeta();
                    meta.setLore(Collections.singletonList("§2诶嘿~"));
                    fill.setItemMeta(meta);
                    inv.setItem(event.getRawSlot(),fill);
                }
            }

            event.setCancelled(true);
            return;
        }
        //回收页
        if(inv.getTitle().equals(title.replace("%player%",p.getName()))){
            //物品栏区域
            if(event.getCurrentItem() != null && event.getCurrentItem().getAmount()!=0 && event.getRawSlot()>53){

                //且是未进配置物品，返回
                if (!(itemValues.containsKey(event.getCurrentItem().getType()))) {

                    // 显示提示
                    String canot = ItemRecycle.main.getConfig().getString("Message.cannotrecycle");
                    p.sendMessage(canot);

                    // 阻止被放入
                    event.setCancelled(true);
                    return;
                }
                //物品栏且在配置内 第一次点击成功
                if(itemValues.containsKey(event.getCurrentItem().getType())){
                    flag = true;
                    return;
                }
            }

            //回收区域
            if(event.getRawSlot()>=9 && event.getRawSlot()<=44){

                if(flag) {
                    updateConfirmButton(p,inv);
                }
                flag = false;
                return;
            }
            //菜单区域
            if((event.getRawSlot()>44 && event.getRawSlot()<=53) || (event.getRawSlot()>=0 && event.getRawSlot()<9)){
                if(event.getRawSlot()==49){
                    //创建按钮
                    ItemStack btn = createItem("Confirm");
                    inv.setItem(49,btn);
                    //回收
                    p.closeInventory();
                }else if(event.getRawSlot()==48 || event.getRawSlot()==50){
                    //创建刷新按钮
                    updateConfirmButton(p,inv);
                    ItemStack btn = createItem("Refresh");
                    inv.setItem(48,btn);
                }
                else {
                    //创建玻璃板
                    ItemStack fill = createItem("Fill");
                    inv.setItem(event.getRawSlot(),fill);
                }
                event.setCancelled(true);
            }
        }
    }
    
    //打开菜单
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player p = (Player) event.getPlayer();
        String title = ItemRecycle.main.getConfig().getString("menuItem.title");
        if (event.getInventory().getName().equals(title.replace("player",p.getName()))) {
            updateConfirmButton(p, event.getInventory());
        }
    }

    //关闭背包
    @EventHandler
    public void close(InventoryCloseEvent event){
        Inventory inv = event.getInventory();
        Player p = (Player) event.getPlayer();
        String title = ItemRecycle.main.getConfig().getString("menuItem.title");

        if(inv.getTitle().equals(title.replace("player",p.getName()))){
            double v = calculateTotalValue(inv);
            //回收
            if(v>0) recycle(p,inv);
        }

    }
    //回收
    private void recycle(Player p,Inventory inv){
        double totalValue = calculateTotalValue(inv);
        econ.depositPlayer(p, totalValue);
        inv.clear();
        String recycle = ItemRecycle.main.getConfig().getString("Message.recycle");
        String coin = ItemRecycle.main.getConfig().getString("Message.coin");
        p.sendMessage( recycle + totalValue + coin);
    }
    //计算总价值
    private double calculateTotalValue(Inventory inventory) {
        Map<Material, Double> itemValues  = getItemValues();
        double totalValue = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if(item.getItemMeta().hasDisplayName()){
                    continue;
                }

                // 在这里计算物品的价值并添加到总价值中
                Material itemType = item.getType();
                if(itemValues.containsKey(itemType)){
                    double value = itemValues.get(itemType);
                    totalValue += value * item.getAmount();
                }
            }
        }
        return totalValue;
    }
    //更新总价值
    private void updateConfirmButton(Player player, Inventory inventory) {
        ItemStack confirmButton = createItem("Confirm");
        ItemMeta meta = confirmButton.getItemMeta();

        double totalValue = calculateTotalValue(inventory);
        String coin = ItemRecycle.main.getConfig().getString("Message.coin");
        String total = ItemRecycle.main.getConfig().getString("menuItem.Confirm.total");
        meta.setLore(Collections.singletonList(total + totalValue + coin));//.replace("%totalValue%", + "§6世初币")

//        player.sendMessage("已更新，总价值为:" + String.valueOf(toString()));

        confirmButton.setItemMeta(meta);
        inventory.setItem(49, confirmButton);
    }


}
