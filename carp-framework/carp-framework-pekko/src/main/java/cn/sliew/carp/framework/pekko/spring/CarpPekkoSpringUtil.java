package cn.sliew.carp.framework.pekko.spring;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Props;

public enum CarpPekkoSpringUtil {
    ;

    public static ActorRef createActorRef(ActorSystem actorSystem, String actorBeanName, Object... args) {
        Props props = CarpPekkoSpringExtension.SPRING_EXTENSION_PROVIDER.get(actorSystem)
                .props(actorBeanName, args);
        return actorSystem.actorOf(props, actorBeanName);
    }

}
