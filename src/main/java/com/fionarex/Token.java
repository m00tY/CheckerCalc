package com.fionarex;

import org.checkerframework.checker.nullness.qual.*;

public class Token {
    public enum Type {
        NUMBER,
        STRING, SYMBOL,
        PLUS, MINUS, STAR, SLASH,
        LPAREN, RPAREN,
        EOF

    }

    public final @NonNull Type type;
    public final @NonNull String lexeme;
    public final @Nullable Object literal;



    public Token(@NonNull Type type, @NonNull String lexeme, @Nullable Object literal) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
    }

    @Override
    public String toString() {
        return type + " '" + lexeme + "'" + (literal != null ? " = " + literal : "");
    }
}
