package com.Tempce.tempceLib.command.manager;

import com.Tempce.tempceLib.command.annotations.Command;
import com.Tempce.tempceLib.command.annotations.SubCommand;
import com.Tempce.tempceLib.command.data.CommandData;
import com.Tempce.tempceLib.command.data.SubCommandData;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * コマンドシステムの管理クラス
 */
public class CommandManager {
    
    private final Map<String, CommandData> commands = new ConcurrentHashMap<>();
    private final Map<String, String> aliases = new ConcurrentHashMap<>();
    
    /**
     * コマンドクラスを登録する
     * @param commandClass 登録するコマンドクラス
     */
    public void registerCommand(Class<?> commandClass) {
        if (!commandClass.isAnnotationPresent(Command.class)) {
            throw new IllegalArgumentException("クラス " + commandClass.getSimpleName() + " に @Command アノテーションが付与されていません");
        }
        
        try {
            Object instance = commandClass.getDeclaredConstructor().newInstance();
            registerCommand(instance);
        } catch (Exception e) {
            throw new RuntimeException("コマンドクラス " + commandClass.getSimpleName() + " のインスタンス化に失敗しました", e);
        }
    }
    
    /**
     * コマンドインスタンスを登録する
     * @param commandInstance 登録するコマンドインスタンス
     */
    public void registerCommand(Object commandInstance) {
        Class<?> commandClass = commandInstance.getClass();
        
        if (!commandClass.isAnnotationPresent(Command.class)) {
            throw new IllegalArgumentException("クラス " + commandClass.getSimpleName() + " に @Command アノテーションが付与されていません");
        }
        
        Command commandAnnotation = commandClass.getAnnotation(Command.class);
        
        // コマンド名の決定
        String commandName = commandAnnotation.name();
        if (commandName.isEmpty()) {
            commandName = commandClass.getSimpleName().toLowerCase();
        }
        
        // サブコマンドの収集
        Map<String, SubCommandData> subCommands = new HashMap<>();
        for (Method method : commandClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SubCommand.class)) {
                SubCommand subCommandAnnotation = method.getAnnotation(SubCommand.class);
                
                // サブコマンド名の決定
                String subCommandName = subCommandAnnotation.name();
                if (subCommandName.isEmpty()) {
                    subCommandName = method.getName().toLowerCase();
                }
                
                // メソッドパラメータの検証
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length != 2 || 
                    !CommandSender.class.isAssignableFrom(paramTypes[0]) || 
                    !String[].class.equals(paramTypes[1])) {
                    throw new IllegalArgumentException("サブコマンドメソッド " + method.getName() + 
                        " のパラメータは (CommandSender, String[]) である必要があります");
                }
                
                method.setAccessible(true);
                
                SubCommandData subCommandData = new SubCommandData(
                    subCommandName,
                    subCommandAnnotation.permission(),
                    Arrays.asList(subCommandAnnotation.alias()),
                    subCommandAnnotation.timeout(),
                    subCommandAnnotation.description(),
                    subCommandAnnotation.usage(),
                    subCommandAnnotation.playerOnly(),
                    method,
                    commandInstance
                );
                
                subCommands.put(subCommandName, subCommandData);
                
                // エイリアスの登録
                for (String alias : subCommandAnnotation.alias()) {
                    subCommands.put(alias.toLowerCase(), subCommandData);
                }
            }
        }
        
        // コマンドデータの作成
        CommandData commandData = new CommandData(
            commandName,
            commandAnnotation.permission(),
            Arrays.asList(commandAnnotation.alias()),
            commandAnnotation.timeout(),
            commandAnnotation.description(),
            commandAnnotation.usage(),
            commandInstance,
            subCommands
        );
        
        // コマンドの登録
        commands.put(commandName, commandData);
        
        // エイリアスの登録
        for (String alias : commandAnnotation.alias()) {
            aliases.put(alias.toLowerCase(), commandName);
        }
        
        System.out.println("コマンド '" + commandName + "' を登録しました (サブコマンド: " + subCommands.size() + "個)");
    }
    
    /**
     * 登録されているコマンドを取得する
     * @param name コマンド名またはエイリアス
     * @return コマンドデータ（見つからない場合はnull）
     */
    public CommandData getCommand(String name) {
        String commandName = aliases.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return commands.get(commandName);
    }
    
    /**
     * 登録されているすべてのコマンドを取得する
     * @return コマンドマップ
     */
    public Map<String, CommandData> getCommands() {
        return new HashMap<>(commands);
    }
    
    /**
     * タブ補完の候補を取得する
     * @param sender コマンド送信者
     * @param args 引数
     * @return 補完候補のリスト
     */
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // コマンド名の補完
            String input = args[0].toLowerCase();
            for (CommandData commandData : commands.values()) {
                if (commandData.getPermission().isEmpty() || sender.hasPermission(commandData.getPermission())) {
                    if (commandData.getName().startsWith(input)) {
                        completions.add(commandData.getName());
                    }
                    for (String alias : commandData.getAliases()) {
                        if (alias.startsWith(input)) {
                            completions.add(alias);
                        }
                    }
                }
            }
        } else if (args.length == 2) {
            // サブコマンド名の補完
            CommandData commandData = getCommand(args[0]);
            if (commandData != null && 
                (commandData.getPermission().isEmpty() || sender.hasPermission(commandData.getPermission()))) {
                
                String input = args[1].toLowerCase();
                for (SubCommandData subCommandData : commandData.getSubCommands().values()) {
                    if (subCommandData.getPermission().isEmpty() || sender.hasPermission(subCommandData.getPermission())) {
                        if (subCommandData.getName().startsWith(input)) {
                            completions.add(subCommandData.getName());
                        }
                        for (String alias : subCommandData.getAliases()) {
                            if (alias.startsWith(input)) {
                                completions.add(alias);
                            }
                        }
                    }
                }
            }
        }
        
        Collections.sort(completions);
        return completions;
    }
    
    /**
     * 登録されているコマンド数を取得する
     * @return コマンド数
     */
    public int getCommandCount() {
        return commands.size();
    }
    
    /**
     * 登録されているサブコマンド数を取得する
     * @return サブコマンド数
     */
    public int getSubCommandCount() {
        return commands.values().stream()
            .mapToInt(cmd -> cmd.getSubCommands().size())
            .sum();
    }
}
