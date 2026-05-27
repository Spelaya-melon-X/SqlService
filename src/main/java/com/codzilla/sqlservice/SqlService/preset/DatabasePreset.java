package com.codzilla.sqlservice.SqlService.preset;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DatabasePreset {
    USERS("users_db", "public", "init_scripts/users_init.sql"),
    PRODUCTS("products_db", "public", "init_scripts/products_init.sql"),
    ORDERS("orders_db", "public", "init_scripts/orders_init.sql");

    private final String databaseName;
    private final String schemaName;
    private final String initScriptPath;
}