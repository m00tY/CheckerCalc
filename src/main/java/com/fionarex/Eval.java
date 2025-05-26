package com.fionarex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.*;

public class Eval {
    private final @Nullable Eval parent;
    private final Map<String, Object> environment = new HashMap<>();
    public static final Object NIL = new Object();

    public Eval() {
        this.parent = null;
    }

    public Eval(Eval parent) {
        this.parent = parent;
    }

    public @NonNull Object eval(SEXP expr) {
        if (expr instanceof Atom) {
            Atom atom = (Atom) expr;
            String value = atom.getValue();

            Object val = lookup(value);
            if (val != null) {
                return val;
            }

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return value;
            }

        } else if (expr instanceof SEXPList) {
            SEXPList list = (SEXPList) expr;

            if (list.isEmpty()) {
                throw new RuntimeException("Cannot evaluate empty list");
            }

            Object op = eval(list.getElements().get(0));
            if (op == null) {
                throw new RuntimeException("Operator is null");
            }

            List<SEXP> args = list.getElements().subList(1, list.size());

            if (op instanceof String operator) {
                switch (operator) {
                    case "define":
                        return evalDefine(args);
                    case "if":
                        return evalIf(args);
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
                    case ">":
                        return evalComparison(args, (a, b) -> a > b);
                    case "<":
                        return evalComparison(args, (a, b) -> a < b);
                    default:
                        throw new RuntimeException("Unknown operator: " + operator);
                }
            }

            if (op instanceof UserFunction fn) {
                List<Object> evaluatedArgs = new ArrayList<>();
                for (SEXP argExpr : args) {
                    evaluatedArgs.add(eval(argExpr));
                }
                return fn.apply(evaluatedArgs);
            }

            throw new RuntimeException("Cannot apply operator of type: " + op.getClass().getName());
        }

        throw new RuntimeException("Unknown SEXP type: " + expr.getClass());
    }

    private @NonNull Object evalIf(List<SEXP> args) {
        if (args.size() < 2 || args.size() > 3) {
            throw new RuntimeException("if expects 2 or 3 arguments");
        }

        Object condition = eval(args.get(0));
        boolean condTrue;

        if (condition instanceof Integer) {
            condTrue = ((Integer) condition) != 0;
        } else if (condition instanceof Boolean) {
            condTrue = (Boolean) condition;
        } else if (condition == null) {
            condTrue = false;
        } else {
            condTrue = true;
        }

        if (condTrue) {
            return eval(args.get(1));
        } else if (args.size() == 3) {
            return eval(args.get(2));
        } else {
            return NIL;
        }
    }

    private interface IntComparisonOperator {
        boolean apply(int a, int b);
    }

    private boolean evalComparison(List<SEXP> args, IntComparisonOperator op) {
        if (args.size() != 2) {
            throw new RuntimeException("Comparison operators require exactly two operands");
        }

        Object leftObj = eval(args.get(0));
        Object rightObj = eval(args.get(1));

        if (!(leftObj instanceof Integer) || !(rightObj instanceof Integer)) {
            throw new RuntimeException("Comparison operands must be integers");
        }

        int left = (Integer) leftObj;
        int right = (Integer) rightObj;

        return op.apply(left, right);
    }

    private @Nullable Object lookup(String name) {
        if (environment.containsKey(name)) {
            return environment.get(name);
        } else if (parent != null) {
            return parent.lookup(name);
        } else {
            return null;
        }
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

    private Object evalDefine(List<SEXP> args) {
        if (args.size() < 2) {
            throw new RuntimeException("Invalid define syntax");
        }

        SEXP nameForm = args.get(0);

        if (nameForm instanceof Atom) {
            String name = ((Atom) nameForm).getValue();
            Object value = eval(args.get(1));
            environment.put(name, value);
            return value;
        }

        if (nameForm instanceof SEXPList) {
            SEXPList sig = (SEXPList) nameForm;
            if (sig.isEmpty() || !(sig.get(0) instanceof Atom)) {
                throw new RuntimeException("Invalid function definition");
            }

            String funcName = ((Atom) sig.get(0)).getValue();
            List<String> params = new ArrayList<>();
            for (int i = 1; i < sig.size(); i++) {
                if (!(sig.get(i) instanceof Atom)) {
                    throw new RuntimeException("Function parameters must be atoms");
                }
                params.add(((Atom) sig.get(i)).getValue());
            }

            SEXP body = args.get(1);
            UserFunction fn = new UserFunction(params, body, this);
            environment.put(funcName, fn);
            return fn;
        }

        throw new RuntimeException("Malformed define expression");
    }

    public void setVariable(String name, Object value) {
        environment.put(name, value);
    }
}
