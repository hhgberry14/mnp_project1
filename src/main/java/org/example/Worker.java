package org.example;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import java.time.Duration;
import java.util.*;

public class Worker {
    interface Command {}

    public static class BuildCarBody implements Command {
        public final int orderId;
        public final ActorRef<ProductionLine.Command> replyTo;
        public BuildCarBody(int orderId, ActorRef<ProductionLine.Command> replyTo) {
            this.orderId = orderId;
            this.replyTo = replyTo;
        }
    }

    public static class ProvideRequests implements Command {
        public final int orderId;
        public final List<String> requests;
        public ProvideRequests(int orderId, List<String> requests) {
            this.orderId = orderId;
            this.requests = requests;
        }
    }

    public static Behavior<Command> create(String name, ActorRef<LocalStorage.Command> storage) {
        return Behaviors.setup(context -> Behaviors.receive(Command.class)
                .onMessage(BuildCarBody.class, msg -> {
                    context.getLog().info("{} beginnt Karosseriebau für Auftrag {}", name, msg.orderId);
                    int duration = new Random().nextInt(6) + 5;
                    context.getSystem().scheduler().scheduleOnce(Duration.ofSeconds(duration),
                            () -> storage.tell(new LocalStorage.RequestSpecials(msg.orderId, context.getSelf())),
                            context.getExecutionContext()
                    );
                    return Behaviors.same();
                })
                .onMessage(ProvideRequests.class, msg -> {
                    context.getLog().info("{} verbaut Spezialwünsche {} für Auftrag {}", name, msg.requests, msg.orderId);
                    int duration = new Random().nextInt(6) + 5;
                    context.getSystem().scheduler().scheduleOnce(Duration.ofSeconds(duration),
                            () -> context.getSelf().tell(new Done(msg.orderId)),
                            context.getExecutionContext()
                    );
                    return Behaviors.same();
                })
                .onMessage(Done.class, msg -> {
                    context.getLog().info("{} hat Auftrag {} abgeschlossen", name, msg.orderId);
                    return Behaviors.stopped();
                })
                .build()
        );
    }

    public static class Done implements Command {
        public final int orderId;
        public Done(int orderId) { this.orderId = orderId; }
    }
}