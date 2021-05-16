package com.a6raywa1cher.ostasks.tsk5;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.text.MessageFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class ForkAlreadyOwnedException extends RuntimeException {
    private final Fork fork;

    private final Philosopher currentOwner;

    private final Philosopher newOwner;

    public ForkAlreadyOwnedException(Fork fork, Philosopher currentOwner, Philosopher newOwner) {
        super(MessageFormat.format(
                "Fork '{0}' is owned by {1}, but {2} is trying to steal",
                fork.getName(), currentOwner.getName(), newOwner.getName()
        ));
        this.fork = fork;
        this.currentOwner = currentOwner;
        this.newOwner = newOwner;
    }
}
