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

import java.util.Objects;

public class PlantUMLVisitor implements Visitor {

    public static final PlantUMLVisitor INSTANCE = new PlantUMLVisitor();

    private char LF = '\n';
    private String SEPERATOR = " --> ";
    private String START_END = "[*]";

    private PlantUMLVisitor() {
    }

    @Override
    public String start(DAG<DagNode> visitable) {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml").append(LF);
        for (DagNode dagNode : visitable.getSources()) {
            sb.append(START_END)
                    .append(SEPERATOR)
                    .append(dagNode.getName())
                    .append(LF);
        }
        return sb.toString();
    }

    @Override
    public String end(DAG<DagNode> visitable) {
        StringBuilder sb = new StringBuilder();
        for (DagNode dagNode : visitable.getSinks()) {
            sb.append(dagNode.getName())
                    .append(SEPERATOR)
                    .append(START_END)
                    .append(LF);
        }
        sb.append("@enduml");
        return sb.toString();
    }

    @Override
    public String visit(DefaultDagEdge edge) {
        StringBuilder sb = new StringBuilder();
        sb.append(edge.getSource().getName())
                .append(SEPERATOR)
                .append(edge.getTarget().getName());
        if (Objects.nonNull(edge.getData()) && edge.getData() instanceof String string) {
            sb.append(" : ").append(string);
        }
        sb.append(LF);
        return sb.toString();
    }
}
