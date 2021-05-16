package com.a6raywa1cher.ostasks.tsk5;

import lombok.Data;

@Data
public class Fork {
    private final String name;

    private Philosopher owner;

    public void setOwner(Philosopher philosopher) {
        if (owner != null && philosopher != null) {
            throw new ForkAlreadyOwnedException(this, owner, philosopher);
        }
        this.owner = philosopher;
    }

    public void eraseOwner() {
        this.owner = null;
    }
}
