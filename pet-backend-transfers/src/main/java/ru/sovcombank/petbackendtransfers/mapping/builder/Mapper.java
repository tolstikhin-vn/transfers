package ru.sovcombank.petbackendtransfers.mapping.builder;

import java.util.Collection;

public interface Mapper<S, T> {
    T map(S source);

    default Collection<T> mapAll(Collection<S> sources) {
        return sources.stream().map(this::map).toList();
    }
}
