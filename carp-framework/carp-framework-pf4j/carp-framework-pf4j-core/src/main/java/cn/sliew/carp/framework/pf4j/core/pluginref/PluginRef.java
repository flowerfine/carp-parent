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
package cn.sliew.carp.framework.pf4j.core.pluginref;

import cn.sliew.milky.common.util.JacksonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.pf4j.Plugin;
import org.pf4j.PluginDescriptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Getter
@EqualsAndHashCode
public class PluginRef {

    /**
     * The PluginRef file extension name.
     */
    public static final String EXTENSION = ".plugin-ref";

    /**
     * The path to the concrete {@link Plugin} implementation.
     * <p>
     * This path will be used to locate a {@link PluginDescriptor} for the {@link Plugin}.
     */
    private final String pluginPath;

    /**
     * A list of directories containing compiled class-files for the {@link Plugin}.
     * <p>
     * These directories should match the build output directories in the {@link Plugin}'s
     * development workspace or IDE.
     */
    private final List<String> classesDirs;

    /**
     * A list of directories containing jars scoped to the {@link Plugin}.
     * <p>
     * These jars should be referenced from the {@link Plugin}'s development workspace or IDE.
     */
    private final List<String> libsDirs;

    /**
     * A {@link PluginRef} is a type of {@link Plugin} that exists as a pointer to an actual Plugin for use
     * during Plugin development.
     * <p>
     * This class includes helper methods via its companion object to support this as a JSON
     * document with a .plugin-ref extension.
     * <p>
     * The intention of {@link PluginRef} is to allow a runtime experience similar to dropping a fully
     * packaged plugin into the host application, without requiring the packaging and deployment
     * step (aside from a one time generation and copy/link of the {@link PluginRef} file).
     *
     * @param pluginPath  The path to the concrete {@link Plugin} implementation.
     * @param classesDirs A list of directories containing compiled class-files for the {@link Plugin}.
     * @param libsDirs    A list of directories containing jars scoped to the {@link Plugin}.
     */
    public PluginRef(String pluginPath, List<String> classesDirs, List<String> libsDirs) {
        this.pluginPath = pluginPath;
        this.classesDirs = classesDirs;
        this.libsDirs = libsDirs;
    }

    /**
     * The path to the plugin ref file.
     */
    @JsonIgnore
    public Path getRefPath() {
        return Paths.get(pluginPath);
    }

    /**
     * Returns whether or not the provided {@link Path} is a valid {@link PluginRef}.
     */
    public static boolean isPluginRef(Path path) {
        return path != null &&
                Files.isRegularFile(path) &&
                path.toString().toLowerCase().endsWith(EXTENSION);
    }

    /**
     * Loads the given {@link Path} as a {@link PluginRef}.
     */
    public static PluginRef loadPluginRef(Path path) {
        if (!isPluginRef(path)) {
            throw new InvalidPluginRefException(path);
        }

        try {
            PluginRef ref = JacksonUtil.getMapper().readValue(path.toFile(), PluginRef.class);
            if (ref.getRefPath().isAbsolute()) {
                return ref;
            } else {
                return new PluginRef(
                        path.getParent().resolve(ref.getRefPath()).toAbsolutePath().toString(),
                        ref.getClassesDirs(),
                        ref.getLibsDirs()
                );
            }
        } catch (IOException ex) {
            throw new MalformedPluginRefException(path, ex);
        }
    }

    /**
     * Thrown when a given plugin ref cannot be found at the given path.
     */
    public static class InvalidPluginRefException extends RuntimeException {
        public InvalidPluginRefException(Path path) {
            super(path != null ? path.getFileName() + " is not a plugin-ref file" : "Null path passed as plugin-ref");
        }
    }

    /**
     * Thrown when a valid plugin ref path contains a malformed body.
     */
    public static class MalformedPluginRefException extends RuntimeException {
        public MalformedPluginRefException(Path path, Throwable cause) {
            super(path.getFileName() + " is not a valid plugin-ref", cause);
        }
    }
}
