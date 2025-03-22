package cn.sliew.carp.framework.socketio.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CarpSocketIoNamespace {

    String value();
}
