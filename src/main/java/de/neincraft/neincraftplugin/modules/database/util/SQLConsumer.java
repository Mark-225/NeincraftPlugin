package de.neincraft.neincraftplugin.modules.database.util;

import java.sql.SQLException;
import java.util.function.Consumer;

@FunctionalInterface
public interface SQLConsumer<T> {

    void accept(T element) throws SQLException;

}
