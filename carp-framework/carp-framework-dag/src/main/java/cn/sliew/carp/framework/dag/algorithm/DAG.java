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
package cn.sliew.carp.framework.dag.algorithm;

import com.google.common.collect.Lists;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DAG<N extends DagNode> implements Visitable {

    private boolean allowLoop;
    private Graph<N, DefaultDagEdge<N>> jgrapht;

    public DAG() {
        this(false);
    }

    public DAG(boolean allowLoop) {
        this.allowLoop = allowLoop;
        jgrapht = GraphTypeBuilder.<N, DefaultDagEdge<N>>directed()
                .allowingSelfLoops(allowLoop)
                .weighted(false)
                .buildGraph();
    }

    public void addNode(N node) {
        jgrapht.addVertex(node);
    }

    public void addEdge(N source, N target) {
        addEdge(source, target, null);
    }

    public void addEdge(N source, N target, Object data) {
        if (jgrapht.containsVertex(source) == false) {
            jgrapht.addVertex(source);
        }
        if (jgrapht.containsVertex(target) == false) {
            jgrapht.addVertex(target);
        }
        jgrapht.addEdge(source, target, new DefaultDagEdge<>(source, target, data));
    }

    public Set<N> nodes() {
        return jgrapht.vertexSet();
    }

    public Set<DefaultDagEdge<N>> edges() {
        return jgrapht.edgeSet();
    }

    public DefaultDagEdge<N> getEdge(N source, N target) {
        return jgrapht.getEdge(source, target);
    }

    public void removeEdge(N source, N target) {
        jgrapht.removeEdge(source, target);
    }

    public Integer inDegree(N node) {
        return jgrapht.inDegreeOf(node);
    }

    public Set<N> inDegreeOf(N node) {
        return jgrapht.incomingEdgesOf(node).stream().map(DefaultDagEdge::getSource).collect(Collectors.toSet());
    }

    public Integer outDegree(N node) {
        return jgrapht.outDegreeOf(node);
    }

    public Set<N> outDegreeOf(N node) {
        return jgrapht.outgoingEdgesOf(node).stream().map(DefaultDagEdge::getTarget).collect(Collectors.toSet());
    }

    public Set<N> getSources() {
        return jgrapht.vertexSet().stream()
                .filter(node -> {
                    return jgrapht.inDegreeOf(node) == 0 ||
                            (allowLoop && jgrapht.inDegreeOf(node) == 1 && inDegreeOf(node).contains(node));
                })
                .collect(Collectors.toSet());
    }

    public Set<N> getSinks() {
        return jgrapht.vertexSet().stream()
                .filter(node -> {
                    return jgrapht.outDegreeOf(node) == 0 ||
                            (allowLoop && jgrapht.outDegreeOf(node) == 1 && outDegreeOf(node).contains(node));
                })
                .collect(Collectors.toSet());
    }

    public List<N> getAncestors(N node) {
        DAG<N> ancestor = new DAG<>();
        ancestor.addNode(node);
        addToAncestor(ancestor, node);
        List<N> topologySort = ancestor.topologySort();
        topologySort.remove(node);
        return topologySort;
    }

    private void addToAncestor(DAG<N> dag, N node) {
        Set<N> inDegreeSet = inDegreeOf(node);
        if (CollectionUtils.isEmpty(inDegreeSet)) {
            return;
        }
        inDegreeSet.forEach(inDegreeNode -> {
            dag.addNode(inDegreeNode);
            dag.addEdge(inDegreeNode, node);
            addToAncestor(dag, inDegreeNode);
        });
    }


    public List<N> getChildren(N node) {
        DAG<N> children = new DAG<>();
        children.addNode(node);
        addToChildren(children, node);
        List<N> topologySort = children.topologySort();
        topologySort.remove(node);
        return topologySort;
    }

    private void addToChildren(DAG<N> dag, N node) {
        Set<N> outDegreeSet = outDegreeOf(node);
        if (CollectionUtils.isEmpty(outDegreeSet)) {
            return;
        }
        outDegreeSet.forEach(outDegreeNode -> {
            dag.addNode(outDegreeNode);
            dag.addEdge(node, outDegreeNode);
            addToChildren(dag, outDegreeNode);
        });
    }

    public Integer getMaxDepth() {
        AllDirectedPaths<N, DefaultDagEdge<N>> paths = new AllDirectedPaths<>(jgrapht);
        return paths.getAllPaths(getSources(), getSinks(), true, null)
                .stream().map(GraphPath::getLength)
                .sorted()
                .findFirst().get();
    }

    public List<N> topologySort() {
        List<N> queue = Lists.newArrayList();
        topologyTraversal(node -> queue.add(node));
        return queue;
    }

    public void topologyTraversal(Consumer<N> consumer) {
        TopologicalOrderIterator<N, DefaultDagEdge<N>> iterator = new TopologicalOrderIterator<>(jgrapht);
        while (iterator.hasNext()) {
            consumer.accept(iterator.next());
        }
    }

    public DAG<N> copy() {
        DAG<N> copy = new DAG<>();
        nodes().forEach(copy::addNode);
        edges().forEach(edge -> copy.addEdge(edge.getSource(), edge.getTarget()));
        return copy;
    }

    @Override
    public String accept(Visitor visitor) {
        StringBuilder sb = new StringBuilder();
        sb.append(visitor.start((DAG<DagNode>) this));
        for (DefaultDagEdge<N> edge : edges()) {
            sb.append(edge.accept(visitor));
        }
        sb.append(visitor.end((DAG<DagNode>) this));
        return sb.toString();
    }
}
