package cn.sliew.carp.framework.kubernetes.resources;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface KubernetesResource<T> {

    T getOrigin();

    Optional<T> get();

    default boolean exists() {
        return get().isPresent();
    }

    T createOrGet(UnaryOperator<T> operator);

    T patch(UnaryOperator<T> operator);

    boolean delete();
}
