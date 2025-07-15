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
    
    /**
     * チャット入力待機状態のデータ
     */
    public static class ChatInputData {
        private final ArgumentInputSession session;
        private final Consumer<CommandGUIManager.CommandGUIData> paginationCreator;
        private final ArgumentInputGUIManager argumentInputManager;
        
        public ChatInputData(ArgumentInputSession session, 
                           Consumer<CommandGUIManager.CommandGUIData> paginationCreator,
                           ArgumentInputGUIManager argumentInputManager) {
            this.session = session;
            this.paginationCreator = paginationCreator;
            this.argumentInputManager = argumentInputManager;
        }
        
        public ArgumentInputSession getSession() { return session; }
        public Consumer<CommandGUIManager.CommandGUIData> getPaginationCreator() { return paginationCreator; }
        public ArgumentInputGUIManager getArgumentInputManager() { return argumentInputManager; }
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
            inputData.getArgumentInputManager().showArgumentInputGUI(player, inputData.getSession(), inputData.getPaginationCreator());
            return;
        }
        
        // 引数タイプに応じたバリデーション
        var currentArg = inputData.getSession().getCurrentArgument();
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
        inputData.getSession().addArgument(input);
        player.sendMessage(ChatColor.GREEN + "入力を受け付けました: " + ChatColor.WHITE + input);
        
        // 次の引数入力またはコマンド実行へ
        inputData.getArgumentInputManager().showArgumentInputGUI(player, inputData.getSession(), inputData.getPaginationCreator());
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
            case PLAYER:
            case ITEM_ID:
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
        waitingForInput.remove(event.getPlayer().getUniqueId());
    }
}
