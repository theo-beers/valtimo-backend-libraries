/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.valtimo.accessandentitlement.domain;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by Ivar Koreman on 28-Feb-17.
 */
public class Money implements UserType {
    private Currency currency;
    private BigDecimal amount;

    public Money() {
        this(0);
    }

    public Money(long amountInCents) {
        this.currency = Currency.getInstance(new Locale("nl", "NL"));
        this.setAmountInCents(amountInCents);
    }

    public Money(String amount) {
        this.currency = Currency.getInstance(new Locale("nl", "NL"));
        this.setAmount(amount);
    }

    public Money(BigDecimal amount) {
        this.currency = Currency.getInstance(new Locale("nl", "NL"));
        this.amount = amount;
    }

    public Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public Money(Money money) {
        this.currency = money.currency;
        this.amount = money.amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public long getAmountInCents() {
        return amount.multiply(new BigDecimal(100)).longValueExact();
    }

    public void setAmountInCents(long amountInCents) {
        amount = new BigDecimal(amountInCents)
            .setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_DOWN)
            .divide(BigDecimal.valueOf(Math.pow(10, currency.getDefaultFractionDigits())), RoundingMode.HALF_DOWN);
    }

    public String getAmount() {
        return amount.toString();
    }

    public void setAmount(String amount) {
        // More information about BigDecimal and why we use it:
        // http://www.opentaps.org/docs/index.php/How_to_Use_Java_BigDecimal:_A_Tutorial

        this.amount = new BigDecimal(amount)
            .setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_DOWN);
    }

    public BigDecimal getBigDecimalAmount() {
        return amount;
    }

    public String getDisplayString() {
        return currency.getSymbol() + " " + amount.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Money)) {
            return false;
        }

        Money money = (Money) obj;

        return this.currency.equals(money.currency) && this.amount.equals(money.amount);
    }

    @Override
    public boolean equals(Object x, Object y) {
        if (!(x instanceof Money) || !(y instanceof Money)) {
            return false;
        }

        return x.equals(y);
    }

    @Override
    public String toString() {
        return currency.getSymbol() + " " + amount.toString();
    }

    @Override
    public int[] sqlTypes() {
        return new int[] {Types.NUMERIC};
    }

    @Override
    public Class<Money> returnedClass() {
        return Money.class;
    }

    @Override
    public int hashCode(Object x) {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws SQLException {
        return new Money(rs.getBigDecimal(names[0]));
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.VARBINARY);
            return;
        }

        st.setString(index, ((Money) value).getAmount());
    }

    @Override
    public Object deepCopy(Object value) {
        if (!(value instanceof Money)) {
            return value;
        }

        return new Money((Money) value);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) {
        return null;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) {
        return null;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) {
        if (!(original instanceof Money)) {
            return original;
        }

        return new Money((Money) original);
    }
}
