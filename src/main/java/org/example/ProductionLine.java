package org.example;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import java.util.*;

public class ProductionLine {
    interface Command {}

    public static class Start implements Command {}
    public static class WorkerDone implements Command {
        public final int orderId;
        public WorkerDone(int orderId) { this.orderId = orderId; }
    }

    public static class AssignOrder implements Command {
        public final int orderId;
        public AssignOrder(int orderId) { this.orderId = orderId; }
    }

    public static Behavior<Command> create(String name, ActorRef<OrderBook.Command> orderBook,
                                           List<ActorRef<Worker.Command>> workers,
                                           ActorRef<LocalStorage.Command> storage) {
        return Behaviors.setup(context -> {
            ActorRef<OrderBook.Response> adapter = context.messageAdapter(OrderBook.Response.class, res -> new AssignOrder(res.orderId));
            orderBook.tell(new OrderBook.AssignOrder(adapter));

            return Behaviors.receive(Command.class)
                    .onMessage(AssignOrder.class, msg -> {
                        if (msg.orderId == null) return Behaviors.same();
                        ActorRef<Worker.Command> worker = workers.get(new Random().nextInt(workers.size()));
                        worker.tell(new Worker.BuildCarBody(msg.orderId, context.getSelf()));
                        return Behaviors.same();
                    })
                    .onMessage(WorkerDone.class, msg -> {
                        context.getLog().info("{} hat Auftrag {} fertiggestellt.", name, msg.orderId);
                        orderBook.tell(new OrderBook.AssignOrder(adapter));
                        return Behaviors.same();
                    })
                    .build();
        });
    }
}
