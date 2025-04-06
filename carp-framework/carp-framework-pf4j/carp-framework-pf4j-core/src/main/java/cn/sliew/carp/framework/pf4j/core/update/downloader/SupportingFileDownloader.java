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

import org.pf4j.update.FileDownloader;

import java.net.URL;

/**
 * A {@link FileDownloader} that can inspect a URL prior to downloading the provided file.
 * <p>
 * Custom FileDownloaders should use this interface if there is a chance that an {@link org.pf4j.update.UpdateRepository}
 * may need to download plugin binaries from different locations with varying download strategies. These
 * FileDownloaders are used automatically in the {@link CompositeFileDownloader}.
 */
public interface SupportingFileDownloader extends FileDownloader {

    /**
     * @return True if {@code url} is supported by this {@link FileDownloader}.
     */
    boolean supports(URL url);
}
