package cn.sliew.carp.framework.kubernetes.resources;

import java.util.function.UnaryOperator;

public interface ReadOnlyKubernetesResource<T> extends KubernetesResource<T> {

    @Override
    default T createOrGet(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    default T patch(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean delete() {
        throw new UnsupportedOperationException();
    }
}
