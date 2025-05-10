package org.example;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import java.util.*;


public class OrderBook {
    interface Command {}

    public static class NewOrder implements Command {}
    public static class AssignOrder implements Command {
        public final ActorRef<Response> replyTo;
        public AssignOrder(ActorRef<Response> replyTo) { this.replyTo = replyTo; }
    }
    public static class Response {
        public final Integer orderId;
        public Response(Integer orderId) { this.orderId = orderId; }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> {
            List<Integer> orders = new ArrayList<>();
            int[] counter = {1};

            context.getSystem().scheduler().scheduleAtFixedRate(
                    Duration.ofSeconds(15), Duration.ofSeconds(15),
                    () -> context.getSelf().tell(new NewOrder()),
                    context.getExecutionContext()
            );

            return Behaviors.receive(Command.class)
                    .onMessage(NewOrder.class, msg -> {
                        orders.add(counter[0]++);
                        context.getLog().info("Neuer Auftrag eingegangen: {}", counter[0] - 1);
                        return Behaviors.same();
                    })
                    .onMessage(AssignOrder.class, msg -> {
                        if (!orders.isEmpty()) {
                            Integer id = orders.remove(0);
                            msg.replyTo.tell(new Response(id));
                        } else {
                            context.getSystem().scheduler().scheduleOnce(
                                    Duration.ofSeconds(10),
                                    () -> context.getSelf().tell(msg),
                                    context.getExecutionContext()
                            );
                        }
                        return Behaviors.same();
                    })
                    .build();
        });
    }
}
