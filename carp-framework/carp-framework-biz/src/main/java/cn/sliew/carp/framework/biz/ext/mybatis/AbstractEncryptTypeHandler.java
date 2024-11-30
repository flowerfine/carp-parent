/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.sliew.carp.framework.biz.ext.mybatis;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.StringTypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractEncryptTypeHandler extends StringTypeHandler {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        super.setNonNullParameter(ps, i, encrypt(parameter), jdbcType);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String nullableResult = super.getNullableResult(rs, columnName);
        if (StringUtils.isNotBlank(nullableResult)) {
            return decrypt(nullableResult);
        }
        return nullableResult;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String nullableResult = super.getNullableResult(rs, columnIndex);
        if (StringUtils.isNotBlank(nullableResult)) {
            return decrypt(nullableResult);
        }
        return nullableResult;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String nullableResult = super.getNullableResult(cs, columnIndex);
        if (StringUtils.isNotBlank(nullableResult)) {
            return decrypt(nullableResult);
        }
        return nullableResult;
    }

    protected abstract String encrypt(String input);

    protected abstract String decrypt(String input);
}
