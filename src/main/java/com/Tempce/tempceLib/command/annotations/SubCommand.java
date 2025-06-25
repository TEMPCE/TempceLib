package com.Tempce.tempceLib.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * サブコマンドメソッドに付与するアノテーション
 * このアノテーションが付与されたメソッドは自動的にサブコマンドとして登録されます
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {
    
    /**
     * サブコマンドのパス（省略時はメソッド名を使用）
     * 単一レベル: "info", "stats"
     * 多階層: "config.reload", "user.create.admin"
     */
    String path() default "";
    
    /**
     * 実行に必要な権限（省略時は権限チェックなし）
     */
    String permission() default "";
    
    /**
     * サブコマンドの別名
     */
    String[] alias() default {};
    
    /**
     * サブコマンドの再実行までの待機時間（秒単位、0の場合は制限なし）
     */
    int timeout() default 0;
    
    /**
     * サブコマンドの説明
     */
    String description() default "";
    
    /**
     * サブコマンドの使用方法
     */
    String usage() default "";
    
    /**
     * プレイヤーのみ実行可能かどうか
     */
    boolean playerOnly() default false;
}
