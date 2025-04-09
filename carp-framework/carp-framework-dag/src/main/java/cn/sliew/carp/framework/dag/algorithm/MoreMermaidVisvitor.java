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

import org.springframework.util.CollectionUtils;

import java.util.Set;

public class MoreMermaidVisvitor implements Visitor {

    public static final MoreMermaidVisvitor INSTANCE = new MoreMermaidVisvitor();

    private MoreMermaidVisvitor() {
    }

    @Override
    public String start(DAG<DagNode> visitable) {
        StringBuilder sb = new StringBuilder();
        sb.append(MermaidVisvitor.INSTANCE.start(visitable));

        Set<DagNode> sources = visitable.getSources();
        if (CollectionUtils.isEmpty(sources) == false) {
            sb.append("START((STAET))").append(MermaidVisvitor.INSTANCE.LF);
            for (DagNode dagNode : visitable.getSources()) {
                sb.append("START")
                        .append(MermaidVisvitor.INSTANCE.SEPERATOR)
                        .append(dagNode.getName())
                        .append(MermaidVisvitor.INSTANCE.LF);
            }
        }
        return sb.toString();
    }

    @Override
    public String end(DAG<DagNode> visitable) {
        StringBuilder sb = new StringBuilder();

        Set<DagNode> sinks = visitable.getSinks();
        if (CollectionUtils.isEmpty(sinks) == false) {
            sb.append("END((END))").append(MermaidVisvitor.INSTANCE.LF);
            for (DagNode dagNode : visitable.getSinks()) {
                sb.append(dagNode.getName())
                        .append(MermaidVisvitor.INSTANCE.SEPERATOR)
                        .append("END")
                        .append(MermaidVisvitor.INSTANCE.LF);
            }
        }

        sb.append(MermaidVisvitor.INSTANCE.end(visitable));
        return sb.toString();
    }

    @Override
    public String visit(DefaultDagEdge visitable) {
        return MermaidVisvitor.INSTANCE.visit(visitable);
    }
}
