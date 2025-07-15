package com.Tempce.tempceLib.command.annotations;

import com.Tempce.tempceLib.command.data.ArgumentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * コマンド引数の詳細情報を定義するアノテーション
 * SubCommandアノテーションと組み合わせて使用します
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandArguments {
    
    /**
     * 引数の配列
     */
    Argument[] value() default {};
    
    /**
     * 単一の引数を定義するアノテーション
     */
    @interface Argument {
        /**
         * 引数名
         */
        String name();
        
        /**
         * 引数のタイプ
         */
        ArgumentType type() default ArgumentType.STRING;
        
        /**
         * 引数の説明
         */
        String description() default "";
        
        /**
         * 必須引数かどうか
         */
        boolean required() default true;
        
        /**
         * デフォルト値（optional引数の場合）
         */
        String defaultValue() default "";
        
        /**
         * 引数の候補値（選択式の場合）
         */
        String[] suggestions() default {};
        
        /**
         * 最小値（数値型の場合）
         */
        double min() default Double.MIN_VALUE;
        
        /**
         * 最大値（数値型の場合）
         */
        double max() default Double.MAX_VALUE;
    }
}
