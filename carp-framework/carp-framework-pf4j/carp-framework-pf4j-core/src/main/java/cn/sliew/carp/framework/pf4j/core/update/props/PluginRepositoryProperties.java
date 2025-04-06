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
package cn.sliew.carp.framework.pf4j.core.update.props;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.SneakyThrows;
import org.pf4j.update.FileDownloader;
import org.pf4j.update.UpdateRepository;

import java.net.URL;

/**
 * Definition of a single {@link UpdateRepository}.
 */
@Data
public class PluginRepositoryProperties {
    /**
     * Flag to determine if repository is enabled.
     */
    private boolean enabled = true;

    /**
     * The base URL to the repository.
     */
    private String url;

    /**
     * Configuration for an optional override of {@link FileDownloader}.
     */
    @Nullable
    private FileDownloaderProperties fileDownloader;

    @SneakyThrows
    public URL getUrl() {
        return new URL(url);
    }
}
