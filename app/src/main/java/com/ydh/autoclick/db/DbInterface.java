package com.ydh.autoclick.db;

public interface DbInterface<T> {
    void success(T result);

    void fail();
}
