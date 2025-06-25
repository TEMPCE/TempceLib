package com.Tempce.tempceLib.command.manager;

import com.Tempce.tempceLib.TempceLib;
import com.Tempce.tempceLib.command.annotations.Command;
import com.Tempce.tempceLib.command.annotations.SubCommand;
import com.Tempce.tempceLib.command.completer.TempceTabCompleter;
import com.Tempce.tempceLib.command.data.CommandData;
import com.Tempce.tempceLib.command.data.SubCommandData;
import com.Tempce.tempceLib.command.executor.TempceCommandExecutor;
import com.Tempce.tempceLib.command.helper.AutoHelpExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * コマンドシステムの管理クラス
 */
public class CommandManager {
      private final Map<String, CommandData> commands = new ConcurrentHashMap<>();
    private final Map<String, String> aliases = new ConcurrentHashMap<>();
    private CommandMap commandMap;
    
    /**
     * コマンドマネージャーのコンストラクタ
     */
    public CommandManager() {
        initializeCommandMap();
    }
    
    /**
     * Bukkitの内部CommandMapを取得して初期化する
     */
    private void initializeCommandMap() {
        try {
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());
        } catch (Exception e) {
            TempceLib.getInstance().getLogger().warning("CommandMapの取得に失敗しました: " + e.getMessage());
        }
    }
    
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
        
        // 自動ヘルプサブコマンドを追加
        addAutoHelpSubCommand(subCommands, commandInstance, commandAnnotation);
        
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
        
        // Bukkitに動的にコマンドを登録
        registerCommandToBukkit(commandData);
        
        // エイリアスの登録
        for (String alias : commandAnnotation.alias()) {
            aliases.put(alias.toLowerCase(), commandName);
        }        TempceLib.getInstance().getLogger().info("コマンド '" + commandName + "' を登録しました (サブコマンド: " + subCommands.size() + "個)");
    }
    
    /**
     * 自動ヘルプサブコマンドを追加する
     * @param subCommands サブコマンドマップ
     * @param commandInstance コマンドインスタンス
     * @param commandAnnotation コマンドアノテーション
     */
    private void addAutoHelpSubCommand(Map<String, SubCommandData> subCommands, Object commandInstance, Command commandAnnotation) {
        // 既に"help"サブコマンドが存在する場合はスキップ
        if (subCommands.containsKey("help")) {
            return;
        }
        
        try {
            // 自動ヘルプメソッドを作成
            Method helpMethod = createHelpMethod(commandAnnotation, subCommands);
            
            SubCommandData helpSubCommand = new SubCommandData(
                "help",
                "", // 権限なし
                    List.of(""), // エイリアス
                0, // タイムアウトなし
                "このコマンドのヘルプを表示",
                "help [subcommand]",
                false, // プレイヤー限定なし
                helpMethod,
                new AutoHelpExecutor(commandAnnotation, subCommands)
            );
            
            subCommands.put("help", helpSubCommand);
            
        } catch (Exception e) {
            TempceLib.getInstance().getLogger().warning("自動ヘルプコマンドの作成に失敗しました: " + e.getMessage());
        }
    }
    
    /**
     * ヘルプメソッドを動的に作成する
     */
    private Method createHelpMethod(Command commandAnnotation, Map<String, SubCommandData> subCommands) throws NoSuchMethodException {
        return AutoHelpExecutor.class.getMethod("executeHelp", CommandSender.class, String[].class);
    }
    
    /**
     * コマンドをBukkitに動的に登録する
     * @param commandData 登録するコマンドデータ
     */
    private void registerCommandToBukkit(CommandData commandData) {
        if (commandMap == null) {
            TempceLib.getInstance().getLogger().warning("CommandMapが利用できないため、コマンド '" + 
                commandData.getName() + "' のBukkit登録をスキップしました");
            return;
        }
        
        try {
            // PluginCommandを作成
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(commandData.getName(), TempceLib.getInstance());
            
            // エグゼキューターとタブコンプリーターを設定
            TempceCommandExecutor executor = new TempceCommandExecutor(this);
            TempceTabCompleter completer = new TempceTabCompleter(this);
            
            pluginCommand.setExecutor(executor);
            pluginCommand.setTabCompleter(completer);
            
            // 説明とusageを設定
            if (!commandData.getDescription().isEmpty()) {
                pluginCommand.setDescription(commandData.getDescription());
            }
            if (!commandData.getUsage().isEmpty()) {
                pluginCommand.setUsage(commandData.getUsage());
            }
            
            // エイリアスを設定
            if (!commandData.getAliases().isEmpty()) {
                pluginCommand.setAliases(commandData.getAliases());
            }
            
            // CommandMapに登録
            commandMap.register(TempceLib.getInstance().getDescription().getName(), pluginCommand);
            
        } catch (Exception e) {
            TempceLib.getInstance().getLogger().severe("コマンド '" + commandData.getName() + 
                "' のBukkit登録に失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
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
     * @param commandName 実行されたコマンド名
     * @param args 引数
     * @return 補完候補のリスト
     */
    public List<String> getTabCompletions(CommandSender sender, String commandName, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // 実行されたコマンドを取得
        CommandData commandData = getCommand(commandName);
        if (commandData == null) {
            return completions;
        }
        
        // コマンドの権限チェック
        if (!commandData.getPermission().isEmpty() && !sender.hasPermission(commandData.getPermission())) {
            return completions;
        }        if (args.length == 1) {
            // サブコマンド名の補完
            String input = args[0].toLowerCase();
            Set<String> addedCompletions = new HashSet<>();
            
            for (SubCommandData subCommandData : commandData.getSubCommands().values()) {
                if (subCommandData.getPermission().isEmpty() || sender.hasPermission(subCommandData.getPermission())) {
                    // メインの名前を追加
                    if (subCommandData.getName().startsWith(input) && !addedCompletions.contains(subCommandData.getName())) {
                        completions.add(subCommandData.getName());
                        addedCompletions.add(subCommandData.getName());
                    }
                    // エイリアスを追加
                    for (String alias : subCommandData.getAliases()) {
                        if (alias.startsWith(input) && !addedCompletions.contains(alias)) {
                            completions.add(alias);
                            addedCompletions.add(alias);
                        }
                    }
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("help")) {
            // helpコマンドの補完 - 利用可能なサブコマンド名を提案
            String input = args[1].toLowerCase();
            Set<String> addedCompletions = new HashSet<>();
            
            for (SubCommandData subCommandData : commandData.getSubCommands().values()) {
                if (!subCommandData.getName().equals("help") && 
                    (subCommandData.getPermission().isEmpty() || sender.hasPermission(subCommandData.getPermission()))) {
                    
                    if (subCommandData.getName().startsWith(input) && !addedCompletions.contains(subCommandData.getName())) {
                        completions.add(subCommandData.getName());
                        addedCompletions.add(subCommandData.getName());
                    }
                }
            }
        }
        
        Collections.sort(completions);
        return completions;
    }
    
    /**
     * タブ補完の候補を取得する（旧形式、互換性のため残す）
     * @param sender コマンド送信者
     * @param args 引数
     * @return 補完候補のリスト
     * @deprecated getTabCompletions(CommandSender, String, String[])を使用してください
     */
    @Deprecated
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
