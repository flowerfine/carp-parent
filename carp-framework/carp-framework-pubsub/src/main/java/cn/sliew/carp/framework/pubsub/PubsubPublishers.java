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
package cn.sliew.carp.framework.pubsub;

import cn.sliew.carp.framework.pubsub.model.PubsubPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PubsubPublishers {

    private List<PubsubPublisher> publishers = new ArrayList<>();

    public void putAll(List<PubsubPublisher> newEntries) {
        publishers.addAll(newEntries);
    }

    public List<PubsubPublisher> getAll() {
        return Collections.unmodifiableList(publishers);
    }

    public List<PubsubPublisher> withType(String system) {
        return publishers.stream()
                .filter(publisher -> Objects.equals(publisher.getSystem(), system))
                .collect(Collectors.toList());
    }
}
