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
package cn.sliew.carp.framework.pf4j.core.update.repository;

import org.pf4j.update.DefaultUpdateRepository;
import org.pf4j.update.FileDownloader;
import org.pf4j.update.FileVerifier;
import org.pf4j.update.SimpleFileDownloader;
import org.pf4j.update.verifier.CompoundVerifier;

import java.net.URL;

/**
 * PF4J forces extensible for configurability. This class just makes it a little more reasonable to bring your own
 * file downloader and verifier to a particular repository.
 */
public class ConfigurableUpdateRepository extends DefaultUpdateRepository {

    private final FileDownloader downloader;
    private final FileVerifier verifier;

    public ConfigurableUpdateRepository(String id, URL url) {
        this(id, url, new SimpleFileDownloader(), new CompoundVerifier());
    }

    public ConfigurableUpdateRepository(String id, URL url, FileDownloader downloader, FileVerifier verifier) {
        super(id, url);
        this.downloader = downloader;
        this.verifier = verifier;
    }

    @Override
    public FileDownloader getFileDownloader() {
        return downloader;
    }

    @Override
    public FileVerifier getFileVerifier() {
        return verifier;
    }
}
