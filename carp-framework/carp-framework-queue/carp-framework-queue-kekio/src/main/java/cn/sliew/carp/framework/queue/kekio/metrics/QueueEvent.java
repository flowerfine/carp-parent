package cn.sliew.carp.framework.queue.kekio.metrics;

import cn.sliew.carp.framework.queue.kekio.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;

public class QueueEvent {

    public static final QueueEvent QueuePolled = new QueueEvent();
    public static final QueueEvent RetryPolled = new QueueEvent();

    public static final QueueEvent MessageAcknowledged = new QueueEvent();
    public static final QueueEvent MessageRetried = new QueueEvent();

    public static final QueueEvent MessageDead = new QueueEvent();
    public static final QueueEvent LockFailed = new QueueEvent();
    public static final QueueEvent NoHandlerCapacity = new QueueEvent();

    @Data
    public static class PayloadQueueEvent extends QueueEvent {
        private Message payload;
    }

    @Data
    @AllArgsConstructor
    public static class MessagePushed extends QueueEvent {
        private Message payload;
    }

    @Data
    @AllArgsConstructor
    public static class MessageProcessing extends QueueEvent {
        private Message payload;
        private Duration lag;

        public MessageProcessing(Message payload, Instant scheduledTime, Instant now) {
            this(payload, Duration.between(scheduledTime, now));
        }
    }

    @Data
    @AllArgsConstructor
    public static class MessageDuplicate extends QueueEvent {
        private Message payload;
    }

    @Data
    @AllArgsConstructor
    public static class MessageRescheduled extends QueueEvent {
        private Message payload;
    }

    @Data
    @AllArgsConstructor
    public static class MessageNotFound extends QueueEvent {
        private Message payload;
    }

    @Data
    @AllArgsConstructor
    public static class HandlerThrewError extends QueueEvent {
        private Message payload;
    }
}
