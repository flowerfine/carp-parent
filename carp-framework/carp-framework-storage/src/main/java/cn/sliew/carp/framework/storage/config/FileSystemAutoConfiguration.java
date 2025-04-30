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
package cn.sliew.carp.framework.storage.config;

import cn.sliew.carp.framework.storage.FileSystemType;
import cn.sliew.carp.framework.storage.util.HadoopUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.aliyun.oss.AliyunOSSFileSystem;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Configuration
public class FileSystemAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = StorageConfigProperties.DEFAULT_STORAGE_CONFIG_PREFIX, value = "type", matchIfMissing = false)
    public FileSystem hadoopFileSystem(StorageConfigProperties properties) throws IOException, URISyntaxException {
        switch (properties.getType()) {
            case "local":
                return createLocalFileSystem(properties.getLocal());
            case "s3":
                return createS3FileSystem(properties.getS3());
            case "oss":
                return createOssFileSystem(properties.getOss());
            case "hdfs":
                return createHdfsFileSystem(properties.getHdfs());
            default:
                throw new RuntimeException("Unsupported storage type: " + properties.getType());
        }
    }

    private FileSystem createLocalFileSystem(LocalConfigProperties properties) throws IOException {
        org.apache.hadoop.conf.Configuration conf = HadoopUtil.getHadoopConfiguration(null);
        FileSystem fileSystem = FileSystem.getLocal(conf);
        setFsWorkingDirectory(fileSystem, properties.getPath());
        return fileSystem;
    }

    private FileSystem createS3FileSystem(S3ConfigProperties properties) throws URISyntaxException, IOException {
        org.apache.hadoop.conf.Configuration conf = HadoopUtil.getHadoopConfiguration(null);
        conf.set("fs.s3a.endpoint", properties.getEndpoint());
        conf.set("fs.s3a.access.key", properties.getAccessKey());
        conf.set("fs.s3a.secret.key", properties.getSecretKey());
        conf.setBoolean("fs.s3a.path.style.access", true);
        URI uri = new URI(FileSystemType.S3.getSchema() + properties.getBucket());
        return FileSystem.get(uri, conf);
    }

    private FileSystem createOssFileSystem(OSSConfigProperties properties) throws IOException, URISyntaxException {
        org.apache.hadoop.conf.Configuration conf = HadoopUtil.getHadoopConfiguration(null);
        conf.set("fs.oss.endpoint", properties.getEndpoint());
        conf.set("fs.oss.accessKeyId", properties.getAccessKey());
        conf.set("fs.oss.accessKeySecret", properties.getSecretKey());
        URI uri = new URI(FileSystemType.OSS.getSchema() + properties.getBucket());
        AliyunOSSFileSystem aliyunOSSFileSystem = new AliyunOSSFileSystem();
        aliyunOSSFileSystem.initialize(uri, conf);
        return aliyunOSSFileSystem;
    }

    private FileSystem createHdfsFileSystem(HdfsConfigProperties properties) throws URISyntaxException, IOException {
        org.apache.hadoop.conf.Configuration conf = HadoopUtil.getHadoopConfiguration(null);
        if (StringUtils.hasText(properties.getDefaultFS())) {
            conf.set("fs.defaultFS", properties.getDefaultFS());
        }
        return FileSystem.get(conf);
    }

    private void setFsWorkingDirectory(FileSystem fileSystem, String workingDirectory) {
        if (workingDirectory == null) {
            log.warn("Null working directory");
            return;
        }
        String path = null;
        try {
            URI uri = new URI(workingDirectory);
            path = uri.getRawPath();
        } catch (Exception e) {
            log.error("Error parsing working directory {}", workingDirectory);
        }
        if (path != null) {
            log.info("Set working directory to {}", path);
            fileSystem.setWorkingDirectory(new Path(path));
        }
    }

}
