package com.Tempce.tempceLib.command.helper;

import com.Tempce.tempceLib.command.data.ArgumentData;
import com.Tempce.tempceLib.command.data.ArgumentType;
import com.Tempce.tempceLib.command.data.SubCommandData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * コマンド引数の自動バリデーションとヘルプメッセージ生成ユーティリティ
 */
public class ArgumentValidator {
    
    /**
     * 引数を自動バリデーションし、エラーがあればメッセージを送信
     * @param sender コマンド送信者
     * @param args 引数配列
     * @param subCommandData サブコマンドデータ
     * @return バリデーション成功時true、失敗時false
     */
    public static boolean validateAndSendErrors(CommandSender sender, String[] args, SubCommandData subCommandData) {
        List<ArgumentData> arguments = subCommandData.getArguments();
        
        if (arguments == null || arguments.isEmpty()) {
            return true; // 引数定義がない場合は成功
        }
        
        // 必須引数の数をチェック
        long requiredCount = arguments.stream().filter(ArgumentData::isRequired).count();
        if (args.length < requiredCount) {
            sendUsageMessage(sender, subCommandData);
            return false;
        }
        
        // 各引数のバリデーション
        for (int i = 0; i < Math.min(args.length, arguments.size()); i++) {
            ArgumentData argDef = arguments.get(i);
            String value = args[i];
            
            String errorMessage = validateArgument(value, argDef, i + 1);
            if (errorMessage != null) {
                sender.sendMessage(ChatColor.RED + errorMessage);
                sendUsageMessage(sender, subCommandData);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 単一引数のバリデーション
     * @param value 引数値
     * @param argDef 引数定義
     * @param position 引数位置（1から始まる）
     * @return エラーメッセージ（成功時はnull）
     */
    public static String validateArgument(String value, ArgumentData argDef, int position) {
        if (value == null || value.trim().isEmpty()) {
            if (argDef.isRequired()) {
                return "引数 " + position + " (" + argDef.getName() + ") は必須です。";
            }
            return null;
        }
        
        switch (argDef.getType()) {
            case INTEGER:
                try {
                    int intValue = Integer.parseInt(value);
                    if (argDef.getMin() != Double.MIN_VALUE && intValue < argDef.getMin()) {
                        return "引数 " + position + " (" + argDef.getName() + ") は " + (int)argDef.getMin() + " 以上である必要があります。";
                    }
                    if (argDef.getMax() != Double.MAX_VALUE && intValue > argDef.getMax()) {
                        return "引数 " + position + " (" + argDef.getName() + ") は " + (int)argDef.getMax() + " 以下である必要があります。";
                    }
                } catch (NumberFormatException e) {
                    return "引数 " + position + " (" + argDef.getName() + ") は整数である必要があります。";
                }
                break;
                
            case DOUBLE:
            case COORDINATE_X:
            case COORDINATE_Y:
            case COORDINATE_Z:
                try {
                    double doubleValue = Double.parseDouble(value);
                    if (argDef.getMin() != Double.MIN_VALUE && doubleValue < argDef.getMin()) {
                        return "引数 " + position + " (" + argDef.getName() + ") は " + argDef.getMin() + " 以上である必要があります。";
                    }
                    if (argDef.getMax() != Double.MAX_VALUE && doubleValue > argDef.getMax()) {
                        return "引数 " + position + " (" + argDef.getName() + ") は " + argDef.getMax() + " 以下である必要があります。";
                    }
                } catch (NumberFormatException e) {
                    String typeName = argDef.getType() == ArgumentType.DOUBLE ? "数値" : "座標値";
                    return "引数 " + position + " (" + argDef.getName() + ") は" + typeName + "である必要があります。";
                }
                break;
                
            case BOOLEAN:
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false") &&
                    !value.equalsIgnoreCase("on") && !value.equalsIgnoreCase("off") &&
                    !value.equalsIgnoreCase("yes") && !value.equalsIgnoreCase("no")) {
                    return "引数 " + position + " (" + argDef.getName() + ") は true/false、on/off、yes/no のいずれかである必要があります。";
                }
                break;
                
            case ONLINE_PLAYER:
            case ALL_PLAYER:
            case ITEM_ID:
            case ENTITY_ID:
            case WORLD:
            case ENCHANTMENT:
            case POTION_EFFECT:
            case STRING:
            default:
                // 文字列系の引数は基本的にバリデーションしない
                // 必要に応じて個別にバリデーションロジックを追加
                break;
        }
        
        return null; // バリデーション成功
    }
    
    /**
     * 使用方法メッセージを自動生成して送信
     * @param sender コマンド送信者
     * @param subCommandData サブコマンドデータ
     */
    public static void sendUsageMessage(CommandSender sender, SubCommandData subCommandData) {
        String usage = generateUsageString(subCommandData);
        sender.sendMessage(ChatColor.RED + "使用方法: " + usage);
        
        // 引数の詳細説明も表示
        if (subCommandData.hasArguments()) {
            sender.sendMessage(ChatColor.YELLOW + "引数の説明:");
            for (int i = 0; i < subCommandData.getArguments().size(); i++) {
                ArgumentData arg = subCommandData.getArguments().get(i);
                String prefix = arg.isRequired() ? ChatColor.RED + "必須" : ChatColor.GREEN + "任意";
                StringBuilder argInfo = new StringBuilder();
                argInfo.append(ChatColor.GRAY).append("  ").append(i + 1).append(". ")
                       .append(prefix).append(ChatColor.WHITE).append(" ")
                       .append(arg.getName()).append(" (").append(arg.getType().getDisplayName()).append(")");
                
                if (!arg.getDescription().isEmpty()) {
                    argInfo.append(ChatColor.GRAY).append(" - ").append(arg.getDescription());
                }
                
                if (arg.isNumeric() && (arg.getMin() != Double.MIN_VALUE || arg.getMax() != Double.MAX_VALUE)) {
                    argInfo.append(ChatColor.GRAY).append(" [");
                    if (arg.getMin() != Double.MIN_VALUE) {
                        argInfo.append("最小: ").append(formatNumber(arg.getMin()));
                    }
                    if (arg.getMax() != Double.MAX_VALUE) {
                        if (arg.getMin() != Double.MIN_VALUE) argInfo.append(", ");
                        argInfo.append("最大: ").append(formatNumber(arg.getMax()));
                    }
                    argInfo.append("]");
                }
                
                if (!arg.isRequired() && !arg.getDefaultValue().isEmpty()) {
                    argInfo.append(ChatColor.GRAY).append(" (デフォルト: ").append(arg.getDefaultValue()).append(")");
                }
                
                sender.sendMessage(argInfo.toString());
            }
        }
    }
    
    /**
     * 使用方法文字列を自動生成
     * @param subCommandData サブコマンドデータ
     * @return 使用方法文字列
     */
    public static String generateUsageString(SubCommandData subCommandData) {
        StringBuilder usage = new StringBuilder();
        
        // コマンド名の取得（仮定：親コマンド名は外部から取得）
        // この部分は実際の実装では親コマンド名を渡すか、SubCommandDataに含める必要があります
        usage.append("/").append(getCommandName(subCommandData));
        
        // サブコマンドパス
        if (!subCommandData.getPath().isEmpty()) {
            usage.append(" ").append(subCommandData.getPath().replace(".", " "));
        }
        
        // 引数
        if (subCommandData.hasArguments()) {
            for (ArgumentData arg : subCommandData.getArguments()) {
                usage.append(" ");
                if (arg.isRequired()) {
                    usage.append("<").append(arg.getName()).append(">");
                } else {
                    usage.append("[").append(arg.getName()).append("]");
                }
            }
        }
        
        return usage.toString();
    }
    
    /**
     * 数値を適切にフォーマット
     */
    private static String formatNumber(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        } else {
            return String.valueOf(value);
        }
    }
    
    /**
     * コマンド名を取得
     */
    private static String getCommandName(SubCommandData subCommandData) {
        return subCommandData.getParentCommandName() != null ? 
               subCommandData.getParentCommandName() : "command";
    }
}
