package com.Tempce.tempceLib.gui.manager.handlers;

import com.Tempce.tempceLib.command.data.ArgumentData;
import com.Tempce.tempceLib.command.data.ArgumentType;
import com.Tempce.tempceLib.gui.data.GUIItemData;
import com.Tempce.tempceLib.gui.data.MaterialCategory;
import com.Tempce.tempceLib.gui.manager.GUIManager;
import com.Tempce.tempceLib.gui.manager.util.GUIItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 引数入力GUI管理クラス
 * 各引数タイプに応じた入力GUIを生成・管理します
 */
public class ArgumentInputGUIManager {
    
    private final ArgumentInputChatListener chatListener;
    
    public ArgumentInputGUIManager(ArgumentInputChatListener chatListener) {
        this.chatListener = chatListener;
    }
    
    /**
     * 引数入力のための段階的GUIセッション情報
     */
    public static class ArgumentInputSession {
        private final String commandName;
        private final String subCommandPath;
        private final List<ArgumentData> argumentsTemplate;
        private final List<String> collectedArguments;
        private int currentArgumentIndex;
        
        public ArgumentInputSession(String commandName, String subCommandPath, List<ArgumentData> argumentsTemplate) {
            this.commandName = commandName;
            this.subCommandPath = subCommandPath;
            this.argumentsTemplate = argumentsTemplate;
            this.collectedArguments = new ArrayList<>();
            this.currentArgumentIndex = 0;
        }
        
        public String getCommandName() { return commandName; }
        public String getSubCommandPath() { return subCommandPath; }
        public List<ArgumentData> getArgumentsTemplate() { return argumentsTemplate; }
        public List<String> getCollectedArguments() { return collectedArguments; }
        public int getCurrentArgumentIndex() { return currentArgumentIndex; }
        
        public void addArgument(String value) {
            collectedArguments.add(value);
            currentArgumentIndex++;
        }
        
        public boolean hasMoreArguments() {
            return currentArgumentIndex < argumentsTemplate.size();
        }
        
        public ArgumentData getCurrentArgument() {
            if (hasMoreArguments()) {
                return argumentsTemplate.get(currentArgumentIndex);
            }
            return null;
        }
        
        public String buildFinalCommand() {
            StringBuilder command = new StringBuilder(commandName);
            if (!subCommandPath.isEmpty()) {
                command.append(" ").append(subCommandPath.replace(".", " "));
            }
            for (String arg : collectedArguments) {
                command.append(" ").append(arg);
            }
            return command.toString();
        }
    }
    
    /**
     * 引数入力GUIを開始
     */
    public void startArgumentInputFlow(Player player, String commandName, String subCommandPath, 
                                     List<ArgumentData> arguments, Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        if (arguments == null || arguments.isEmpty()) {
            // 引数がない場合は直接実行
            String command = commandName + (subCommandPath.isEmpty() ? "" : " " + subCommandPath.replace(".", " "));
            player.closeInventory();
            player.performCommand(command);
            return;
        }
        
        ArgumentInputSession session = new ArgumentInputSession(commandName, subCommandPath, arguments);
        showArgumentInputGUI(player, session, paginationCreator);
    }
    
    /**
     * 現在の引数に応じた入力GUIを表示
     */
    public void showArgumentInputGUI(Player player, ArgumentInputSession session, 
                                    Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        if (!session.hasMoreArguments()) {
            // 全ての引数が揃ったのでコマンドを実行
            String finalCommand = session.buildFinalCommand();
            player.closeInventory();
            player.performCommand(finalCommand);
            return;
        }
        
        ArgumentData currentArg = session.getCurrentArgument();
        
        switch (currentArg.getType()) {
            case ONLINE_PLAYER:
                showPlayerSelectionGUI(player, session, paginationCreator, true); // オンラインのみ
                break;
            case ALL_PLAYER:
                showPlayerSelectionGUI(player, session, paginationCreator, false); // オフライン含む
                break;
            case WORLD:
                showWorldSelectionGUI(player, session, paginationCreator);
                break;
            case ITEM_ID, ITEM_ID_TOOL, ITEM_ID_BLOCK, ITEM_ID_NATURE_BLOCK, ITEM_ID_WEAPON_ARMOR, ITEM_ID_DECORATION,
                 ITEM_ID_FOOD:
                showItemSelectionGUI(player, session, paginationCreator);
                break;
          case BOOLEAN:
                showBooleanSelectionGUI(player, session, paginationCreator);
                break;
            case ENCHANTMENT:
                showEnchantmentSelectionGUI(player, session, paginationCreator);
                break;
            case POTION_EFFECT:
                showPotionEffectSelectionGUI(player, session, paginationCreator);
                break;
            case INTEGER:
                showIntegerSelectionUsingGUIManager(player, session, paginationCreator);
                break;
            case DOUBLE:
                showDoubleSelectionUsingGUIManager(player, session, paginationCreator);
                break;
            case COORDINATE_X:
            case COORDINATE_Y:
            case COORDINATE_Z:
                showCoordinateSelectionGUI(player, session, paginationCreator);
                break;
            default:
                showTextInputGUI(player, session, paginationCreator);
                break;
        }
    }
    
    /**
     * プレイヤー選択GUI
     */
    private void showPlayerSelectionGUI(Player player, ArgumentInputSession session, 
                                      Consumer<CommandGUIManager.CommandGUIData> paginationCreator, boolean onlineOnly) {
        ArgumentData arg = session.getCurrentArgument();
        
        if (onlineOnly) {
            // オンラインプレイヤーのみ - 既存のAPIを使用
            GUIManager.getInstance().createPlayerSelectionGUI(player, 
                    ChatColor.BLUE + "オンラインプレイヤーを選択: " + arg.getName(),
                    (selectedPlayer) -> {
                        session.addArgument(selectedPlayer.getName());
                        showArgumentInputGUI(player, session, paginationCreator);
                    });
        } else {
            // 全プレイヤー（オフライン含む） - 新しいAPIを使用
            GUIManager.getInstance().createPlayerNameSelectionGUI(player, 
                    ChatColor.BLUE + "プレイヤーを選択: " + arg.getName() + " (オフライン含む)",
                    true, // includeOffline = true
                    (selectedPlayerName) -> {
                        session.addArgument(selectedPlayerName);
                        showArgumentInputGUI(player, session, paginationCreator);
                    });
        }
    }
    
    /**
     * ワールド選択GUI
     */
    private void showWorldSelectionGUI(Player player, ArgumentInputSession session, 
                                     Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        ArgumentData arg = session.getCurrentArgument();
        List<GUIItemData> items = new ArrayList<>();
        
        int slot = 0;
        for (World world : Bukkit.getWorlds()) {
            // ループ変数を最終変数にキャプチャ
            final String worldName = world.getName();
            final World.Environment worldEnvironment = world.getEnvironment();
            
            Material material = switch (worldEnvironment) {
                case NORMAL -> Material.GRASS_BLOCK;
                case NETHER -> Material.NETHERRACK;
                case THE_END -> Material.END_STONE;
                default -> Material.BEDROCK;
            };
            
            ItemStack worldItem = GUIItemCreator.createItem(material, 
                    ChatColor.GREEN + worldName,
                    Arrays.asList(
                            ChatColor.GRAY + "環境: " + ChatColor.WHITE + worldEnvironment.name(),
                            ChatColor.GRAY + "引数: " + ChatColor.WHITE + arg.getName(),
                            ChatColor.GRAY + "説明: " + ChatColor.WHITE + arg.getDescription(),
                            "",
                            ChatColor.YELLOW + "クリックして選択"
                    ));
            
            items.add(new GUIItemData(worldItem, slot++, (guiItemData) -> {
                session.addArgument(worldName);
                showArgumentInputGUI(player, session, paginationCreator);
            }));
        }
        
        addNavigationItems(items, session, paginationCreator, player);
        
        paginationCreator.accept(new CommandGUIManager.CommandGUIData(
                ChatColor.BLUE + "ワールドを選択: " + arg.getName(), 
                items, 54, null, CommandGUIManager.CommandGUIType.SUBCOMMAND_MENU));
    }
    
    /**
     * アイテム選択GUI（APIを使用版）
     */
    private void showItemSelectionGUI(Player player, ArgumentInputSession session, 
                                    Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        ArgumentData arg = session.getCurrentArgument();
        
        // 引数タイプからMaterialCategoryに変換
        MaterialCategory category = convertArgumentTypeToCategory(arg.getType());
        
        // APIのマテリアル選択GUIを使用
        GUIManager.getInstance().createMaterialSelectionGUI(player, 
                ChatColor.BLUE + category.getDisplayName() + "を選択: " + arg.getName(),
                category,
                (selectedMaterial) -> {
                    session.addArgument(selectedMaterial.name());
                    showArgumentInputGUI(player, session, paginationCreator);
                });
    }
    
    /**
     * ArgumentTypeをMaterialCategoryに変換
     */
    private MaterialCategory convertArgumentTypeToCategory(ArgumentType type) {
        return switch (type) {
            case ITEM_ID_TOOL -> MaterialCategory.TOOLS;
            case ITEM_ID_BLOCK -> MaterialCategory.BLOCKS;
            case ITEM_ID_NATURE_BLOCK -> MaterialCategory.NATURE_BLOCKS;
            case ITEM_ID_WEAPON_ARMOR -> MaterialCategory.WEAPONS_ARMOR;
            case ITEM_ID_FOOD -> MaterialCategory.FOOD;
            case ITEM_ID_DECORATION -> MaterialCategory.DECORATION;
            default -> MaterialCategory.ALL;
        };
    }
    
    /**
     * アイテムタイプ別のマテリアル配列を取得（パブリックメソッド）
     */
    public Material[] getItemsByType(ArgumentType type) {
        return switch (type) {
            case ITEM_ID_TOOL -> new Material[]{
                // ツール類 - 全ツール
                Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE, Material.DIAMOND_SWORD,
                Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SHOVEL, Material.IRON_HOE, Material.IRON_SWORD,
                Material.GOLDEN_PICKAXE, Material.GOLDEN_AXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_HOE, Material.GOLDEN_SWORD,
                Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_SHOVEL, Material.STONE_HOE, Material.STONE_SWORD,
                Material.WOODEN_PICKAXE, Material.WOODEN_AXE, Material.WOODEN_SHOVEL, Material.WOODEN_HOE, Material.WOODEN_SWORD,
                Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE, Material.NETHERITE_SWORD,
                Material.SHEARS, Material.FLINT_AND_STEEL, Material.FISHING_ROD, Material.COMPASS, Material.CLOCK,
                Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.SHIELD, Material.CARROT_ON_A_STICK,
                Material.WARPED_FUNGUS_ON_A_STICK, Material.LEAD, Material.NAME_TAG, Material.SPYGLASS,
                Material.BRUSH, Material.RECOVERY_COMPASS
            };
            
            case ITEM_ID_BLOCK -> new Material[]{
                // ブロック系 - 全ブロック
                // 石系
                Material.STONE, Material.COBBLESTONE, Material.GRANITE, Material.DIORITE, Material.ANDESITE,
                Material.POLISHED_GRANITE, Material.POLISHED_DIORITE, Material.POLISHED_ANDESITE,
                Material.DEEPSLATE, Material.COBBLED_DEEPSLATE, Material.POLISHED_DEEPSLATE, Material.DEEPSLATE_BRICKS,
                Material.CRACKED_DEEPSLATE_BRICKS, Material.DEEPSLATE_TILES, Material.CRACKED_DEEPSLATE_TILES,
                Material.TUFF, Material.CALCITE, Material.SMOOTH_BASALT, Material.BASALT, Material.POLISHED_BASALT,
                // ネザー系
                Material.NETHERRACK, Material.SOUL_SAND, Material.SOUL_SOIL, Material.BLACKSTONE, Material.POLISHED_BLACKSTONE,
                Material.GILDED_BLACKSTONE,
                Material.NETHER_BRICKS, Material.CRACKED_NETHER_BRICKS, Material.CHISELED_NETHER_BRICKS, Material.RED_NETHER_BRICKS,
                Material.NETHER_WART_BLOCK, Material.WARPED_WART_BLOCK, Material.SHROOMLIGHT,
                // エンド系
                Material.END_STONE, Material.END_STONE_BRICKS, Material.PURPUR_BLOCK, Material.PURPUR_PILLAR,
                Material.CHORUS_PLANT, Material.CHORUS_FLOWER,
                // 砂岩系
                Material.SANDSTONE, Material.CHISELED_SANDSTONE, Material.CUT_SANDSTONE, Material.SMOOTH_SANDSTONE,
                Material.RED_SANDSTONE, Material.CHISELED_RED_SANDSTONE, Material.CUT_RED_SANDSTONE, Material.SMOOTH_RED_SANDSTONE,
                // クォーツ系
                Material.QUARTZ_BLOCK, Material.CHISELED_QUARTZ_BLOCK, Material.QUARTZ_PILLAR, Material.SMOOTH_QUARTZ,
                Material.QUARTZ_BRICKS,
                // プリズマリン系
                Material.PRISMARINE, Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE,
                // レンガ系
                Material.BRICKS, Material.MOSSY_COBBLESTONE, Material.MOSSY_STONE_BRICKS, Material.INFESTED_STONE,
                Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, Material.CHISELED_STONE_BRICKS,
                // その他
                Material.OBSIDIAN, Material.CRYING_OBSIDIAN, Material.BEDROCK, Material.BARRIER, Material.STRUCTURE_VOID,
                Material.MAGMA_BLOCK, Material.BONE_BLOCK, Material.DRIED_KELP_BLOCK, Material.HAY_BLOCK,
                Material.TARGET, Material.LODESTONE, Material.RESPAWN_ANCHOR, Material.ANCIENT_DEBRIS,
                // 液体ブロック（バケツとして表示）
                Material.WATER_BUCKET, Material.LAVA_BUCKET
            };
            
            case ITEM_ID_NATURE_BLOCK -> new Material[]{
                // 自然ブロック - 全自然ブロック
                // 土系
                Material.DIRT, Material.COARSE_DIRT, Material.PODZOL, Material.MYCELIUM, Material.GRASS_BLOCK,
                Material.DIRT_PATH, Material.FARMLAND, Material.ROOTED_DIRT, Material.MUD, Material.MUDDY_MANGROVE_ROOTS,
                // 砂・砂利系
                Material.SAND, Material.RED_SAND, Material.GRAVEL, Material.SUSPICIOUS_SAND, Material.SUSPICIOUS_GRAVEL,
                // 粘土・テラコッタ系
                Material.CLAY, Material.TERRACOTTA, Material.WHITE_TERRACOTTA, Material.ORANGE_TERRACOTTA,
                Material.MAGENTA_TERRACOTTA, Material.LIGHT_BLUE_TERRACOTTA, Material.YELLOW_TERRACOTTA,
                Material.LIME_TERRACOTTA, Material.PINK_TERRACOTTA, Material.GRAY_TERRACOTTA,
                Material.LIGHT_GRAY_TERRACOTTA, Material.CYAN_TERRACOTTA, Material.PURPLE_TERRACOTTA,
                Material.BLUE_TERRACOTTA, Material.BROWN_TERRACOTTA, Material.GREEN_TERRACOTTA,
                Material.RED_TERRACOTTA, Material.BLACK_TERRACOTTA,
                // 氷系
                Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE, Material.SNOW_BLOCK, Material.POWDER_SNOW,
                // 原木系
                Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.JUNGLE_LOG,
                Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG, Material.CHERRY_LOG,
                Material.STRIPPED_OAK_LOG, Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_SPRUCE_LOG,
                Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
                Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG,
                // 木材系
                Material.OAK_WOOD, Material.BIRCH_WOOD, Material.SPRUCE_WOOD, Material.JUNGLE_WOOD,
                Material.ACACIA_WOOD, Material.DARK_OAK_WOOD, Material.MANGROVE_WOOD, Material.CHERRY_WOOD,
                Material.STRIPPED_OAK_WOOD, Material.STRIPPED_BIRCH_WOOD, Material.STRIPPED_SPRUCE_WOOD,
                Material.STRIPPED_JUNGLE_WOOD, Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_DARK_OAK_WOOD,
                Material.STRIPPED_MANGROVE_WOOD, Material.STRIPPED_CHERRY_WOOD,
                // 葉系
                Material.OAK_LEAVES, Material.BIRCH_LEAVES, Material.SPRUCE_LEAVES, Material.JUNGLE_LEAVES,
                Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES,
                Material.AZALEA_LEAVES, Material.FLOWERING_AZALEA_LEAVES,
                // 鉱石系
                Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
                Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
                Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
                Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
                Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
                // その他自然ブロック
                Material.WATER, Material.LAVA, Material.SEAGRASS, Material.TALL_SEAGRASS, Material.KELP, Material.KELP_PLANT,
                Material.SPONGE, Material.WET_SPONGE,
                Material.TUBE_CORAL_BLOCK, Material.BRAIN_CORAL_BLOCK, Material.BUBBLE_CORAL_BLOCK,
                Material.FIRE_CORAL_BLOCK, Material.HORN_CORAL_BLOCK, Material.DEAD_TUBE_CORAL_BLOCK,
                Material.DEAD_BRAIN_CORAL_BLOCK, Material.DEAD_BUBBLE_CORAL_BLOCK, Material.DEAD_FIRE_CORAL_BLOCK,
                Material.DEAD_HORN_CORAL_BLOCK
            };
            
            case ITEM_ID_WEAPON_ARMOR -> new Material[]{
                // 武器・防具 - 全武器防具
                // 剣類
                Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.STONE_SWORD, 
                Material.WOODEN_SWORD, Material.NETHERITE_SWORD,
                // 遠距離武器
                Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.SHIELD,
                // ダイヤモンド防具
                Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
                // 鉄防具
                Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
                // 金防具
                Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
                // 革防具
                Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
                // ネザライト防具
                Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
                // チェーンメイル防具
                Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
                // タートルヘルメット
                Material.TURTLE_HELMET,
                // エリトラ
                Material.ELYTRA,
                // 矢
                Material.ARROW, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW
            };
            
            case ITEM_ID_FOOD -> new Material[]{
                // 食べ物 - 全食べ物
                // 果物・野菜系
                Material.APPLE, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.MELON_SLICE,
                Material.CARROT, Material.GOLDEN_CARROT, Material.POTATO, Material.BAKED_POTATO, Material.POISONOUS_POTATO,
                Material.BEETROOT, Material.SWEET_BERRIES, Material.GLOW_BERRIES, Material.PUMPKIN_PIE,
                // 穀物系
                Material.BREAD, Material.WHEAT, Material.COOKIE, Material.CAKE,
                // 肉系
                Material.BEEF, Material.COOKED_BEEF, Material.PORKCHOP, Material.COOKED_PORKCHOP,
                Material.CHICKEN, Material.COOKED_CHICKEN, Material.MUTTON, Material.COOKED_MUTTON,
                Material.RABBIT, Material.COOKED_RABBIT, Material.RABBIT_STEW,
                // 魚系
                Material.COD, Material.COOKED_COD, Material.SALMON, Material.COOKED_SALMON,
                Material.TROPICAL_FISH, Material.PUFFERFISH, Material.DRIED_KELP,
                // スープ・シチュー系
                Material.MUSHROOM_STEW, Material.BEETROOT_SOUP, Material.SUSPICIOUS_STEW,
                // 飲み物・その他
                Material.MILK_BUCKET, Material.HONEY_BOTTLE, Material.CHORUS_FRUIT, Material.SPIDER_EYE,
                Material.FERMENTED_SPIDER_EYE, Material.ROTTEN_FLESH
            };
            
            case ITEM_ID_DECORATION -> new Material[]{
                // 装飾ブロック - 全装飾ブロック
                // ウール系
                Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
                Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL,
                Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
                Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL,
                // コンクリート系
                Material.WHITE_CONCRETE, Material.ORANGE_CONCRETE, Material.MAGENTA_CONCRETE, Material.LIGHT_BLUE_CONCRETE,
                Material.YELLOW_CONCRETE, Material.LIME_CONCRETE, Material.PINK_CONCRETE, Material.GRAY_CONCRETE,
                Material.LIGHT_GRAY_CONCRETE, Material.CYAN_CONCRETE, Material.PURPLE_CONCRETE, Material.BLUE_CONCRETE,
                Material.BROWN_CONCRETE, Material.GREEN_CONCRETE, Material.RED_CONCRETE, Material.BLACK_CONCRETE,
                // ガラス系
                Material.GLASS, Material.TINTED_GLASS, Material.WHITE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS,
                Material.MAGENTA_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS,
                Material.LIME_STAINED_GLASS, Material.PINK_STAINED_GLASS, Material.GRAY_STAINED_GLASS,
                Material.LIGHT_GRAY_STAINED_GLASS, Material.CYAN_STAINED_GLASS, Material.PURPLE_STAINED_GLASS,
                Material.BLUE_STAINED_GLASS, Material.BROWN_STAINED_GLASS, Material.GREEN_STAINED_GLASS,
                Material.RED_STAINED_GLASS, Material.BLACK_STAINED_GLASS,
                // カーペット系
                Material.WHITE_CARPET, Material.ORANGE_CARPET, Material.MAGENTA_CARPET, Material.LIGHT_BLUE_CARPET,
                Material.YELLOW_CARPET, Material.LIME_CARPET, Material.PINK_CARPET, Material.GRAY_CARPET,
                Material.LIGHT_GRAY_CARPET, Material.CYAN_CARPET, Material.PURPLE_CARPET, Material.BLUE_CARPET,
                Material.BROWN_CARPET, Material.GREEN_CARPET, Material.RED_CARPET, Material.BLACK_CARPET,
                // 照明系
                Material.TORCH, Material.SOUL_TORCH, Material.REDSTONE_TORCH, Material.LANTERN, Material.SOUL_LANTERN,
                Material.GLOWSTONE, Material.SEA_LANTERN, Material.REDSTONE_LAMP, Material.BEACON, Material.CONDUIT,
                Material.SHROOMLIGHT, Material.CRYING_OBSIDIAN, Material.RESPAWN_ANCHOR,
                // 花・植物系
                Material.POPPY, Material.DANDELION, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET,
                Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
                Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE,
                Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY,
                // その他装飾
                Material.PAINTING, Material.ITEM_FRAME, Material.GLOW_ITEM_FRAME, Material.FLOWER_POT,
                Material.ARMOR_STAND, Material.END_ROD, Material.LIGHTNING_ROD, Material.CHAIN,
                Material.CANDLE, Material.WHITE_CANDLE, Material.ORANGE_CANDLE, Material.MAGENTA_CANDLE,
                Material.LIGHT_BLUE_CANDLE, Material.YELLOW_CANDLE, Material.LIME_CANDLE, Material.PINK_CANDLE,
                Material.GRAY_CANDLE, Material.LIGHT_GRAY_CANDLE, Material.CYAN_CANDLE, Material.PURPLE_CANDLE,
                Material.BLUE_CANDLE, Material.BROWN_CANDLE, Material.GREEN_CANDLE, Material.RED_CANDLE, Material.BLACK_CANDLE
            };
            
            default -> Material.values(); // 全てのMaterialを返す
        };
    }
    
    /**
     * ブール値選択GUI
     */
    private void showBooleanSelectionGUI(Player player, ArgumentInputSession session, 
                                       Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        ArgumentData arg = session.getCurrentArgument();
        List<GUIItemData> items = new ArrayList<>();
        
        // True選択
        ItemStack trueItem = GUIItemCreator.createItem(Material.LIME_WOOL, 
                ChatColor.GREEN + "True",
                Arrays.asList(
                        ChatColor.GRAY + "引数: " + ChatColor.WHITE + arg.getName(),
                        ChatColor.GRAY + "説明: " + ChatColor.WHITE + arg.getDescription(),
                        "",
                        ChatColor.YELLOW + "クリックして選択"
                ));
        items.add(new GUIItemData(trueItem, 11, (guiItemData) -> {
            session.addArgument("true");
            showArgumentInputGUI(player, session, paginationCreator);
        }));
        
        // False選択
        ItemStack falseItem = GUIItemCreator.createItem(Material.RED_WOOL, 
                ChatColor.RED + "False",
                Arrays.asList(
                        ChatColor.GRAY + "引数: " + ChatColor.WHITE + arg.getName(),
                        ChatColor.GRAY + "説明: " + ChatColor.WHITE + arg.getDescription(),
                        "",
                        ChatColor.YELLOW + "クリックして選択"
                ));
        items.add(new GUIItemData(falseItem, 15, (guiItemData) -> {
            session.addArgument("false");
            showArgumentInputGUI(player, session, paginationCreator);
        }));
        
        addNavigationItems(items, session, paginationCreator, player);
        
        paginationCreator.accept(new CommandGUIManager.CommandGUIData(
                ChatColor.BLUE + "真偽値を選択: " + arg.getName(), 
                items, 54, null, CommandGUIManager.CommandGUIType.SUBCOMMAND_MENU));
    }
    
    /**
     * エンチャント選択GUI
     */
    private void showEnchantmentSelectionGUI(Player player, ArgumentInputSession session, 
                                           Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        ArgumentData arg = session.getCurrentArgument();
        List<GUIItemData> items = new ArrayList<>();
        
        int slot = 0;
        // Enchantment.values()は非推奨のため、Registry APIを使用
        for (Enchantment enchantment : org.bukkit.Registry.ENCHANTMENT) {
            ItemStack enchantItem = GUIItemCreator.createItem(Material.ENCHANTED_BOOK, 
                    ChatColor.LIGHT_PURPLE + enchantment.getKey().getKey(),
                    Arrays.asList(
                            ChatColor.GRAY + "ID: " + ChatColor.WHITE + enchantment.getKey().getKey(),
                            ChatColor.GRAY + "引数: " + ChatColor.WHITE + arg.getName(),
                            ChatColor.GRAY + "説明: " + ChatColor.WHITE + arg.getDescription(),
                            "",
                            ChatColor.YELLOW + "クリックして選択"
                    ));
            
            items.add(new GUIItemData(enchantItem, slot++, (guiItemData) -> {
                session.addArgument(enchantment.getKey().getKey());
                showArgumentInputGUI(player, session, paginationCreator);
            }));
            
            if (slot >= 45) break;
        }
        
        addNavigationItems(items, session, paginationCreator, player);
        
        paginationCreator.accept(new CommandGUIManager.CommandGUIData(
                ChatColor.BLUE + "エンチャントを選択: " + arg.getName(), 
                items, 54, null, CommandGUIManager.CommandGUIType.SUBCOMMAND_MENU));
    }
    
    /**
     * ポーション効果選択GUI
     */
    private void showPotionEffectSelectionGUI(Player player, ArgumentInputSession session, 
                                            Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        ArgumentData arg = session.getCurrentArgument();
        List<GUIItemData> items = new ArrayList<>();
        
        int slot = 0;
        // PotionEffectType.values()は非推奨のため、Registry APIを使用
        for (PotionEffectType effectType : org.bukkit.Registry.EFFECT) {
            if (effectType != null) {
                ItemStack potionItem = GUIItemCreator.createItem(Material.POTION, 
                        ChatColor.AQUA + effectType.getKey().getKey(),
                        Arrays.asList(
                                ChatColor.GRAY + "ID: " + ChatColor.WHITE + effectType.getKey().getKey(),
                                ChatColor.GRAY + "引数: " + ChatColor.WHITE + arg.getName(),
                                ChatColor.GRAY + "説明: " + ChatColor.WHITE + arg.getDescription(),
                                "",
                                ChatColor.YELLOW + "クリックして選択"
                        ));
                
                items.add(new GUIItemData(potionItem, slot++, (guiItemData) -> {
                    session.addArgument(effectType.getKey().getKey());
                    showArgumentInputGUI(player, session, paginationCreator);
                }));
                
                if (slot >= 45) break;
            }
        }
        
        addNavigationItems(items, session, paginationCreator, player);
        
        paginationCreator.accept(new CommandGUIManager.CommandGUIData(
                ChatColor.BLUE + "ポーション効果を選択: " + arg.getName(), 
                items, 54, null, CommandGUIManager.CommandGUIType.SUBCOMMAND_MENU));
    }
    
    /**
     * 整数選択GUI（APIを使用）
     */
    private void showIntegerSelectionUsingGUIManager(Player player, ArgumentInputSession session, 
                                                   Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        ArgumentData arg = session.getCurrentArgument();
        
        // 範囲を決定
        int min = (int) (arg.getMin() != Double.NEGATIVE_INFINITY ? arg.getMin() : -1000);
        int max = (int) (arg.getMax() != Double.POSITIVE_INFINITY ? arg.getMax() : 1000);
        int defaultValue = Math.max(min, Math.min(max, 0));
        
        // APIの数値選択GUIを使用
        GUIManager.getInstance().createNumberSelectionGUI(player, 
                "整数を選択: " + arg.getName(), 
                min, max, defaultValue, 
                (selectedNumber) -> {
                    session.addArgument(String.valueOf(selectedNumber));
                    showArgumentInputGUI(player, session, paginationCreator);
                });
    }
    
    /**
     * 小数選択GUI（APIを使用）
     */
    private void showDoubleSelectionUsingGUIManager(Player player, ArgumentInputSession session, 
                                                  Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        ArgumentData arg = session.getCurrentArgument();
        
        // 小数の場合は整数として扱って後で小数に変換
        int min = (int) (arg.getMin() != Double.NEGATIVE_INFINITY ? arg.getMin() : -100);
        int max = (int) (arg.getMax() != Double.POSITIVE_INFINITY ? arg.getMax() : 100);
        int defaultValue = Math.max(min, Math.min(max, 0));
        
        // APIの数値選択GUIを使用
        GUIManager.getInstance().createNumberSelectionGUI(player, 
                "数値を選択: " + arg.getName() + " (小数入力も可能)", 
                min, max, defaultValue, 
                (selectedNumber) -> {
                    session.addArgument(String.valueOf(selectedNumber));
                    showArgumentInputGUI(player, session, paginationCreator);
                });
    }
    
    /**
     * 座標選択GUI
     */
    private void showCoordinateSelectionGUI(Player player, ArgumentInputSession session, 
                                          Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        ArgumentData arg = session.getCurrentArgument();
        List<GUIItemData> items = new ArrayList<>();
        
        // 座標用の候補を生成
        List<String> candidates = generateCoordinateCandidates(arg, player);
        
        int slot = 0;
        for (String value : candidates) {
            Material material = Material.COMPASS;
            if (value.startsWith("~")) {
                material = Material.ENDER_EYE;
            }
            
            ItemStack coordItem = GUIItemCreator.createItem(material, 
                    ChatColor.GREEN + value,
                    Arrays.asList(
                            ChatColor.GRAY + "引数: " + ChatColor.WHITE + arg.getName(),
                            ChatColor.GRAY + "タイプ: " + ChatColor.WHITE + arg.getType().getDisplayName(),
                            ChatColor.GRAY + "説明: " + ChatColor.WHITE + arg.getDescription(),
                            ChatColor.GRAY + "値: " + ChatColor.WHITE + value,
                            "",
                            ChatColor.YELLOW + "クリックして選択"
                    ));
            
            items.add(new GUIItemData(coordItem, slot++, (guiItemData) -> {
                session.addArgument(value);
                showArgumentInputGUI(player, session, paginationCreator);
            }));
            
            if (slot >= 45) break;
        }
        
        // カスタム入力ボタン
        if (slot < 45) {
            ItemStack customItem = GUIItemCreator.createItem(Material.WRITABLE_BOOK, 
                    ChatColor.YELLOW + "カスタム座標を入力",
                    Arrays.asList(
                            ChatColor.GRAY + "引数: " + ChatColor.WHITE + arg.getName(),
                            ChatColor.GRAY + "説明: " + ChatColor.WHITE + arg.getDescription(),
                            "",
                            ChatColor.AQUA + "チャットで座標を入力します",
                            ChatColor.GRAY + "相対座標（~）も使用できます"
                    ));
            
            items.add(new GUIItemData(customItem, slot, (guiItemData) -> showTextInputGUI(player, session, paginationCreator)));
        }
        
        addNavigationItems(items, session, paginationCreator, player);
        
        paginationCreator.accept(new CommandGUIManager.CommandGUIData(
                ChatColor.BLUE + arg.getType().getDisplayName() + "を選択: " + arg.getName(), 
                items, 54, null, CommandGUIManager.CommandGUIType.SUBCOMMAND_MENU));
    }
    
    /**
     * テキスト入力GUI（チャット入力促進）
     */
    private void showTextInputGUI(Player player, ArgumentInputSession session, 
                                Consumer<CommandGUIManager.CommandGUIData> paginationCreator) {
        ArgumentData arg = session.getCurrentArgument();
        List<GUIItemData> items = new ArrayList<>();
        
        // チャット入力説明アイテム
        ItemStack inputItem = GUIItemCreator.createItem(Material.WRITABLE_BOOK, 
                ChatColor.YELLOW + "チャットで入力",
                Arrays.asList(
                        ChatColor.GRAY + "引数: " + ChatColor.WHITE + arg.getName(),
                        ChatColor.GRAY + "タイプ: " + ChatColor.WHITE + arg.getType().getDisplayName(),
                        ChatColor.GRAY + "説明: " + ChatColor.WHITE + arg.getDescription(),
                        "",
                        ChatColor.AQUA + "GUIを閉じてチャットで値を入力してください",
                        ChatColor.GRAY + "入力例: " + getInputExample(arg.getType())
                ));
        items.add(new GUIItemData(inputItem, 22, (guiItemData) -> {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "=== 引数入力 ===");
            player.sendMessage(ChatColor.YELLOW + "引数: " + ChatColor.WHITE + arg.getName());
            player.sendMessage(ChatColor.YELLOW + "タイプ: " + ChatColor.WHITE + arg.getType().getDisplayName());
            player.sendMessage(ChatColor.YELLOW + "説明: " + ChatColor.WHITE + arg.getDescription());
            player.sendMessage(ChatColor.GRAY + "入力例: " + getInputExample(arg.getType()));
            player.sendMessage(ChatColor.AQUA + "チャットで値を入力してください（'cancel'でキャンセル）：");
            
            // チャットリスナーで入力を待機
            if (chatListener != null) {
                chatListener.startChatInput(player, session, paginationCreator, this);
            } else {
                // フォールバック：デフォルト値を使用
                String defaultValue = arg.isRequired() ? getInputExample(arg.getType()) : arg.getDefaultValue();
                session.addArgument(defaultValue);
                showArgumentInputGUI(player, session, paginationCreator);
            }
        }));
        
        addNavigationItems(items, session, paginationCreator, player);
        
        paginationCreator.accept(new CommandGUIManager.CommandGUIData(
                ChatColor.BLUE + arg.getType().getDisplayName() + "を入力: " + arg.getName(), 
                items, 54, null, CommandGUIManager.CommandGUIType.SUBCOMMAND_MENU));
    }
    
    /**
     * 各引数タイプの入力例を取得
     */
    private String getInputExample(ArgumentType type) {
        return switch (type) {
            case STRING -> "テキスト";
            case INTEGER -> "123";
            case DOUBLE -> "12.34";
            case COORDINATE_X, COORDINATE_Y, COORDINATE_Z -> "100";
            default -> "値";
        };
    }
    
    /**
     * ナビゲーションアイテム（戻る、キャンセル）を追加
     */
    private void addNavigationItems(List<GUIItemData> items, ArgumentInputSession session, 
                                  Consumer<CommandGUIManager.CommandGUIData> paginationCreator, Player player) {
        // 戻るボタン（前の引数に戻る）
        if (session.getCurrentArgumentIndex() > 0) {
            ItemStack backItem = GUIItemCreator.createItem(Material.ARROW, 
                    ChatColor.YELLOW + "前の引数に戻る",
                    List.of(ChatColor.GRAY + "前の引数の入力に戻ります"));
            items.add(new GUIItemData(backItem, 45, (guiItemData) -> {
                // 前の引数を削除して戻る
                if (!session.getCollectedArguments().isEmpty()) {
                    session.getCollectedArguments().remove(session.getCollectedArguments().size() - 1);
                    session.currentArgumentIndex--;
                    showArgumentInputGUI(player, session, paginationCreator);
                }
            }));
        }
        
        // キャンセルボタン
        ItemStack cancelItem = GUIItemCreator.createItem(Material.BARRIER, 
                ChatColor.RED + "キャンセル",
                List.of(ChatColor.GRAY + "引数入力をキャンセルします"));
        items.add(new GUIItemData(cancelItem, 53, (guiItemData) -> {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "コマンド実行をキャンセルしました。");
        }));
    }
    
    /**
     * 座標候補を生成
     */
    private List<String> generateCoordinateCandidates(ArgumentData arg, Player player) {
        List<String> candidates = new ArrayList<>();
        
        // プレイヤーの現在位置を基準にした候補
        Location loc = player.getLocation();
        
        switch (arg.getType()) {
            case COORDINATE_X:
                candidates.add(String.valueOf((int) loc.getX()));
                candidates.add("~");
                candidates.add("~10");
                candidates.add("~-10");
                candidates.add("0");
                break;
            case COORDINATE_Y:
                candidates.add(String.valueOf((int) loc.getY()));
                candidates.add("~");
                candidates.add("~10");
                candidates.add("~-10");
                candidates.add("64"); // 海面レベル
                candidates.add("256"); // 建築限界
                break;
            case COORDINATE_Z:
                candidates.add(String.valueOf((int) loc.getZ()));
                candidates.add("~");
                candidates.add("~10");
                candidates.add("~-10");
                candidates.add("0");
                break;
            default:
                // 座標以外のタイプの場合は何もしない
                break;
        }
        
        return candidates;
    }
}
