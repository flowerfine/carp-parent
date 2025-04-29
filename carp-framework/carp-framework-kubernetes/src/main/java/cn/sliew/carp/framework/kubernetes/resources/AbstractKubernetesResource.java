package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.UnaryOperator;

@RequiredArgsConstructor
public class AbstractKubernetesResource<T extends HasMetadata> implements KubernetesResource<T> {

    private final KubernetesClient client;
    private final T origin;

    @Override
    public T getOrigin() {
        return origin;
    }

    @Override
    public Optional<T> get() {
        return Optional.ofNullable(client.resource(origin).get());
    }

    @Override
    public T createOrGet(UnaryOperator<T> operator) {
        return get().orElseGet(() -> {
            T resource = operator.apply(origin);
            return client.resource(resource).createOrReplace();
        });
    }

    @Override
    public T patch(UnaryOperator<T> operator) {
        return client.resource(origin).patch(operator.apply(origin));
    }

    @Override
    public boolean delete() {
        client.resource(origin).delete();
        return true;
    }
}
