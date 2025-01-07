package org.hhlaowang.hhPlayerTp;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UiButton {
    static public Map<Integer, UiButton> mapping = new HashMap<Integer, UiButton>();
    public List<Integer> Index = new ArrayList<>();         // 按钮的索引列表
    public String cmd = "";                                 // 按钮要执行的命令
    public Material material = Material.AIR;                // 按钮的材质
    public int customModelData = 0;                         // 自定义的模型ID, 更换材质需要用到
    public String display;                                  // 显示的文本

    // 判断指定索引的位置是否存在按钮, 如果存在, 那么返回该按钮, 否则, 返回null
    static public UiButton UiButtonMapContainIndex(List<UiButton> list, Integer index){
        for(UiButton uiButton : list){
            if(uiButton.Index.contains(index)){
                return uiButton;
            }
        }
        return null;
    }
}
