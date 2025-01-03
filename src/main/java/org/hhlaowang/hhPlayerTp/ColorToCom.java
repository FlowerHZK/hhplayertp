package org.hhlaowang.hhPlayerTp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorToCom {
//    public static Component colorStringToComponent(Player player, String colorString) {
//        if(colorString != null && !colorString.isEmpty()){
//            // 将 & 符号的颜色代码转换为 § 符号，这是Minecraft中实际使用的颜色代码前缀
//            colorString = colorString.replace("&", "§");
//        }
//        colorString = PlaceholderAPI.setPlaceholders(player, colorString);
//        // 使用 LegacyComponentSerializer 解析颜色代码并转换为 Component
//        Component component = LegacyComponentSerializer.legacySection().deserialize(colorString);
//        component = component.decoration(TextDecoration.ITALIC, false);                        // 非斜体
//        return component;
//    }
    public static Component colorStringToComponent(String colorString) {
        if(colorString != null && !colorString.isEmpty()){
            // 将 & 符号的颜色代码转换为 § 符号，这是Minecraft中实际使用的颜色代码前缀
            colorString = colorString.replace("&", "§");
        }
        // 使用 LegacyComponentSerializer 解析颜色代码并转换为 Component
        Component component = LegacyComponentSerializer.legacySection().deserialize(colorString);
        component = component.decoration(TextDecoration.ITALIC, false);                        // 非斜体
        return component;
    }
}
