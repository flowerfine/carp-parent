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

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public enum DagUtil {
    ;

    public static <N> void execute(DAG<N> dag, Consumer<Set<N>> consumer) {
        execute(dag, (dag1, node) -> true, (dag1, edge) -> true, consumer);
    }

    public static <N> void execute(DAG<N> dag, BiPredicate<DAG<N>, N> nodeValidator, BiPredicate<DAG<N>, DefaultDagEdge<N>> edgeValidator, Consumer<Set<N>> consumer) {
        Set<N> sources = dag.getSources();
        DAG<N> state = dag.copy();
        doExecute(dag, state, sources, nodeValidator, edgeValidator, consumer);
    }

    private static <N> void doExecute(DAG<N> dag, DAG<N> state, Set<N> set, BiPredicate<DAG<N>, N> nodeValidator, BiPredicate<DAG<N>, DefaultDagEdge<N>> edgeValidator, Consumer<Set<N>> consumer) {
        if (CollectionUtils.isEmpty(set)) {
            return;
        }
        Set<N> checkedSet = set.stream().filter(node -> nodeValidator.test(dag, node)).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(checkedSet) == false) {
            consumer.accept(checkedSet);
        }

        // 收集下一批次需要执行的节点
        Set<N> nextSet = Sets.newLinkedHashSet();
        set.forEach(node -> {
            dag.outDegreeOf(node).forEach(outDegreeNode -> {
                DefaultDagEdge<N> edge = dag.getEdge(node, outDegreeNode);
                if (edgeValidator.test(dag, edge)) {
                    state.removeEdge(node, outDegreeNode);
                }
                if (state.inDegree(outDegreeNode) == 0) {
                    nextSet.add(outDegreeNode);
                }
            });
        });

        // 执行下一批次
        if (CollectionUtils.isNotEmpty(nextSet)) {
            doExecute(dag, state, nextSet, nodeValidator, edgeValidator, consumer);
        }
    }
}
