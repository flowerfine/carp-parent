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
package cn.sliew.carp.framework.queue.kekio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class AbstractLifecycle implements SmartLifecycle {

    private AtomicBoolean running = new AtomicBoolean(false);
    private ReentrantLock lifecycleLock = new ReentrantLock();

    @Override
    public void start() {
        this.lifecycleLock.lock();
        try {
            if (!isRunning()) {
                doStart();
                running.compareAndSet(false, true);
            }
        } catch (Exception e) {
            log.error("Start queue error", e);
        } finally {
            this.lifecycleLock.unlock();
        }
    }

    protected abstract void doStart() throws Exception;

    @Override
    public void stop() {
        if (isRunning()) {
            this.lifecycleLock.lock();
            try {
                doStop();
                running.compareAndSet(true, false);
            } catch (Exception e) {
                log.error("Stop queue error", e);
            } finally {
                this.lifecycleLock.unlock();
            }
        }
    }

    protected abstract void doStop()  throws Exception;

    @Override
    public boolean isRunning() {
        return running.get();
    }

}
