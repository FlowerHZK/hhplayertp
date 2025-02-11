package org.hhlaowang.hhPlayerTp;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hhlaowang.hhPlayerTp.HhPlayerTp.*;

public class PlayerGuiListen implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory openInventory = e.getInventory();        // 判断玩家点击的GUI是不是当前类的GUI
        if(openInventory.getHolder() instanceof CustomHolder){
            // 取消事件: 这样玩家就没办法拿出来物品了
            e.setCancelled(true);
            Player player = (Player)e.getWhoClicked();
            int RawSlot = e.getRawSlot();
            // getRawSlot获得玩家点击的格子编号
            // 但是玩家点击GUI之外不是格子的地方也会触发InventoryClickEvent, 需要做处理!
            if(RawSlot < 0 || RawSlot >= e.getInventory().getSize()){
                return;
            }
            if(playerButtons.contains(RawSlot)){        // 判断点击的位置是不是玩家按钮的位置
                PlayerGuiData playerGuiDataOne = playerGuiDatas.get(player);    // 获取当前玩家的GUI数据
                int playerIdx = playerGuiDataOne.page * playerButtons.size();   // 获取当前的玩家索引 = 页码*数量 + 0
                for(int idx = 0; (idx < playerButtons.size()) &&                // 遍历所有按钮, 直到找到对应的玩家, 但是如果没有找到, 那么认为点击的位置不是玩家位置
                        (playerIdx < playerGuiDataOne.serverPlayerList.size()); idx++,playerIdx++){
                    if(playerButtons.get(idx) == RawSlot){
                        // 获取目标玩家
                        String destPlayerName = playerGuiDataOne.serverPlayerList.get(idx);
                        Player destPlayer = Bukkit.getPlayer(destPlayerName);
                        if(destPlayer != null){
                            if(destPlayer.equals(player)){
                                player.sendMessage(ColorToCom.colorStringToComponent("&4你不能向自己发送传送请求"));
                                break;
                            }
                            // 左键: 向目标玩家destPlayerName玩家发送指定的指令
                            if(e.getClick() == ClickType.LEFT){
                                String commandToExecute = PlaceholderAPI.setPlaceholders(destPlayer, playerCmdLeft);
                                Bukkit.getServer().dispatchCommand(player, commandToExecute);
                            }
                            // 右键: 向目标玩家destPlayerName玩家发送指定的指令
                            else if(e.getClick() == ClickType.RIGHT){
                                String commandToExecute = PlaceholderAPI.setPlaceholders(destPlayer, playerCmdRight);
                                Bukkit.getServer().dispatchCommand(player, commandToExecute);
                            }

                            // 向目标玩家发送聊天框点击事件
                            genPlayerChatMessage(player);     // 生成目标玩家的聊天框内容(解析papi), 变量所有者为发出请求的玩家
                            for(Component p:playerChatMessage){
                                destPlayer.sendMessage(p);
                            }
                        }
                        break;
                    }
                }
            }else{
                UiButton uibutton = UiButton.UiButtonMapContainIndex(HhPlayerTp.cmdButtonList, RawSlot);
                if(uibutton != null){     // 这里添加指令按钮的响应(可以执行任何指令)
                    String commandToExecute = PlaceholderAPI.setPlaceholders(player, uibutton.cmd);
                    Bukkit.getServer().dispatchCommand(player, commandToExecute);
                }
            }
        }
    }

    // 此段代码为AI生成代码 FIXME 后续需要整理代码, 移动带其他地方
    // 最后: 分割字符串, 并解析颜色代码&或§转为Component
    public static List<Component> splitStringToComponents(String input) {
        List<Component> components = new ArrayList<>();
        // 定义匹配 [accept] 和 [deny] 的正则表达式模式
        Pattern specialPattern = Pattern.compile("\\[accept]|\\[deny]");
        Matcher specialMatcher = specialPattern.matcher(input);

        int start = 0;
        while (specialMatcher.find()) {
            // 获取特殊元素之前的普通字符串部分并添加为组件（如果有）
            if (specialMatcher.start() > start) {
                String normalPart = input.substring(start, specialMatcher.start());
                Component component = ColorToCom.colorStringToComponent(normalPart);
                components.add(component);
            }
            // 添加特殊元素作为组件
            Component component = ColorToCom.colorStringToComponent(specialMatcher.group());
            components.add(component);
            start = specialMatcher.end();
        }

        // 添加最后剩余的普通字符串部分（如果有）
        if (start < input.length()) {
            String lastPart = input.substring(start);
            Component component = ColorToCom.colorStringToComponent(lastPart);
            components.add(component);
        }
        return components;
    }
}
