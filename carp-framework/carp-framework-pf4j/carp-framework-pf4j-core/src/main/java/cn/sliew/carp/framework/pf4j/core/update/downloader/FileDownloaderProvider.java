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
package cn.sliew.carp.framework.pf4j.core.update.downloader;

import cn.sliew.carp.framework.pf4j.core.config.Configurable;
import cn.sliew.carp.framework.pf4j.core.update.props.FileDownloaderProperties;
import cn.sliew.milky.common.util.JacksonUtil;
import org.pf4j.update.FileDownloader;
import org.pf4j.update.UpdateRepository;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * Provides a {@link FileDownloader} based on configuration.
 */
public class FileDownloaderProvider {

    private final CompositeFileDownloader compositeFileDownloader;

    public FileDownloaderProvider(CompositeFileDownloader compositeFileDownloader) {
        this.compositeFileDownloader = compositeFileDownloader;
    }

    /**
     * Get a {@link FileDownloader} for the {@link UpdateRepository}.
     */
    public FileDownloader get(FileDownloaderProperties fileDownloaderProperties) {
        if (fileDownloaderProperties == null || fileDownloaderProperties.getClassName() == null) {
            return compositeFileDownloader;
        }

        try {
            Class<?> downloaderClass = getClass().getClassLoader().loadClass(fileDownloaderProperties.getClassName());

            if (!FileDownloader.class.isAssignableFrom(downloaderClass)) {
                throw new RuntimeException(
                        "Configured fileDownloader exists but does not implement FileDownloader: " + downloaderClass.getCanonicalName()
                );
            }

            Configurable configurable = downloaderClass.getAnnotation(Configurable.class);
            if (configurable != null) {
                Object config = JacksonUtil.getMapper().convertValue(fileDownloaderProperties.getConfig(), configurable.value());

                Constructor<?> ctor = Arrays.stream(downloaderClass.getConstructors())
                        .filter(c -> c.getParameterCount() == 1 &&
                                c.getParameterTypes()[0].isAssignableFrom(configurable.value()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(
                                "Could not find matching constructor on file downloader for injecting config"
                        ));

                return (FileDownloader) ctor.newInstance(config);
            } else {
                return (FileDownloader) downloaderClass.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate FileDownloader", e);
        }
    }
}
