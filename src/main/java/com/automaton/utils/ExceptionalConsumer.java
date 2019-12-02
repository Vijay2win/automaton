package com.automaton.utils;

public interface ExceptionalConsumer<T> {
    void accept(T paramT) throws Exception;
}
