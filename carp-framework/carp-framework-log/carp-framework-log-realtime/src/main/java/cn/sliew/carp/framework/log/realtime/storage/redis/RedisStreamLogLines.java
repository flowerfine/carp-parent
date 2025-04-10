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
package cn.sliew.carp.framework.log.realtime.storage.redis;

import cn.hutool.core.date.DateUtil;
import cn.sliew.carp.framework.common.nio.FileUtil;
import cn.sliew.carp.framework.log.realtime.service.dto.StreamLogLine;
import cn.sliew.carp.framework.log.realtime.storage.StreamLogLines;
import cn.sliew.carp.framework.log.realtime.util.StreamLogUtil;
import cn.sliew.milky.common.util.JacksonUtil;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisStreamLogLines implements StreamLogLines {

    public static final String STREAM_LOG_KEY = "line";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void log(String streamKey, StreamLogLine logLine) {
        redisTemplate.opsForStream().add(streamKey,
                ImmutableMap.of(STREAM_LOG_KEY, JacksonUtil.toJsonString(logLine)));
        redisTemplate.expire(streamKey, 10, TimeUnit.DAYS);
    }

    @Override
    public List<StreamLogLine> getLines(String streamKey) {
        List<ObjectRecord<String, String>> records = redisTemplate.opsForStream()
                .read(String.class, StreamOffset.fromStart(streamKey));

        return Optional.ofNullable(records)
                .map(lines ->
                        lines.stream().map(
                                        record ->
                                                JacksonUtil.parseJsonString(
                                                        record.getValue(),
                                                        StreamLogLine.class))
                                .toList())
                .orElse(Collections.emptyList());
    }

    @Override
    public String persist(String streamKey) {
        redisTemplate.delete(streamKey);
        String date = DateUtil.format(new Date(), "yyyy/MM/dd/HH/mm");
        String objectName = String.format("/tmp/carp/stream_log/%s/%s.txt", date, streamKey);
        Path file = null;
        try {
            file = FileUtil.createFile(Path.of(objectName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<StreamLogLine> lines = getLines(streamKey);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file.toFile());
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
            for (StreamLogLine line : lines) {
                writeLine(bufferedOutputStream, line);
                bufferedOutputStream.flush();
            }
        } catch (IOException e) {

        }
        return objectName;
    }

    private void writeLine(OutputStream outputStream, StreamLogLine line) throws IOException {
        String lineStr = StreamLogUtil.format(line);
        outputStream.write(lineStr.getBytes(StandardCharsets.UTF_8));
        outputStream.write('\n');
    }
}
