package com.fionarex;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.index.qual.NonNegative;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final @NonNull List<@NonNull Token> tokens;
    private @NonNegative int position = 0;

    public Parser(@NonNull List<@NonNull Token> tokens) {
        this.tokens = tokens;
    }

    private @NonNull Token current() {
        if (position >= tokens.size()) {
            // Defensive fallback if we ever run past end
            return new Token(Token.Type.EOF, "", null);
        }
        return tokens.get(position);
    }

    private void advance() {
        if (position < tokens.size()) {
            position++;
        }
    }

    public @NonNull SEXP parse() {
        Token tok = current();
        switch (tok.type) {
            case LPAREN:
                return parseList();

            case NUMBER:
            case SYMBOL:
            case STRING:
            case PLUS:
            case MINUS:
            case STAR:
            case SLASH:
                advance();
                return new Atom(tok.lexeme);

            case EOF:
                throw new RuntimeException("Unexpected EOF while parsing");

            default:
                throw new RuntimeException("Unexpected token: " + tok);
        }
    }

    private @NonNull SEXPList parseList() {
        expect(Token.Type.LPAREN);
        SEXPList list = new SEXPList();

        while (true) {
            Token currentToken = current();
            if (currentToken.type == Token.Type.RPAREN) {
                break;
            }
            if (currentToken.type == Token.Type.EOF) {
                throw new RuntimeException("Unexpected EOF: missing closing ')'");
            }
            list.add(parse());
        }

        expect(Token.Type.RPAREN);
        return list;
    }

    private void expect(Token.@NonNull Type expected) {
        Token currentToken = current();
        if (currentToken.type != expected) {
            throw new RuntimeException("Expected " + expected + " but found " + currentToken);
        }
        advance();
    }

    public @NonNull List<@NonNull SEXP> parseAll() {
        List<SEXP> expressions = new ArrayList<>();

        while (true) {
            Token currentToken = current();
            if (currentToken.type == Token.Type.EOF) {
                break;
            }
            expressions.add(parse());
        }

        return expressions;
    }
}
