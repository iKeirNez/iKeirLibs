package com.ikeirnez.ikeirlibs.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Same concept as {@link java.util.concurrent.Phaser}, however this is non-blocking
 */
public class NonBlockingPhaser {

    /**
     * Time after the final party arrives to invoke the runnable, this will catch parties that register after the last one arrives
     */
    private static final long RUNNABLE_WAIT_TIME = 500;

    /**
     * The total parties arrived, or the instance is yet to wait on
     */
    private AtomicInteger partiesTotal = new AtomicInteger(0);

    /**
     * The total parties this instance is yet to wait on
     */
    private AtomicInteger partiesWait = new AtomicInteger(0);

    /**
     * The runnable which will be run when all parties have arrived, will be invoked asynchronously
     */
    private Runnable runnable;

    private AtomicReference<NonBlockingPhaserRunnable> runnableExecutionThread = new AtomicReference<>(null);

    /**
     * Equals true if the Runnable has been run and this class is no longer usable
     */
    private AtomicBoolean done = new AtomicBoolean(false);

    /**
     * Whether or not this instance has been terminated
     */
    private AtomicBoolean terminated = new AtomicBoolean(false);

    public NonBlockingPhaser(Runnable runnable){
        this(0, runnable);
    }

    /**
     * Instigates a new instance
     *
     * @param initialParties The initial amount of parties to wait on, if 0 we will still continue to wait
     * @param runnable The runnable to be executed when all parties have arrived
     */
    public NonBlockingPhaser(int initialParties, Runnable runnable){
        if (initialParties != 0){
            register(initialParties);
        }

        this.runnable = runnable;
    }

    /**
     * Gets the total amount of registered parties, this includes ones that have already arrived.
     *
     * @return the total amount of registered parties
     */
    public int getRegisteredParties(){
        return this.partiesTotal.get();
    }

    /**
     * Gets the amount of unarrived parties (parties we're waiting on).
     *
     * @return the amount of unarrived parties
     */
    public int getUnarrivedParties(){
        return this.partiesWait.get();
    }

    /**
     * Registers 1 party.
     *
     * @return the new amount of parties we're waiting on
     */
    public int register(){
        return register(1);
    }

    /**
     * Registers an amount of parties.
     *
     * @param amount the amount of parties to register
     * @return the new amount of parties we're waiting on
     */
    public int register(int amount){
        if (amount < 1){
            throw new IllegalArgumentException("Cannot register less than 1 parties.");
        }

        NonBlockingPhaserRunnable runnableExecutionThread = this.runnableExecutionThread.get();

        if (runnableExecutionThread != null){
            if (isDone()){
                throw new RuntimeException("The runnable task has already run, you were too late.");
            } else {
                runnableExecutionThread.cancelled.set(true);
            }
        }

        this.partiesTotal.addAndGet(amount);
        return this.partiesWait.addAndGet(amount);
    }

    /**
     * Sets 1 party as being arrived.
     * See #arrive(Integer)
     *
     * @return the new amount of parties we're waiting on
     */
    public int arrive(){
        return arrive(1);
    }

    /**
     * Sets a number of parties as being arrived.
     * If all parties have arrived when this method is run, the countdown to the {@link java.lang.Runnable} being run begins
     * If another party registers in that time, the countdown will be cancelled and the process will begin again
     *
     * @param amount the amount of parties to set as being arrived
     * @return the new amount of parties we're waiting on
     */
    public int arrive(int amount){
        this.partiesWait.set(this.partiesWait.get() - amount);

        int waiting = getUnarrivedParties();

        if (waiting < 0){
            throw new RuntimeException("More than " + getRegisteredParties() + " have arrived.");
        } else if (!isTerminated() && waiting == 0){
            if (isDone()){
                throw new RuntimeException("The runnable task has already run, you were too late.");
            }

            NonBlockingPhaserRunnable runnableExecutionThread = this.runnableExecutionThread.get();
            if (runnableExecutionThread != null){
                runnableExecutionThread.cancelled.set(true);
            }

            this.runnableExecutionThread = new AtomicReference<>(new NonBlockingPhaserRunnable(runnable));
            new Thread(this.runnableExecutionThread.get()).start();
        }

        return waiting;
    }

    /**
     * Gets if the {@link java.lang.Runnable} has been run.
     *
     * @return has the runnable been run
     */
    public boolean isDone(){
        return this.done.get();
    }

    /**
     * Terminates the class, preventing it from being used.
     */
    public void terminate(){
        this.terminated.set(true);
    }

    /**
     * Gets if the class is terminated.
     *
     * @return is the class terminated
     */
    public boolean isTerminated(){
        return this.terminated.get();
    }

    private class NonBlockingPhaserRunnable implements Runnable {

        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final Runnable runnable;

        public NonBlockingPhaserRunnable(Runnable runnable){
            this.runnable = runnable;
        }

        @Override
        public void run() {
            // this waits a little bit of time for any more registrations
            // otherwise this class wouldn't work in some cases

            try {
                Thread.sleep(RUNNABLE_WAIT_TIME);
            } catch (InterruptedException e) {}

            if (!cancelled.get()){
                done.set(true);
                runnable.run();
            }
        }
    }
}
