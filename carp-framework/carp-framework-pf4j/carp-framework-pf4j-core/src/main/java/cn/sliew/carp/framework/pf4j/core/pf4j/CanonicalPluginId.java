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
package cn.sliew.carp.framework.pf4j.core.pf4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of the Carp canonical plugin ID format.
 * <p>
 * The namespace is dual-purposed: It allows multiple organizations to have the same plugin ID, but not clash in
 * an installation. It can also be used for authorization for plugin publishing, writes and so-on.
 * <p>
 * For example, official Carp plugins might have an `cn.sliew.carp` namespace, and authz would require the
 * ADMIN role to write to. Alternatively, `cn.sliew.streaming.platform.delivery-engineering` would require a user
 * to be in the Delivery Engineering team. This mapping of namespace to authorization would need to be enabled via
 * configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CanonicalPluginId {

    private static Pattern pattern = Pattern.compile("^(?<namespace>[\\w\\-.])+\\.(?<id>[\\w\\-]+)$");

    /**
     * An organizationally-unique namespace.
     */
    private String namespace;

    /**
     * The plugin ID. This must be unique within a namespace.
     */
    private String id;

    /**
     * Returns a boolean of whether or not the given {@param pluginId} is correctly formed.
     *
     * @param pluginId The plugin ID to validate.
     * @return boolean
     */
    public static boolean isValid(String pluginId) {
        return pattern.matcher(pluginId).matches();
    }

    /**
     * Validates the given {@param pluginId}, throwing an exception if the ID is malformed.
     *
     * @param pluginId The plugin ID to validate.
     */
    public static void validate(String pluginId) {
        if (!isValid(pluginId)) {
            throw new MalformedPluginIdException(pluginId);
        }
    }

    /**
     * @param pluginId
     * @return
     */
    public static CanonicalPluginId parse(String pluginId) {
        Matcher matcher = pattern.matcher(pluginId);
        if (matcher.matches()) {
            return new CanonicalPluginId(matcher.group("namespace"), matcher.group("id"));
        }
        return null;
    }

    /**
     * Thrown when a plugin ID is malformed.
     */
    public static class MalformedPluginIdException extends RuntimeException {

        /**
         * Constructor.
         *
         * @param pluginId
         */
        public MalformedPluginIdException(String pluginId) {
            super(String.format("Plugin '%s' does not conform to Carp's Canonical Plugin ID. " +
                    "Canonical IDs must follow a '{namespace}.{pluginId}' format (%s).", pluginId, pattern));
        }
    }
}
