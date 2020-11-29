package io.github.vipcxj.beanknife.converters;

import io.github.vipcxj.beanknife.PropertyConverter;

public class NullIntegerAsZeroConverter implements PropertyConverter<Integer, Integer> {
    @Override
    public Integer convert(Integer value) {
        return value != null ? value : 0;
    }
}
