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
package cn.sliew.carp.framework.spring.util;

import cn.hutool.core.lang.UUID;
import cn.sliew.carp.framework.common.nio.FileUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class SystemUtil implements InitializingBean, DisposableBean {

    private static final String LOCAL_FS_STORAGE_DIR_NAME = "storage";
    private static String workspace;

    @Value("${app.workspace:/tmp/carp}")
    public void setWorkspace(String workspace) {
        SystemUtil.workspace = workspace;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        FileUtil.deleteDir(Path.of(workspace));
    }

    @Override
    public void destroy() throws Exception {
        FileUtil.deleteDir(Path.of(workspace));
    }

    public static Path getWorkspace() throws IOException {
        return FileUtil.createDir(Path.of(workspace));
    }

    public static Path getRandomWorkspace() throws IOException {
        return FileUtil.createDir(getWorkspace().resolve(UUID.randomUUID().toString(true)));
    }

    public static Path getLocalStorageDir() throws IOException {
        return FileUtil.createDir(getWorkspace().resolve(LOCAL_FS_STORAGE_DIR_NAME));
    }

    public static Path getPluginsPath() throws IOException {
        return FileUtil.createDir(getWorkspace().resolve("plugins"));
    }

    public static Path getDataSourcePluginsPath() throws IOException {
        return FileUtil.createDir(getPluginsPath().resolve("datasource"));
    }

    public static Path getKubeConfigPath() throws IOException {
        return FileUtil.createDir(getWorkspace().resolve("kubeconfig"));
    }

}
