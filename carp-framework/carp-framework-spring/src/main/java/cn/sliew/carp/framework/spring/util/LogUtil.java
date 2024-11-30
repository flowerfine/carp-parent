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

import org.slf4j.Logger;
import org.slf4j.event.Level;

public enum LogUtil {
    ;

    public static void log(Logger logger, Level level, String message, Object... params) {
        switch (level) {
            case TRACE:
                trace(logger, message, params);
                break;
            case DEBUG:
                debug(logger, message, params);
                break;
            case INFO:
                info(logger, message, params);
                break;
            case WARN:
                warn(logger, message, params);
                break;
            case ERROR:
                error(logger, message, params);
                break;
            default:
        }
    }

    public static void trace(Logger logger, String message, Object... params) {
        if (logger.isTraceEnabled()) {
            doLog(logger, Level.TRACE, message, params);
        }
    }

    public static void debug(Logger logger, String message, Object... params) {
        if (logger.isDebugEnabled()) {
            doLog(logger, Level.DEBUG, message, params);
        }
    }

    public static void info(Logger logger, String message, Object... params) {
        if (logger.isInfoEnabled()) {
            doLog(logger, Level.INFO, message, params);
        }
    }

    public static void warn(Logger logger, String message, Object... params) {
        if (logger.isWarnEnabled()) {
            doLog(logger, Level.WARN, message, params);
        }
    }

    public static void error(Logger logger, String message, Object... params) {
        if (logger.isErrorEnabled()) {
            doLog(logger, Level.ERROR, message, params);
        }
    }

    private static void doLog(Logger logger, Level level, String message, Object... params) {
        switch (level) {
            case TRACE:
                logger.trace(message, params);
                break;
            case DEBUG:
                logger.debug(message, params);
                break;
            case INFO:
                logger.info(message, params);
                break;
            case WARN:
                logger.warn(message, params);
                break;
            case ERROR:
                logger.error(message, params);
                break;
            default:
        }
    }
}
