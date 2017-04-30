package com.automaton.utils;

public interface ExceptionalConsumer<T> {
    void accept(T t) throws Exception;
}
