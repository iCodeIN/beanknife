package io.github.vipcxj.beanknife.converters;

import io.github.vipcxj.beanknife.PropertyConverter;

import java.math.BigDecimal;

public class NullBigDecimalAsZeroConverter implements PropertyConverter<BigDecimal, BigDecimal> {
    @Override
    public BigDecimal convert(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
