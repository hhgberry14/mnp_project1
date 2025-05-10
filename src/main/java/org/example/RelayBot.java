package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.ArrayList;
import java.util.List;

public class RelayBot extends AbstractBehavior<RelayBot.Message> {

    public interface Message {};

    public record Hello(List<ActorRef<RelayBot.Message>> order, int count) implements Message {  }
    public record Start(List<ActorRef<Message>> order) implements Message {  }

    public static Behavior<Message> create(String name) {
        return Behaviors.setup(context -> new RelayBot(context, name));
    }

    private final String name;

    private RelayBot(ActorContext<Message> context, String name) {
        super(context);
        this.name = name;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Hello.class, this::onHello)
                .onMessage(Start.class, this::onStart)
                .build();
    }

    private  Behavior<Message> onStart(Start msg) {
        var next_hop = msg.order.removeFirst();
        var new_order = new ArrayList<>(msg.order);
        new_order.add(next_hop);
        next_hop.tell(new Hello(new_order, 1));
        return this;
    }

    private Behavior<Message> onHello(Hello msg) {
        if (msg.count > 15) {
            return Behaviors.stopped();
        }
        getContext().getLog().info("I, {}, got Hello ({})", this.name, msg.count);
        var next_hop = msg.order.removeFirst();
        var new_order = new ArrayList<>(msg.order);
        new_order.add(next_hop);
        next_hop.tell(new Hello(new_order, msg.count + 1));
        return this;
    }
}
