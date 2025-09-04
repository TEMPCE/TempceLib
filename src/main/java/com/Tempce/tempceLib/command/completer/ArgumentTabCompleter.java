package com.Tempce.tempceLib.command.completer;

import com.Tempce.tempceLib.command.data.ArgumentData;
import com.Tempce.tempceLib.command.data.ArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 引数タイプ別のTAB補完候補を生成するユーティリティクラス
 */
public class ArgumentTabCompleter {
    
    /**
     * 引数定義に基づいてTAB補完候補を生成
     * @param argument 引数定義
     * @param currentInput 現在の入力
     * @param sender コマンド送信者
     * @return 補完候補リスト
     */
    public static List<String> getCompletions(ArgumentData argument, String currentInput, CommandSender sender) {
        List<String> completions = new ArrayList<>();
        
        switch (argument.getType()) {
            case ONLINE_PLAYER:
                completions.addAll(getOnlinePlayerCompletions(currentInput));
                break;
                
            case ALL_PLAYER:
                completions.addAll(getAllPlayerCompletions(currentInput));
                break;
                
            case WORLD:
                completions.addAll(getWorldCompletions(currentInput));
                break;
                
            case ITEM_ID:
                completions.addAll(getItemCompletionsByType(ArgumentType.ITEM_ID, currentInput));
                break;
                
            case ITEM_ID_TOOL:
                completions.addAll(getItemCompletionsByType(ArgumentType.ITEM_ID_TOOL, currentInput));
                break;
                
            case ITEM_ID_BLOCK:
                completions.addAll(getItemCompletionsByType(ArgumentType.ITEM_ID_BLOCK, currentInput));
                break;
                
            case ITEM_ID_NATURE_BLOCK:
                completions.addAll(getItemCompletionsByType(ArgumentType.ITEM_ID_NATURE_BLOCK, currentInput));
                break;
                
            case ITEM_ID_WEAPON_ARMOR:
                completions.addAll(getItemCompletionsByType(ArgumentType.ITEM_ID_WEAPON_ARMOR, currentInput));
                break;
                
            case ITEM_ID_FOOD:
                completions.addAll(getItemCompletionsByType(ArgumentType.ITEM_ID_FOOD, currentInput));
                break;
                
            case ITEM_ID_DECORATION:
                completions.addAll(getItemCompletionsByType(ArgumentType.ITEM_ID_DECORATION, currentInput));
                break;
                
            case ENCHANTMENT:
                completions.addAll(getEnchantmentCompletions(currentInput));
                break;
                
            case POTION_EFFECT:
                completions.addAll(getPotionEffectCompletions(currentInput));
                break;
                
            case BOOLEAN:
                completions.addAll(getBooleanCompletions(currentInput));
                break;
                
            case INTEGER:
                completions.addAll(getIntegerCompletions(argument, currentInput));
                break;
                
            case DOUBLE:
            case COORDINATE_X:
            case COORDINATE_Y:
            case COORDINATE_Z:
                completions.addAll(getDoubleCompletions(argument, currentInput));
                break;
                
            case STRING:
                // カスタム候補があれば使用
                if (!argument.getSuggestions().isEmpty()) {
                    completions.addAll(filterSuggestions(argument.getSuggestions(), currentInput));
                }
                break;
                
            case ENTITY_ID:
                // エンティティタイプの候補（実装は省略）
                break;
        }
        
        return completions;
    }
    
    /**
     * オンラインプレイヤー名の補完候補
     */
    private static List<String> getOnlinePlayerCompletions(String input) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * 全プレイヤー名の補完候補（オフライン含む）
     */
    private static List<String> getAllPlayerCompletions(String input) {
        List<String> completions = new ArrayList<>();
        
        // オンラインプレイヤーを最初に追加
        completions.addAll(getOnlinePlayerCompletions(input));
        
        // オフラインプレイヤーを追加（重複除外）
        for (org.bukkit.OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            String name = offlinePlayer.getName();
            if (name != null && name.toLowerCase().startsWith(input.toLowerCase()) 
                && !completions.contains(name)) {
                completions.add(name);
            }
        }
        
        return completions;
    }
    
    /**
     * ワールド名の補完候補
     */
    private static List<String> getWorldCompletions(String input) {
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * エンチャントの補完候補
     */
    private static List<String> getEnchantmentCompletions(String input) {
        List<String> enchantments = new ArrayList<>();
        
        // Enchantment.values()は非推奨のため、Registry APIを使用
        for (Enchantment enchantment : org.bukkit.Registry.ENCHANTMENT) {
            String key = enchantment.getKey().getKey();
            if (key.toLowerCase().startsWith(input.toLowerCase())) {
                enchantments.add(key);
            }
        }
        
        return enchantments;
    }
    
    /**
     * ポーション効果の補完候補
     */
    private static List<String> getPotionEffectCompletions(String input) {
        List<String> effects = new ArrayList<>();
        
        // PotionEffectType.values()は非推奨のため、Registry APIを使用
        for (PotionEffectType effectType : org.bukkit.Registry.EFFECT) {
            if (effectType != null) {
                String name = effectType.getKey().getKey().toLowerCase();
                if (name.startsWith(input.toLowerCase())) {
                    effects.add(name);
                }
            }
        }
        
        return effects;
    }
    
    /**
     * ブール値の補完候補
     */
    private static List<String> getBooleanCompletions(String input) {
        List<String> booleans = Arrays.asList("true", "false", "on", "off", "yes", "no");
        return booleans.stream()
                .filter(bool -> bool.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * 整数の補完候補
     */
    private static List<String> getIntegerCompletions(ArgumentData argument, String input) {
        List<String> numbers = new ArrayList<>();
        
        if (input.isEmpty()) {
            // 入力が空の場合、範囲に基づいて候補を提案
            if (argument.getMin() != Double.MIN_VALUE) {
                numbers.add(String.valueOf((int)argument.getMin()));
            }
            if (argument.getMax() != Double.MAX_VALUE) {
                numbers.add(String.valueOf((int)argument.getMax()));
            }
            if (!argument.getDefaultValue().isEmpty()) {
                numbers.add(argument.getDefaultValue());
            }
            
            // 一般的な値も追加
            if (argument.getMin() <= 1 && argument.getMax() >= 1) numbers.add("1");
            if (argument.getMin() <= 10 && argument.getMax() >= 10) numbers.add("10");
            if (argument.getMin() <= 64 && argument.getMax() >= 64) numbers.add("64");
        } else {
            try {
                int inputNum = Integer.parseInt(input);
                // 入力が有効な数値の場合、そのまま候補に
                if ((argument.getMin() == Double.MIN_VALUE || inputNum >= argument.getMin()) &&
                    (argument.getMax() == Double.MAX_VALUE || inputNum <= argument.getMax())) {
                    numbers.add(input);
                }
            } catch (NumberFormatException e) {
                // 数値でない場合は候補なし
            }
        }
        
        return numbers;
    }
    
    /**
     * 小数・座標の補完候補
     */
    private static List<String> getDoubleCompletions(ArgumentData argument, String input) {
        List<String> numbers = new ArrayList<>();
        
        if (input.isEmpty()) {
            // 座標の場合は特別な候補を提案
            if (argument.getType() == ArgumentType.COORDINATE_Y) {
                numbers.addAll(Arrays.asList("64", "100", "200", "300"));
            } else if (argument.getType() == ArgumentType.COORDINATE_X || 
                      argument.getType() == ArgumentType.COORDINATE_Z) {
                numbers.addAll(Arrays.asList("0", "100", "1000", "~"));
            } else {
                // 一般的な小数値
                numbers.addAll(Arrays.asList("0.0", "1.0", "10.0"));
            }
            
            if (!argument.getDefaultValue().isEmpty()) {
                numbers.add(argument.getDefaultValue());
            }
        } else {
            try {
                double inputNum = Double.parseDouble(input);
                // 入力が有効な数値の場合、そのまま候補に
                if ((argument.getMin() == Double.MIN_VALUE || inputNum >= argument.getMin()) &&
                    (argument.getMax() == Double.MAX_VALUE || inputNum <= argument.getMax())) {
                    numbers.add(input);
                }
            } catch (NumberFormatException e) {
                // 座標の場合は相対座標の候補も
                if (argument.getType() == ArgumentType.COORDINATE_X || 
                    argument.getType() == ArgumentType.COORDINATE_Y || 
                    argument.getType() == ArgumentType.COORDINATE_Z) {
                    if (input.startsWith("~")) {
                        numbers.add(input);
                    } else if ("~".startsWith(input)) {
                        numbers.add("~");
                    }
                }
            }
        }
        
        return numbers;
    }
    
    /**
     * カスタム候補をフィルタリング
     */
    private static List<String> filterSuggestions(List<String> suggestions, String input) {
        return suggestions.stream()
                .filter(suggestion -> suggestion.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * アイテムタイプ別の補完候補
     */
    private static List<String> getItemCompletionsByType(ArgumentType type, String input) {
        Material[] materials = getItemsByType(type);
        List<String> items = new ArrayList<>();
        
        for (Material material : materials) {
            String name;
            // ブロック系でバケツアイテムの場合は液体ブロック名を返す
            if (type == ArgumentType.ITEM_ID_BLOCK && material == Material.WATER_BUCKET) {
                name = "water";
            } else if (type == ArgumentType.ITEM_ID_BLOCK && material == Material.LAVA_BUCKET) {
                name = "lava";
            } else {
                name = material.name().toLowerCase();
            }
            
            if (name.startsWith(input.toLowerCase())) {
                items.add(name);
                if (items.size() >= 20) break; // 候補数制限
            }
        }
        
        return items;
    }
    
    /**
     * アイテムタイプ別のマテリアル配列を取得
     * ArgumentInputGUIManagerと同じロジックを使用
     */
    private static Material[] getItemsByType(ArgumentType type) {
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
            
            default -> {
                // Material.values()が非推奨の場合に備えて、全てのMaterialを配列で返す
                yield Material.values(); // 全てのMaterialを返す
            }
        };
    }
}
