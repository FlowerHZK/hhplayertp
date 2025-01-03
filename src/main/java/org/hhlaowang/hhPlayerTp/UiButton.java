package org.hhlaowang.hhPlayerTp;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class UiButton {
    public List<Integer> Index = new ArrayList<>();         // 按钮的索引列表
    public String cmd = "";                                 // 按钮要执行的命令
    public Material material = Material.AIR;    // 按钮的材质
    public int customModelData = 0;                         // 自定义的模型ID, 更换材质需要用到
}
