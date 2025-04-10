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
package cn.sliew.carp.framework.log.realtime.service.impl;

import cn.sliew.carp.framework.log.realtime.service.StreamLogService;
import cn.sliew.carp.framework.log.realtime.service.dto.StreamLogLine;
import cn.sliew.carp.framework.log.realtime.storage.StreamLogLines;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class StreamLogServiceImpl implements StreamLogService {

    @Autowired
    private StreamLogLines logLines;

    @Override
    public void info(String streamKey, String message, Object... params) {
        log(streamKey, Level.INFO, message, params);
    }

    @Override
    public void info(String streamKey, Logger logger, String message, Object... params) {
        logger.info(message, params);
        info(streamKey, message, params);
    }

    @Override
    public void warn(String streamKey, String message, Object... params) {
        log(streamKey, Level.WARN, message, params);
    }

    @Override
    public void warn(String streamKey, Logger logger, String message, Object... params) {
        logger.warn(message, params);
        warn(streamKey, message, params);
    }

    @Override
    public void error(String streamKey, String message, Object... params) {
        log(streamKey, Level.ERROR, message, params);
    }

    @Override
    public void error(String streamKey, Logger logger, String message, Object... params) {
        logger.error(message, params);
        error(streamKey, message, params);
    }

    private void log(String streamKey, Level level, String message, Object... params) {
        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(message, params);
        logLines.log(streamKey,
                StreamLogLine.builder()
                        .level(level)
                        .message(formattingTuple.getMessage())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @Override
    public void persist(String streamKey) {
        logLines.persist(streamKey);
    }
}
