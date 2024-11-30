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

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DAG<N> {

    private Graph<N, DefaultDagEdge<N>> jgrapht = GraphTypeBuilder.<N, DefaultDagEdge<N>>directed()
            .allowingSelfLoops(false)
            .weighted(false)
            .buildGraph();

    public void addNode(N node) {
        jgrapht.addVertex(node);
    }

    public void addEdge(N source, N target) {
        jgrapht.addEdge(source, target, new DefaultDagEdge<>(source, target));
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
        return jgrapht.incomingEdgesOf(node).stream().map(DefaultDagEdge::getTarget).collect(Collectors.toSet());
    }

    public Integer outDegree(N node) {
        return jgrapht.outDegreeOf(node);
    }

    public Set<N> outDegreeOf(N node) {
        return jgrapht.outgoingEdgesOf(node).stream().map(DefaultDagEdge::getTarget).collect(Collectors.toSet());
    }

    public Set<N> getSources() {
        return jgrapht.vertexSet().stream()
                .filter(node -> jgrapht.inDegreeOf(node) == 0)
                .collect(Collectors.toSet());
    }

    public Set<N> getSinks() {
        return jgrapht.vertexSet().stream()
                .filter(node -> jgrapht.outDegreeOf(node) == 0)
                .collect(Collectors.toSet());
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

    /**
     * https://magjac.com/graphviz-visual-editor/
     * <p>
     * digraph {
     * A -> B;
     * B -> C;
     * B -> D;
     * B -> E;
     * A -> F;
     * A -> K;
     * C -> G;
     * D -> G;
     * E -> G;
     * F -> H;
     * G -> H;
     * H -> I;
     * H -> J;
     * }
     */
    public static void main(String[] args) {
        DAG<String> dag = new DAG<>();
        dag.addNode("A");
        dag.addNode("B");
        dag.addNode("C");
        dag.addNode("D");
        dag.addNode("E");
        dag.addNode("F");
        dag.addNode("G");
        dag.addNode("H");
        dag.addNode("I");
        dag.addNode("J");
        dag.addNode("K");

        dag.addEdge("A", "B");
        dag.addEdge("B", "C");
        dag.addEdge("B", "D");
        dag.addEdge("B", "E");
        dag.addEdge("A", "F");
        dag.addEdge("A", "K");

        dag.addEdge("C", "G");
        dag.addEdge("D", "G");
        dag.addEdge("E", "G");

        dag.addEdge("F", "H");
        dag.addEdge("G", "H");

        dag.addEdge("H", "I");
        dag.addEdge("H", "J");

        System.out.println(dag.topologySort());
        List<Set<String>> queue = Lists.newArrayList();
        DagUtil.execute(dag, nodes -> queue.add(nodes));
        System.out.println(queue);
    }
}
