package com.fionarex;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Eval {
    private final Map<String, Object> environment = new HashMap<>();

    public @Nullable Object eval(SEXP expr) {
        if (expr instanceof Atom) {
            Atom atom = (Atom) expr;
            String value = atom.getValue();

            if (environment.containsKey(value)) {
                return environment.get(value);
            }

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return value;
            }

        } else if (expr instanceof SEXPList) {
            SEXPList list = (SEXPList) expr;

            if (list.isEmpty()) {
                return null;
            }

            Object op = eval(list.getElements().get(0));
            List<SEXP> args = list.getElements().subList(1, list.size());

            if (op instanceof String) {
                String operator = (String) op;

                switch (operator) {
                    case "+":
                        return evalArithmetic(args, (a, b) -> a + b);
                    case "-":
                        return evalArithmetic(args, (a, b) -> a - b);
                    case "*":
                        return evalArithmetic(args, (a, b) -> a * b);
                    case "/":
                        return evalArithmetic(args, (a, b) -> {
                            if (b == 0) throw new RuntimeException("Division by zero");
                            return a / b;
                        });
                    default:
                        throw new RuntimeException("Unknown operator: " + operator);
                }
            }

            return list;
        }

        throw new RuntimeException("Unknown SEXP type: " + expr.getClass());
    }

    private interface IntBinaryOperator {
        int apply(int a, int b);
    }

    private int evalArithmetic(List<SEXP> args, IntBinaryOperator op) {
        if (args.isEmpty()) {
            throw new RuntimeException("Operator requires at least one operand");
        }

        Object resultObj = eval(args.get(0));
        if (!(resultObj instanceof Integer)) {
            throw new RuntimeException("Expected integer operand but got: " + resultObj);
        }
        int result = (Integer) resultObj;
        
        for (int i = 1; i < args.size(); i++) {
            Object nextObj = eval(args.get(i));
            if (!(nextObj instanceof Integer)) {
                throw new RuntimeException("Expected integer operand but got: " + nextObj);
            }
            result = op.apply(result, (Integer) nextObj);
        }
        return result;
    }

    public void setVariable(String name, Object value) {
        environment.put(name, value);
    }
}
