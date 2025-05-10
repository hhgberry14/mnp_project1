package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.ArrayList;
import java.util.Arrays;

public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Create> {

    public static class Create {
    }

    public static Behavior<Create> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    private AkkaMainSystem(ActorContext<Create> context) {
        super(context);
    }

    @Override
    public Receive<Create> createReceive() {
        return newReceiveBuilder().onMessage(Create.class, this::onCreate).build();
    }

    private Behavior<Create> onCreate(Create command) {
        //#create-actors
        var alice = this.getContext().spawn(RelayBot.create("alice"), "alice");
        var bob = this.getContext().spawn(RelayBot.create("bob"), "bob");
        var charlie = this.getContext().spawn(RelayBot.create("charlie"), "charlie");

        var order = new ArrayList<>(Arrays.asList(bob, charlie, alice));
        alice.tell(new RelayBot.Start(order));

        //#create-actors

        return this;
    }
}