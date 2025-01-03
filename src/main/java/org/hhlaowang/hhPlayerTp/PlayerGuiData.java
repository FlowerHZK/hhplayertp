package org.hhlaowang.hhPlayerTp;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class PlayerGuiData {
    public int page = 0;            // 当前页码(玩家打开菜单的时候会自动清零)
    Inventory openedInventory;      // 玩家当前打开的GUI
    ArrayList<String> serverPlayerList = new ArrayList<>();  // 打开GUI的时候获取到的玩家
    Player player;                  // 当前打开GUI的玩家
}
