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
package cn.sliew.carp.framework.queue.kekio.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;

/**
 * An attribute representing the number of times a message has been retried
 * due to ack timeouts.
 */
@Data
@AllArgsConstructor
@JsonTypeName("ackAttempts")
public class AckAttemptsAttribute implements Attribute {
    @Serial
    private static final long serialVersionUID = 5072537520379932042L;

    private int ackAttempts;

    public AckAttemptsAttribute() {
        this(0);
    }
}
