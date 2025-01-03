package org.hhlaowang.hhPlayerTp;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.hhlaowang.hhPlayerTp.PlayerGuiListen.splitStringToComponents;

public final class HhPlayerTp extends JavaPlugin implements CommandExecutor {
    static public int guiLine;                          // GUI的行数
    static public String guiTitle;                      // GUI的标题
    static public List<Integer> prevButtons;            // 上一页按钮的位置
    static public List<Integer> nextButtons;            // 下一页按钮的位置
    static public List<Integer> playerButtons;          // 玩家按钮的位置(点击之后发出tpa指令)
    static public String playerCmdLeft;
    static public String playerCmdRight;
    static public List<Component> playerChatMessage;    // 玩家聊天框内容
    static public List<String> playerChatBoxText;       // 玩家聊天框内容, 原始文本


    static public Material prevButtonMaterial;          // 上一页按钮的材质
    static public Material nextButtonMaterial;          // 下一页按钮的材质
    static public Integer prevButtonCustomModelData;    // 自定义模型数据
    static public Integer nextButtonCustomModelData;    // 自定义模型数据

    //类名随意, 这个是用于箱子GUI的判断的, 因为现在已经不需要用Title来对箱子进行判断了
    public static class CustomHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    // 玩家打开的GUI的数据, 例如当前页码, 当前获取到的服务器玩家, 当前打开的箱子GUI界面等
    static public Map<Player, PlayerGuiData> playerGuiDatas = new HashMap<>();

                //    按钮名字  按钮{索引, 指令}
    static public Map<String, UiButton> cmdButtonMaps = new HashMap<>();

    private List<Integer> GetSlotRange(String slotString) {
        if (slotString.startsWith("[") && slotString.endsWith("]") && slotString.length() >= 2) {
            slotString = slotString.substring(1, slotString.length() - 1);
        }
        String[] rangeParts = slotString.split("-");
        int startSlot = Integer.parseInt(rangeParts[0]);
        int endSlot = Integer.parseInt(rangeParts[1]);
        List<Integer> slotList = new ArrayList<>();
        for (int i = startSlot; i <= endSlot; i++) {
            slotList.add(i);
        }
        return slotList;
    }

    private List<Integer> IntListStringToIntList(String IntListString) {
        // 包含数字字符串的列表（假设是类似 "[9, 10, 11, 12, 13, 14, 15]" 这样的形式先存在一个元素的列表中）
        List<String> stringList = Collections.singletonList(IntListString);
        // 第一步先去除首尾的中括号，然后按逗号分割得到单个数字字符串的列表
        List<String> numbersStringList = Arrays.asList(stringList.get(0).substring(1, stringList.get(0).length() - 1).split(","));
        // 使用Java 8的Stream API将每个数字字符串转换为整数，并收集到新的List<Integer>中
        return numbersStringList.stream()
                .map(String::trim)  // 去除字符串两边可能的空白字符
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    @Override
    public void onEnable() {
        // 初始化日志记录
        Log.Init("[" + this.getName() + "]");

        // 设置指令处理
        PluginCommand pluginCommand = Bukkit.getPluginCommand("hhPlayerTp");
        if(pluginCommand != null) {
            pluginCommand.setExecutor(this);
        }else{
            Log.warning("[hhPlayerTp] Plugin command not found");
        }
        // 注册事件监听
        Bukkit.getPluginManager().registerEvents(new PlayerGuiListen(),this);
        // 读取配置文件
        loadConfig();
    }

    // 读取配置文件
    public void loadConfig() {
        // 清空所有配置变量
        guiLine = -1;
        guiTitle = null;
        prevButtons = new ArrayList<>();
        nextButtons = new ArrayList<>();
        playerButtons = new ArrayList<>();
        prevButtonMaterial = Material.AIR;
        nextButtonMaterial = Material.AIR;
        prevButtonCustomModelData = 0;
        nextButtonCustomModelData = 0;
        cmdButtonMaps = new HashMap<>();

        Log.info("--------------------hhplayertp--------------------");
        // 保存默认的config.yml文件, 如果已经存在了, 那么不需要创建
        FileConfiguration config;
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
            Log.warning("config.yml不存在, 正在生成默认的配置文件config.yml");
            config = this.getConfig();
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
        // 获取配置版本
        String version = config.getString("config_version");
        Log.info("配置文件版本 : " + version);
        // 获取玩家传送菜单标题
        guiTitle = config.getString("playertp.title", "玩家传送");  // 菜单的标题
        Log.info("guiTitle = " + guiTitle);
        guiLine = config.getInt("playertp.line", 6);              // 菜单的行数

        // 读取chat_box_text的内容, 并解析出详细信息
        List<String> playerChatBoxText = config.getStringList("playertp.chat_box_text");
        genPlayerChatMessage(null);     // 后续需要移动到玩家点击事件那里, 并且传入参数修改为, 请求发出者




        // 获取playertp下面的具体内容
        ConfigurationSection cfSec = config.getConfigurationSection("playertp.items");
        assert cfSec != null;
        for (String itemName : cfSec.getKeys(false)) {
            String fun = cfSec.getString(itemName + ".fun");
            if(fun != null) {
                switch (fun) {
                    case "player" -> {
                        // 获取玩家的按钮位置
                        String slotRange = cfSec.getString(itemName + ".slot");
                        if (slotRange != null) {
                            if(slotRange.contains("-")){
                                playerButtons = GetSlotRange(slotRange);
                            }else if(slotRange.contains("[") && slotRange.contains("]")){
                                playerButtons = IntListStringToIntList(slotRange);
                            }
                            Log.info(itemName + " Index = " + playerButtons);
                        }
                        // 获取玩家的按钮需要执行的指令:   左键
                        playerCmdLeft = cfSec.getString(itemName + ".left");
                        if(playerCmdLeft != null) {
                            Log.info(itemName + " LeftCmd = " + playerCmdLeft);
                        }
                        // 获取玩家的按钮需要执行的指令:   右键
                        playerCmdRight = cfSec.getString(itemName + ".right");
                        if(playerCmdRight != null) {
                            Log.info(itemName + " RightCmd = " + playerCmdRight);
                        }
                    }
                    case "prev_page" -> {
                        // 获取Prev的按钮材质
                        String materialName = cfSec.getString(itemName + ".material");
                        if (materialName != null) {
                            prevButtonMaterial = Material.getMaterial(materialName);
                        }else{
                            prevButtonMaterial = Material.ARROW;
                        }
                        Log.info(itemName + " Material = " + prevButtonMaterial);
                        // 获取Prev的按钮位置
                        String slotRange = cfSec.getString(itemName + ".slot");
                        if (slotRange != null) {
                            if (slotRange.contains("-")) {
                                prevButtons = GetSlotRange(slotRange);
                            }else if(slotRange.contains("[") && slotRange.contains("]")){
                                prevButtons = IntListStringToIntList(slotRange);
                            }
                        }
                        Log.info(itemName + " Index = " + prevButtons);
                        // 获取Prev的CustomModelData
                        prevButtonCustomModelData = cfSec.getInt(itemName + ".custom_model_data", 0);
                        Log.info(itemName + " CustomModelData = " + prevButtonCustomModelData);
                    }
                    case "next_page" -> {
                        // 获取Prev的按钮材质
                        String materialName = cfSec.getString(itemName + ".material");
                        if (materialName != null) {
                            Material material = Material.getMaterial(materialName);
                            if (material != null) {
                                nextButtonMaterial = Material.getMaterial(materialName);
                            }else{
                                nextButtonMaterial = Material.ARROW;
                            }
                        }else{
                            nextButtonMaterial = Material.ARROW;
                        }
                        Log.info(itemName + " Material = " + nextButtonMaterial);
                        // 获取Prev的按钮位置
                        String slotRange = cfSec.getString(itemName + ".slot");
                        if (slotRange != null) {
                            if(slotRange.contains("-")) {
                                nextButtons = GetSlotRange(slotRange);
                            }else if(slotRange.contains("[") && slotRange.contains("]")){
                                nextButtons = IntListStringToIntList(slotRange);
                            }
                        }
                        Log.info(itemName + " Index = " + nextButtons);
                        // 获取Next的CustomModelData
                        nextButtonCustomModelData = cfSec.getInt(itemName + ".custom_model_data", 0);
                        Log.info(itemName + " CustomModelData = " + nextButtonCustomModelData);
                    }
                    case "cmd" -> {
                        UiButton uiButton = new UiButton();
                        // 1. 获取cmd按钮的材质
                        String materialName = cfSec.getString(itemName + ".material");
                        if (materialName != null) {
                            Material material = Material.getMaterial(materialName);
                            uiButton.material = Objects.requireNonNullElse(material, Material.ARROW);
                        }else{
                            uiButton.material = Material.STONE;
                        }
                        // 2. 获取cmd按钮的索引
                        String slotRange = cfSec.getString(itemName + ".slot");
                        if (slotRange != null) {
                            if(slotRange.contains("-")) {
                                uiButton.Index = GetSlotRange(slotRange);
                            }else if(slotRange.contains("[") && slotRange.contains("]")){
                                uiButton.Index = IntListStringToIntList(slotRange);
                            }
                        } else {
                            uiButton.Index = null;
                        }
                        Log.info(itemName + " Button Location = " + uiButton.Index);
                        // 3. 获取cmd按钮的 CustomModelData
                        uiButton.customModelData = cfSec.getInt(itemName + ".custom_model_data", 0);
                        Log.info(itemName + " customModelData = " + uiButton.customModelData);
                        // 4. 获取cmd按钮的命令(cmd)
                        uiButton.cmd = cfSec.getString(itemName + ".cmd");
                        Log.info(itemName + " cmd = " + uiButton.cmd);
                    }
                }
            }
        }
        Log.info("--------------------hhplayertp--------------------");
    }

    // 生成玩家聊天框内容
    public static void genPlayerChatMessage(Player requestSender){
        Component componentAccept =  ColorToCom.colorStringToComponent("[同意]");     // 用于判断是否存在同意的字符串
        Component componentDeny =  ColorToCom.colorStringToComponent("[拒绝]");       // 用于判断是否存在拒绝的字符串
        if(playerChatBoxText != null) {
            playerChatMessage = new ArrayList<>();
            for(String one : playerChatBoxText) {
                List<Component> listSp = splitStringToComponents(one);        // 分割一行的内容, 如果有[同意] 或 [拒绝] 的话
                Component componentOneLine = Component.text("");
                for(Component c : listSp) {
                    if(c.equals(componentAccept)){                          // 如果是同意的字符串, 那么需要添加点击事件
                        Component MsgAccept = Component.text("[同意]").color(TextColor.color(0, 255, 0));           // 显示文本
                        MsgAccept = MsgAccept.hoverEvent(HoverEvent.showText(ColorToCom.colorStringToComponent("点击同意传送")));     // 悬停文本
                        MsgAccept = MsgAccept.clickEvent(ClickEvent.runCommand("/tpaccept"));                                       // 执行指令
                        componentOneLine = componentOneLine.append(MsgAccept);
                    }else if(c.equals(componentDeny)){                      // 如果是拒绝的字符串, 那么需要添加点击事件
                        Component MsgDeny = Component.text("[拒绝]").color(TextColor.color(255, 0, 0));             // 显示文本
                        MsgDeny = MsgDeny.hoverEvent(HoverEvent.showText(ColorToCom.colorStringToComponent("点击拒绝传送")));         // 悬停文本
                        MsgDeny = MsgDeny.clickEvent(ClickEvent.runCommand("/tpdeny"));                                             // 执行指令
                        componentOneLine = componentOneLine.append(MsgDeny);
                    }else{                                                  // 如果都不是, 那么直接追加
                        componentOneLine = componentOneLine.append(c);
                    }
                }
                playerChatMessage.add(componentOneLine);
            }
        }else{
            Log.info("listchat_box_text = null");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender,
                             @NotNull Command command,
                             @NotNull String s,
                             String[] args) {
        if(commandSender instanceof Player player) {
            if(args.length == 1){
                if (args[0].equals("open")) {
                    GuiOpen(player);            // 打开菜单
                }
            }
        }else{
            Log.info("onCommand length = " + args.length);
            if(args.length == 1){
                if (args[0].equals("reload")){
                    loadConfig();
                }
            }
        }
        return false;
    }

    void GuiOpen(Player player){
        PlayerGuiData playerGuiData = new PlayerGuiData();
        // 1. 创建GUI
        playerGuiData.openedInventory = Bukkit.createInventory(new CustomHolder(), guiLine * 9,
                MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(null, guiTitle)));
        // 2.1 获取当前玩家列表
        playerGuiData.serverPlayerList.clear();
        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
        for (Player player1 : players) {
            playerGuiData.serverPlayerList.add(player1.getName());
        }
        // 2.2 使用sort()的Comparator.naturalOrder()比较器(自然顺序)方法对curPlayerSetFlags进行排序
        playerGuiData.serverPlayerList.sort(Comparator.naturalOrder());
        // 3. 设置当前页码为0
        playerGuiData.page = 0;
        // 4. 保存当前打卡开GUI的玩家
        playerGuiData.player = player;
        // 5. 刷新菜单
        GuiRefresh(playerGuiData);
        // 6. 打开GUI
        player.openInventory(playerGuiData.openedInventory);
        // 7. 保存playerGuiData
        playerGuiDatas.put(player, playerGuiData);
    }



    void GuiRefresh(PlayerGuiData playerGuiData){
        int playerIdx = 0;
        for(int i = 0; i < guiLine*9; i++){
            if(playerButtons.contains(i) && (playerIdx < playerGuiData.serverPlayerList.size())){
                ItemMeta meta = new ItemStack(Material.PLAYER_HEAD).getItemMeta();
                ItemStack playerSkull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) playerSkull.getItemMeta();
                // 设置头颅所属玩家的名字（假设是名为"Notch"的玩家）
                skullMeta.setOwningPlayer(playerGuiData.player);
                playerSkull.setItemMeta(skullMeta);
                // 明确文本内容
                Component component = MiniMessage.miniMessage().deserialize(playerGuiData.serverPlayerList.get(playerIdx));
                // 设置字体颜色
                component = component.color(TextColor.color(HSVLike.fromRGB(0,255,0)));
                // 设置字体斜体为 false
                component = component.decoration(TextDecoration.ITALIC, false);
                // 设置物品显示名称
                meta.displayName(component);
                Component loreLine1;            // 描述
                loreLine1 = MiniMessage.miniMessage().deserialize("左键请求传送至对方位置").color(TextColor.color(HSVLike.fromRGB(0,255,0)));
                loreLine1 = loreLine1.decoration(TextDecoration.ITALIC, false);

                Component loreLine2 = MiniMessage.miniMessage().deserialize("右键请求对方传送至你的位置");            // 描述
                loreLine2 = loreLine2.color(TextColor.color(HSVLike.fromRGB(255,255,0)));
                loreLine2 = loreLine2.decoration(TextDecoration.ITALIC, false);

                List <Component> listLore = new ArrayList<>();
                listLore.add(loreLine1);
                listLore.add(loreLine2);
                meta.lore(listLore);
                playerSkull.setItemMeta(meta);
                // 将玩家头颅放入GUI的第一个格子（索引为0）
                playerGuiData.openedInventory.setItem(i, playerSkull);
                playerIdx++;
            }
        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
