package com.fionarex;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.index.qual.NonNegative;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final @NonNull String input;
    private @NonNegative int position = 0;

    public Lexer(@NonNull String input) {
        this.input = input;
    }

    private void advance() {
        this.position += 1;
    }

    private char currentChar() {
        return position < input.length() ? input.charAt(position) : '\0';
    }

    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char c = currentChar();

            switch (c) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    advance();
                    break;
                case '(':
                    tokens.add(new Token(Token.Type.LPAREN, "(", null));
                    advance();
                    break;
                case ')':
                    tokens.add(new Token(Token.Type.RPAREN, ")", null));
                    advance();
                    break;
                case '+':
                    tokens.add(new Token(Token.Type.PLUS, "+", null));
                    advance();
                    break;
                case '-':
                    tokens.add(new Token(Token.Type.MINUS, "-", null));
                    if (isNextDigit()) {
                        advance();
                        lexNumber();
                    }
                    advance();
                    break;
                case '*':
                    tokens.add(new Token(Token.Type.STAR, "*", null));
                    advance();
                    break;
                case '/':
                    tokens.add(new Token(Token.Type.SLASH, "/", null));
                    advance();
                    break;
                case '"':
                    tokens.add(lexString());
                    break;
                default:
                    if (Character.isDigit(c)) {
                        tokens.add(lexNumber());
                    } else if (isSymbolStart(c)) {
                        tokens.add(lexSymbol());
                    } else {
                        throw new RuntimeException("Unexpected character: " + c);
                    }
            }
        }

        tokens.add(new Token(Token.Type.EOF, "", null));
        return tokens;
    }

    private boolean isNextDigit() {
        return position + 1 < input.length() && Character.isDigit(input.charAt(position + 1));
    }

    private boolean isSymbolStart(char c) {
        return Character.isLetter(c) || "+-*/=!<>?.".indexOf(c) != -1;
    }

    private Token lexNumber() {
        int start = position;
        if (currentChar() == '-') advance();

        while (position < input.length() && (Character.isDigit(currentChar()) || ".".indexOf(currentChar()) == 0)) {
            advance();
        }

        if (".".indexOf(currentChar()) == 0) {
            advance();
            while (position < input.length() && Character.isDigit(currentChar())) {
                advance();
            }
        }

        String numberStr = input.substring(start, position);
        double value = Double.parseDouble(numberStr);
        return new Token(Token.Type.NUMBER, numberStr, value);
    }

    private Token lexSymbol() {
        int start = position;
        while (position < input.length() && !Character.isWhitespace(currentChar()) && "()\"".indexOf(currentChar()) == -1) {
            advance();
        }
        String symbol = input.substring(start, position);
        return new Token(Token.Type.SYMBOL, symbol, null);
    }

    private Token lexString() {
        advance();
        int start = position;

        while (position < input.length() && currentChar() != '"') {
            advance();
        }

        if (currentChar() != '"') {
            throw new RuntimeException("Unterminated string literal");
        }

        String value = input.substring(start, position);
        advance();

        return new Token(Token.Type.STRING, value, value);
    }
}
