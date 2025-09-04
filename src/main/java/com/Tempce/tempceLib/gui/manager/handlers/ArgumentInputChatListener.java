package com.Tempce.tempceLib.gui.manager.handlers;

import com.Tempce.tempceLib.gui.manager.handlers.ArgumentInputGUIManager.ArgumentInputSession;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 引数入力のためのチャットリスナー
 */
public class ArgumentInputChatListener implements Listener {
    
    private final Map<UUID, ChatInputData> waitingForInput = new ConcurrentHashMap<>();
    private final Map<UUID, NumberInputData> waitingForNumberInput = new ConcurrentHashMap<>();

    /**
     * チャット入力待機状態のデータ
     */
        public record ChatInputData(ArgumentInputSession session,
                                    Consumer<CommandGUIManager.CommandGUIData> paginationCreator,
                                    ArgumentInputGUIManager argumentInputManager) {
    }

    /**
     * 数値入力データ（GUIManagerから移行）
     */
        public record NumberInputData(String title, int min, int max, Consumer<Integer> onSelect) {
    }
    
    /**
     * プレイヤーをチャット入力待機状態にする
     */
    public void startChatInput(Player player, ArgumentInputSession session, 
                              Consumer<CommandGUIManager.CommandGUIData> paginationCreator,
                              ArgumentInputGUIManager argumentInputManager) {
        waitingForInput.put(player.getUniqueId(), new ChatInputData(session, paginationCreator, argumentInputManager));
    }
    
    /**
     * プレイヤーを数値入力待機状態にする（GUIManagerから移行）
     */
    public void startNumberInput(Player player, String title, int min, int max, Consumer<Integer> onSelect) {
        UUID playerId = player.getUniqueId();
        waitingForNumberInput.put(playerId, new NumberInputData(title, min, max, onSelect));
        player.sendMessage(ChatColor.YELLOW + "数値を入力してください。'cancel'でキャンセルできます。");
    }
    
    /**
     * プレイヤーのチャット入力待機状態をキャンセル
     */
    public void cancelChatInput(Player player) {
        waitingForInput.remove(player.getUniqueId());
    }
    
    /**
     * プレイヤーがチャット入力待機中かどうか
     */
    public boolean isWaitingForInput(Player player) {
        return waitingForInput.containsKey(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // 数値入力待機状態をチェック（優先度高）
        if (waitingForNumberInput.containsKey(playerId)) {
            handleNumberInput(event);
            return;
        }
        
        // 引数入力待機状態をチェック
        if (!waitingForInput.containsKey(playerId)) {
            return;
        }
        
        // チャットイベントをキャンセル（他のプレイヤーに見えないようにする）
        event.setCancelled(true);
        
        ChatInputData inputData = waitingForInput.remove(playerId);
        String input = event.getMessage().trim();
        
        // 入力のバリデーション
        if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("キャンセル")) {
            player.sendMessage(ChatColor.RED + "入力をキャンセルしました。");
            return;
        }
        
        if (input.isEmpty()) {
            player.sendMessage(ChatColor.RED + "入力が空です。再度入力してください。");
            inputData.argumentInputManager().showArgumentInputGUI(player, inputData.session(), inputData.paginationCreator());
            return;
        }
        
        // 引数タイプに応じたバリデーション
        var currentArg = inputData.session().getCurrentArgument();
        if (currentArg != null) {
            String validationError = validateInput(input, currentArg);
            if (validationError != null) {
                player.sendMessage(ChatColor.RED + validationError);
                player.sendMessage(ChatColor.YELLOW + "再度入力してください（'cancel'でキャンセル）：");
                waitingForInput.put(playerId, inputData); // 再度待機状態にする
                return;
            }
        }
        
        // 入力を受け入れて次の処理へ
        inputData.session().addArgument(input);
        player.sendMessage(ChatColor.GREEN + "入力を受け付けました: " + ChatColor.WHITE + input);
        
        // 次の引数入力またはコマンド実行へ
        inputData.argumentInputManager().showArgumentInputGUI(player, inputData.session(), inputData.paginationCreator());
    }
    
    /**
     * 数値入力の処理（GUIManagerから移行）
     */
    private void handleNumberInput(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        NumberInputData inputData = waitingForNumberInput.get(playerId);
        if (inputData == null) {
            return; // 待機状態ではない
        }
        
        event.setCancelled(true); // チャットメッセージを他のプレイヤーに表示しない
        
        String message = event.getMessage().trim();
        
        // キャンセルコマンド
        if (message.equalsIgnoreCase("cancel")) {
            waitingForNumberInput.remove(playerId);
            org.bukkit.Bukkit.getScheduler().runTask(com.Tempce.tempceLib.TempceLib.getInstance(), () -> {
                player.sendMessage(ChatColor.RED + "数値入力をキャンセルしました。");
                // GUIに戻る処理は必要に応じて実装
            });
            return;
        }
        
        // 数値の解析
        try {
            int value = Integer.parseInt(message);
            
            // 範囲チェック
            if (value < inputData.min() || value > inputData.max()) {
                org.bukkit.Bukkit.getScheduler().runTask(com.Tempce.tempceLib.TempceLib.getInstance(), () -> player.sendMessage(ChatColor.RED + "数値が範囲外です。" +
                                 inputData.min() + " - " + inputData.max() + " の範囲で入力してください。"));
                return;
            }
            
            // 入力成功
            waitingForNumberInput.remove(playerId);
            org.bukkit.Bukkit.getScheduler().runTask(com.Tempce.tempceLib.TempceLib.getInstance(), () -> {
                player.sendMessage(ChatColor.GREEN + "数値 " + value + " が選択されました。");
                inputData.onSelect().accept(value);
            });
            
        } catch (NumberFormatException e) {
            org.bukkit.Bukkit.getScheduler().runTask(com.Tempce.tempceLib.TempceLib.getInstance(), () -> player.sendMessage(ChatColor.RED + "無効な数値です。整数を入力してください。"));
        }
    }
    
    /**
     * 入力値のバリデーション
     */
    private String validateInput(String input, com.Tempce.tempceLib.command.data.ArgumentData argument) {
        switch (argument.getType()) {
            case INTEGER:
                try {
                    int value = Integer.parseInt(input);
                    if (argument.getMin() != Double.MIN_VALUE && value < argument.getMin()) {
                        return "値が小さすぎます。最小値: " + (int)argument.getMin();
                    }
                    if (argument.getMax() != Double.MAX_VALUE && value > argument.getMax()) {
                        return "値が大きすぎます。最大値: " + (int)argument.getMax();
                    }
                } catch (NumberFormatException e) {
                    return "整数を入力してください。";
                }
                break;
                
            case DOUBLE:
                try {
                    double value = Double.parseDouble(input);
                    if (argument.getMin() != Double.MIN_VALUE && value < argument.getMin()) {
                        return "値が小さすぎます。最小値: " + argument.getMin();
                    }
                    if (argument.getMax() != Double.MAX_VALUE && value > argument.getMax()) {
                        return "値が大きすぎます。最大値: " + argument.getMax();
                    }
                } catch (NumberFormatException e) {
                    return "数値を入力してください。";
                }
                break;
                
            case COORDINATE_X:
            case COORDINATE_Y:
            case COORDINATE_Z:
                try {
                    double value = Double.parseDouble(input);
                    if (argument.getMin() != Double.MIN_VALUE && value < argument.getMin()) {
                        return "座標値が小さすぎます。最小値: " + argument.getMin();
                    }
                    if (argument.getMax() != Double.MAX_VALUE && value > argument.getMax()) {
                        return "座標値が大きすぎます。最大値: " + argument.getMax();
                    }
                } catch (NumberFormatException e) {
                    return "座標は数値で入力してください。";
                }
                break;
                
            case BOOLEAN:
                if (!input.equalsIgnoreCase("true") && !input.equalsIgnoreCase("false") &&
                    !input.equalsIgnoreCase("on") && !input.equalsIgnoreCase("off") &&
                    !input.equalsIgnoreCase("yes") && !input.equalsIgnoreCase("no")) {
                    return "true/false、on/off、yes/no のいずれかを入力してください。";
                }
                break;
                
            case STRING:
            case ONLINE_PLAYER:
            case ALL_PLAYER:
            case ITEM_ID:
            case ITEM_ID_TOOL:
            case ITEM_ID_BLOCK:
            case ITEM_ID_NATURE_BLOCK:
            case ITEM_ID_WEAPON_ARMOR:
            case ITEM_ID_FOOD:
            case ITEM_ID_DECORATION:
            case ENTITY_ID:
            case WORLD:
            case ENCHANTMENT:
            case POTION_EFFECT:
                // これらのタイプは基本的にチャット入力では使用しない（GUI選択）
                // 文字列として受け入れる
                break;
        }
        
        return null; // バリデーション成功
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // プレイヤーがログアウトしたら待機状態をクリア
        UUID playerId = event.getPlayer().getUniqueId();
        waitingForInput.remove(playerId);
        waitingForNumberInput.remove(playerId);
    }
}
