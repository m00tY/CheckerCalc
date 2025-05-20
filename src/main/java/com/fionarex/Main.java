package com.fionarex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Eval evaluator = new Eval();

        if (args.length == 0) {
            // Start REPL
            System.out.println("Welcome to the REPL. Type expressions, or 'exit' to quit.");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while (true) {
                    System.out.print("> ");
                    line = reader.readLine();
                    if (line == null || line.strip().equalsIgnoreCase("exit")) {
                        System.out.println("Goodbye.");
                        break;
                    }
                    if (line.isBlank()) {
                        continue;
                    }

                    try {
                        Lexer lexer = new Lexer(line);
                        List<Token> tokens = lexer.lex();
                        Parser parser = new Parser(tokens);
                        List<SEXP> expressions = parser.parseAll();

                        for (SEXP expr : expressions) {
                            Object result = evaluator.eval(expr);
                            System.out.println(result);
                        }
                    } catch (Exception e) {
                        System.err.println("Error: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("IO error in REPL: " + e.getMessage());
                System.exit(1);
            }

        } else {
            // File mode
            String fileName = args[0];
            Path file = Path.of(fileName);
            if (!Files.isReadable(file)) {
                System.err.printf("Error: Cannot read file '%s'%n", fileName);
                System.exit(1);
            }

            String content;
            try {
                content = Files.readString(file).strip();
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                System.exit(1);
                return;
            }

            if (content.isEmpty()) {
                System.err.println("Error: File is empty.");
                System.exit(1);
            }

            try {
                Lexer lexer = new Lexer(content);
                List<Token> tokens = lexer.lex();

                if (tokens.isEmpty()) {
                    System.err.println("Error: No tokens generated.");
                    System.exit(1);
                }

                Parser parser = new Parser(tokens);
                List<SEXP> expressions = parser.parseAll();

                if (expressions.isEmpty()) {
                    System.err.println("Error: No expressions parsed.");
                    System.exit(1);
                }

                for (SEXP expr : expressions) {
                    Object result = evaluator.eval(expr);
                    System.out.println(result);
                }
            } catch (Exception e) {
                System.err.println("Error during parsing or evaluation: " + e.getMessage());
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }
}
