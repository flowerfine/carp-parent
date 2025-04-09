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

public class MermaidVisvitor implements Visitor {

    public static final MermaidVisvitor INSTANCE = new MermaidVisvitor();

    private String LF = ";\n";
    private String SEPERATOR = " -----> ";

    private MermaidVisvitor() {
    }

    @Override
    public String start(DAG<DagNode> visitable) {
        StringBuilder sb = new StringBuilder();
        sb.append("flowchart TD;").append(LF);
        return sb.toString();
    }

    @Override
    public String end(DAG<DagNode> visitable) {
        return "";
    }

    @Override
    public String visit(DefaultDagEdge visitable) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s(%s)", visitable.getSource().getKey(), visitable.getSource().getName()))
                .append(SEPERATOR)
                .append(String.format("%s(%s)", visitable.getTarget().getKey(), visitable.getTarget().getName()))
                .append(LF);
        return sb.toString();
    }
}
