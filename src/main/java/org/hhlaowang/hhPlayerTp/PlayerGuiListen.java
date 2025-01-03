package org.hhlaowang.hhPlayerTp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import me.clip.placeholderapi.PlaceholderAPI;

import static org.hhlaowang.hhPlayerTp.HhPlayerTp.*;

public class PlayerGuiListen implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory openInventory = e.getInventory();        // 判断玩家点击的GUI是不是当前类的GUI
        if(openInventory.getHolder() instanceof CustomHolder){
            //判定成功后执行的代码
            // 取消事件: 这样玩家就没办法拿出来物品了
            e.setCancelled(true);
            // 自从Mojang把HIM删掉以后, 能触发InventoryClickEvent的只有Player了
            // 目前来说可以直接把它强转成Player
            Player player = (Player)e.getWhoClicked();
            int RawSlot = e.getRawSlot();
            // getRawSlot获得玩家点击的格子编号
            // 但是玩家点击GUI之外不是格子的地方也会触发InventoryClickEvent, 需要做处理!
            if(RawSlot < 0 || RawSlot >= e.getInventory().getSize()){
                return;
            }
            if(playerButtons.contains(RawSlot)){        // 判断点击的位置是不是玩家的位置
                PlayerGuiData playerGuiDataOne = playerGuiDatas.get(player);     // 获取当前玩家的GUI数据
                int playerIdx = playerGuiDataOne.page * playerButtons.size();   // 获取当前的玩家索引
                for(int idx = 0; (idx < playerButtons.size()) &&                // 遍历所有按钮, 直到找到对应的玩家, 但是如果没有找到, 那么认为点击的位置不是玩家位置
                        (playerIdx < playerGuiDataOne.serverPlayerList.size()); idx++,playerIdx++){
                    if(playerButtons.get(idx) == RawSlot){
                        Log.info("playerIdx: " + playerIdx);
                        // 获取目标玩家
                        String destPlayerName = playerGuiDataOne.serverPlayerList.get(idx);
                        Log.info("玩家索引 : " + playerIdx + " 名字: [" + destPlayerName + "]");
                        Player destPlayer = Bukkit.getPlayer(destPlayerName);
                        if(destPlayer != null){
                            // 向目标玩家destPlayerName玩家发送tpa的执行
                            if(e.getClick() == ClickType.LEFT){
                                String commandToExecute = "tpa " + destPlayerName;
                                Bukkit.getServer().dispatchCommand(player, commandToExecute);
                            }
                            // 向目标玩家destPlayerName玩家发送tpahere的执行
                            else if(e.getClick() == ClickType.RIGHT){
                                String commandToExecute = "tpahere " + destPlayerName;
                                Bukkit.getServer().dispatchCommand(player, commandToExecute);
                            }

                            // 向目标玩家发送点击事件
                            Component hoverTipSpace = Component.
                                    text("");
                            destPlayer.sendMessage(hoverTipSpace);      // 第一行空行
                            Component hoverTipRequest = ColorToCom.colorStringToComponent("&6  玩家[&4" + player.getName() + "&6]向你发送传送请求");
                            Component hoverTipImg = Component.
                                    text(PlaceholderAPI.setPlaceholders(null, ":hhplayertptip:"));//构建一个绿色的悬停文本
                            destPlayer.sendMessage(hoverTipRequest);    // 第二行请求文本
                            destPlayer.sendMessage(hoverTipSpace);      // 第三行空行
                            Component MsgAccept = getComponent();       // 生成第四行的内容, 因为是有点击事件的, 所以放在了这里面
                            destPlayer.sendMessage(MsgAccept);          // 第四行 : 同意. 取消
                            destPlayer.sendMessage(hoverTipImg);        // 图片放在最后一行
                        }else{
                            Log.info("获取不到玩家");
                        }
                        break;
                    }
                }
            }else{
                Log.info("点击的位置不是玩家位置");
            }
        }
    }

    private static @NotNull Component getComponent() {
        Component hoverTextAccept = Component.text("点击同意传送")
                .color(TextColor.color(0, 255, 0));//构建一个绿色的悬停文本
        Component MsgAccept = Component.text("  [同意]").color(TextColor.color(0, 255, 0));
        MsgAccept = MsgAccept.hoverEvent(HoverEvent.showText(hoverTextAccept));
        MsgAccept = MsgAccept.clickEvent(ClickEvent.runCommand("/tpaccept"));
        Component MsgSpace = Component.text("        ");
        MsgSpace = MsgSpace.hoverEvent(HoverEvent.showText(MsgSpace));
        MsgSpace = MsgSpace.clickEvent(null);       // 这里我想的是传入null, 让玩家点击不产生任何事件, 还没有试过, 不知道可不可行
        Component MsgDeny = Component.text("[拒绝]")
                .color(TextColor.color(255, 0, 0));//构建一个红色的悬停文本
        Component hoverTextDeny = Component.text("点击拒绝传送").color(TextColor.color(255, 0, 0));
        MsgDeny = MsgDeny.hoverEvent(HoverEvent.showText(hoverTextDeny));
        MsgDeny = MsgDeny.clickEvent(ClickEvent.runCommand("/tpdeny"));
        MsgAccept = MsgAccept.append(MsgSpace);
        MsgAccept = MsgAccept.append(MsgDeny);
        return MsgAccept;
    }
}
