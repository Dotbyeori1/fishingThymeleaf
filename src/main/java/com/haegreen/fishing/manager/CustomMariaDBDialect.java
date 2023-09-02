package com.haegreen.fishing.manager;

import org.hibernate.dialect.MariaDB103Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.IntegerType;

public class CustomMariaDBDialect extends MariaDB103Dialect {

    public CustomMariaDBDialect() {
        super();
        registerFunction("bitand", new SQLFunctionTemplate(IntegerType.INSTANCE, "(?1 & ?2)"));
    }
}
