package ru.etc1337.api.other;

import java.util.LinkedList;

public final class LimitList<T>
        extends LinkedList<T> {
    private final int dPT;

    public LimitList(int n) {
        this.dPT = n;
    }

    @Override
    public boolean add(T t) {
        if (this.size() >= this.dPT) {
            this.removeFirst();
        }
        return super.add(t);
    }
}