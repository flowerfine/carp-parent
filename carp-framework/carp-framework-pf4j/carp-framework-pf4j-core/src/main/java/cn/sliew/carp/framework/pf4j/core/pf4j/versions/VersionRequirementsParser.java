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
package cn.sliew.carp.framework.pf4j.core.pf4j.versions;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides utility methods for parsing version requirements values from and to {@link VersionRequirements}.
 * <p>
 * Version requirements are in the format of "{service}{constraint}", where:
 * <p>
 * - `service` is the service name that is supported by a plugin
 * - `constraint` is a semVer expression to be constrained ( >=1.5.0 , >=1.0.0 & <2.0.0)
 */
public class VersionRequirementsParser {

    private static final Pattern SUPPORTS_PATTERN = Pattern.compile(
            "^(?<service>[\\w\\-]+)(?<constraint>.*[><=]{1,2}[0-9]+\\.[0-9]+\\.[0-9]+.*)$"
    );
    private static final Version CONSTRAINT_VALIDATOR = Version.valueOf("0.0.0");

    private static final String SUPPORTS_PATTERN_SERVICE_GROUP = "service";
    private static final String SUPPORTS_PATTERN_CONSTRAINT_GROUP = "constraint";

    /**
     * Parse a single version.
     */
    public static VersionRequirements parse(String version) {
        Matcher matcher = SUPPORTS_PATTERN.matcher(version);
        if (!matcher.matches()) {
            throw new InvalidPluginVersionRequirementException(version);
        }
        // we use semver to validate that the constraint is valid.
        try {
            CONSTRAINT_VALIDATOR.satisfies(matcher.group(SUPPORTS_PATTERN_CONSTRAINT_GROUP));
        } catch (ParseException e) {
            throw new InvalidPluginVersionRequirementException(version);
        }

        return new VersionRequirements(
                matcher.group(SUPPORTS_PATTERN_SERVICE_GROUP),
                matcher.group(SUPPORTS_PATTERN_CONSTRAINT_GROUP)
        );
    }

    /**
     * Parse a list of comma-delimited versions.
     */
    public static List<VersionRequirements> parseAll(String version) {
        return Arrays.stream(version.split(","))
                .map(String::trim)
                .map(VersionRequirementsParser::parse)
                .collect(Collectors.toList());
    }

    /**
     * Convert a list of {@link VersionRequirements} into a string.
     */
    public static String stringify(List<VersionRequirements> requirements) {
        return requirements.stream()
                .map(VersionRequirements::toString)
                .collect(Collectors.joining(","));
    }

    /**
     * Version constraint requirements for a plugin release.
     */
    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class VersionRequirements {
        private final String service;
        private final String constraint;

        @Override
        public String toString() {
            return service + constraint;
        }
    }

    /**
     * Thrown when a given version requirement is invalid.
     */
    public static class InvalidPluginVersionRequirementException extends RuntimeException {
        public InvalidPluginVersionRequirementException(String version) {
            super("The provided version requirement '" +
                    version +
                    "' is not valid: It must conform a valid semantic version expression");
        }
    }
}
