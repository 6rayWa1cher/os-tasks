package com.a6raywa1cher.ostasks.tsk5;

import lombok.Data;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.text.MessageFormat;
import java.util.Objects;

@Data
@Slf4j
public class Philosopher {
    private final String name;

    private final ForkRepository forkRepository;

    private final int dinnerDuration;

    private int dinnerCount = 0;

    public void dinner() throws InterruptedException {
//        Fork fork1 = forkRepository.getFork(this);
//        Fork fork2 = forkRepository.getFork(this);
        Pair<Fork, Fork> twoForks = forkRepository.getTwoForks(this);
        Fork fork1 = twoForks.getLeft();
        Fork fork2 = twoForks.getRight();

        log.debug(MessageFormat.format("Philosopher {0} eating with forks: {1} and {2}",
                this.getName(), fork1.getName(), fork2.getName()));

        if (dinnerDuration > 0) Thread.sleep(dinnerDuration);

        this.dinnerCount++;
        forkRepository.returnFork(fork1);
        forkRepository.returnFork(fork2);
        log.debug(MessageFormat.format("Philosopher {0} returned forks {1} and {2}",
                this.getName(), fork1.getName(), fork2.getName()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Philosopher that = (Philosopher) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
