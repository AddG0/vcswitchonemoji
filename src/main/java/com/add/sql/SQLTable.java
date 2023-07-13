package com.add.sql;

public interface SQLTable {
    String getName();

    Class<?> getType();

    String getTableName();
}
