package de.neincraft.neincraftplugin.modules.database.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class UUIDDataType implements UserType<UUID> {
    @Override
    public int getSqlType() {
        return SqlTypes.VARCHAR;
    }

    @Override
    public Class<UUID> returnedClass() {
        return UUID.class;
    }

    @Override
    public boolean equals(UUID x, UUID y) {
        return Objects.equals(x,y);
    }

    @Override
    public int hashCode(UUID x) {
        return x.hashCode();
    }

    @Override
    public UUID nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String s = rs.getString(position);
        if (s == null) return null;
        try {
            return UUID.fromString(s);
        }catch (IllegalArgumentException e){
            throw new SQLException("String %s not a valid uuid".formatted(s), e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, UUID value, int index, SharedSessionContractImplementor session) throws SQLException {
        if(value == null){
            st.setNull(index, SqlTypes.VARCHAR);
            return;
        }
        st.setString(index, value.toString());
    }

    @Override
    public UUID deepCopy(UUID value) {
        //UUID is immutable so this is fine
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(UUID value) {
        return value;
    }

    @Override
    public UUID assemble(Serializable cached, Object owner) {
        return cached instanceof UUID uuid ? uuid : null;
    }
}
