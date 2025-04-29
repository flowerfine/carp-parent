package cn.sliew.carp.framework.kubernetes.resources;

public interface KubernetesResourceCollection<T> {

    Iterable<T> listResources();
}
