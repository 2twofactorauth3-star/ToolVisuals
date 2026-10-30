package ru.he3hauka.toolvisuals.animation.protocol.server;

import ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire.Mode;
import ru.he3hauka.toolvisuals.network.RevisionOutbox;

public final class StateOutbox extends RevisionOutbox<PlaybackLedger.Published> {
    public StateOutbox(int capacity){super(capacity,value->new Identity(value.state().actor(),value.state().epoch(),value.state().revision()),value->value.state().mode()==Mode.STOPPED);}
}
