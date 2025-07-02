package com.omori.taskmanagement.springboot.hibernate;

import com.omori.taskmanagement.springboot.model.usermgmt.UserStatus;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class UserStatusType implements UserType<UserStatus> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<UserStatus> returnedClass() {
        return null;
    }

    @Override
    public boolean equals(UserStatus userStatus, UserStatus j1) {
        return false;
    }

    @Override
    public int hashCode(UserStatus userStatus) {
        return 0;
    }

    @Override
    public UserStatus nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        String name = rs.getString(position);
        return name == null ? null : UserStatus.valueOf(name);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, UserStatus value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value.name(), Types.OTHER);
        }
    }

    @Override
    public UserStatus deepCopy(UserStatus userStatus) {
        return null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(UserStatus userStatus) {
        return null;
    }

    @Override
    public UserStatus assemble(Serializable serializable, Object o) {
        return null;
    }

    @Override
    public  UserStatus replace(UserStatus detached, UserStatus managed, Object owner){
        return  detached;
    }

}
