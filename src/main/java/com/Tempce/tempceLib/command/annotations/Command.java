package com.Tempce.tempceLib.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * コマンドクラスに付与するアノテーション
 * このアノテーションが付与されたクラスは自動的にコマンドとして登録されます
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    
    /**
     * コマンド名（省略時はクラス名をlowerCaseに変換したものを使用）
     */
    String name() default "";
    
    /**
     * 実行に必要な権限（省略時は権限チェックなし）
     */
    String permission() default "";
    
    /**
     * コマンドの別名
     */
    String[] alias() default {};
    
    /**
     * コマンドの再実行までの待機時間（秒単位、0の場合は制限なし）
     */
    int timeout() default 0;
    
    /**
     * コマンドの説明
     */
    String description() default "";
    
    /**
     * コマンドの使用方法
     */
    String usage() default "";
}
