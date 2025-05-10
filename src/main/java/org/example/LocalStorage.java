package org.example;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import java.util.*;

public class LocalStorage {
    interface Command {}

    public static class RequestSpecials implements Command {
        public final int orderId;
        public final ActorRef<Worker.Command> worker;
        public RequestSpecials(int orderId, ActorRef<Worker.Command> worker) {
            this.orderId = orderId;
            this.worker = worker;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.receive(Command.class)
                .onMessage(RequestSpecials.class, msg -> {
                    List<String> specials = Arrays.asList("Ledersitze", "Klimaautomatik", "Elektrische Fensterheber", "Automatikgetriebe");
                    Random rand = new Random();
                    List<String> selected = List.of(
                            specials.get(rand.nextInt(specials.size())),
                            specials.get(rand.nextInt(specials.size()))
                    );
                    msg.worker.tell(new Worker.ProvideRequests(msg.orderId, selected));
                    return Behaviors.same();
                })
                .build();
    }
}