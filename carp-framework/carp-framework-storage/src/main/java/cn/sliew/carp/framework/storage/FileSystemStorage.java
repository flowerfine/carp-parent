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
package cn.sliew.carp.framework.storage;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FileSystemStorage implements FileStorage {

    private final FileSystem fs;

    @Override
    public boolean support(URI uri) {
        return StringUtils.startsWithIgnoreCase(uri.toString(), fs.getUri().toString());
    }

    @Override
    public URI getUri(String path) throws IOException {
        Path fileSystemPath = toFileSystemPath(path);
        return fileSystemPath.toUri();
    }

    @Override
    public boolean exists(String path) throws IOException {
        return fs.exists(toFileSystemPath(path));
    }

    @Override
    public List<FileInfo> list(String path) throws IOException {
        if (exists(path) == false) {
            return Collections.emptyList();
        }
        FileStatus[] fileStatuses = fs.listStatus(toFileSystemPath(path));
        return Arrays.stream(fileStatuses)
                .map(this::toFileInfo)
                .sorted(Comparator.comparing(FileInfo::getName))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FileInfo> get(String path) throws IOException {
        if (exists(path) == false) {
            return Optional.empty();
        }
        FileStatus fileStatus = fs.getFileStatus(toFileSystemPath(path));
        return Optional.of(toFileInfo(fileStatus));
    }

    @Override
    public Optional<byte[]> getData(String path) throws IOException {
        Optional<InputStream> stream = getStream(path);
        if (!stream.isPresent()) {
            return Optional.empty();
        }
        try (InputStream inputStream = stream.get();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            IOUtils.copyBytes(inputStream, outputStream, 1024);
            return Optional.of(outputStream.toByteArray());
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public Optional<byte[]> getData(URI uri) throws IOException {
        Optional<InputStream> stream = getStream(uri);
        if (!stream.isPresent()) {
            return Optional.empty();
        }
        try (InputStream inputStream = stream.get();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            IOUtils.copyBytes(inputStream, outputStream, 1024);
            return Optional.of(outputStream.toByteArray());
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public Optional<InputStream> getStream(String path) throws IOException {
        if (exists(path) == false) {
            return Optional.empty();
        }
        return Optional.of(fs.open(toFileSystemPath(path)));
    }

    @Override
    public Optional<InputStream> getStream(URI uri) throws IOException {
        Path path = new Path(uri);
        if (fs.exists(path)) {
            return Optional.empty();
        }
        return Optional.of(fs.open(path));
    }

    @Override
    public FileInfo putData(String path, byte[] data) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            return putInputStream(path, inputStream);
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public FileInfo putInputStream(String path, InputStream inputStream) throws IOException {
        if (exists(path)) {
            throw new FileAlreadyExistsException(path);
        }
        Path filePath = toFileSystemPath(path);
        if (fs.exists(filePath.getParent()) == false) {
            fs.mkdirs(filePath.getParent());
        }
        try (FSDataOutputStream outputStream = fs.create(filePath, false)) {
            IOUtils.copyBytes(inputStream, outputStream, 1024);
        } catch (IOException e) {
            throw e;
        }
        return get(path).orElseThrow();
    }

    @Override
    public boolean delete(String path) throws IOException {
        if (exists(path) == false) {
            return true;
        }

        return fs.delete(toFileSystemPath(path), true);
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        return fs.delete(new Path(uri), true);
    }

    private Path toFileSystemPath(String path) {
        return new Path(fs.getWorkingDirectory(), formatPath(path));
    }

    private FileInfo toFileInfo(FileStatus fileStatus) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setUri(fileStatus.getPath().toUri());
        fileInfo.setPath(fileStatus.getPath().toString());
        fileInfo.setName(fileStatus.getPath().getName());
        fileInfo.setDir(fileInfo.isDir());
        fileInfo.setUpdateTime(new Date(fileStatus.getModificationTime()));
        return fileInfo;
    }

    private String formatPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        if (path.charAt(0) != '/') {
            return '/' + path;
        }
        return path;
    }
}
