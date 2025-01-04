package org.hhlaowang.hhPlayerTp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ColorToCom {
    public static Component colorStringToComponent(@NotNull String colorString) {
        if(!colorString.isEmpty()){
            // 将 & 符号的颜色代码转换为 § 符号，这是Minecraft中实际使用的颜色代码前缀
            colorString = colorString.replace("&", "§");
        }
        // 使用 LegacyComponentSerializer 解析颜色代码并转换为 Component
        Component component = LegacyComponentSerializer.legacySection().deserialize(colorString);
        component = component.decoration(TextDecoration.ITALIC, false);                        // 非斜体
        return component;
    }
}
