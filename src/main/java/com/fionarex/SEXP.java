package com.fionarex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.*;

public abstract class SEXP {
    public abstract boolean isAtom();
    public abstract boolean isList();
    public abstract String toString();
}

class Atom extends SEXP {
    private final String value;

    public Atom(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean isAtom() {
        return true;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Atom)) return false;
        return Objects.equals(value, ((Atom) obj).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

class SEXPList extends SEXP {
    private final List<SEXP> elements;

    public SEXPList() {
        this.elements = new ArrayList<>();
    }

    public SEXPList(List<SEXP> elements) {
        this.elements = new ArrayList<>(elements);
    }

    public void add(SEXP sexp) {
        elements.add(sexp);
    }

    public List<SEXP> getElements() {
        return elements;
    }

    @Override
    public boolean isAtom() {
        return false;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < elements.size(); i++) {
            sb.append(elements.get(i).toString());
            if (i < elements.size() - 1) sb.append(" ");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof SEXPList)) return false;
        return Objects.equals(elements, ((SEXPList) obj).elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }
}

