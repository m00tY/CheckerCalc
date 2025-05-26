package com.fionarex;

import java.util.List;

public class UserFunction {
    private final List<String> parameters;
    private final SEXP body;
    private final Eval closureEnv;

    public UserFunction(List<String> parameters, SEXP body, Eval closureEnv) {
        this.parameters = parameters;
        this.body = body;
        this.closureEnv = closureEnv;
    }

    public Object apply(List<Object> args) {
        Eval localEnv = new Eval(closureEnv); // Create a new local environment
        for (int i = 0; i < parameters.size(); i++) {
            localEnv.setVariable(parameters.get(i), args.get(i));
        }
        return localEnv.eval(body); // This returns Object
    }

    public String toString() {
        return "[function (" + String.join(" ", parameters) + ")]";
    }
}
