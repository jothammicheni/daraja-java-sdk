package com.github.jothammicheni.daraja.listener;

import com.github.jothammicheni.daraja.event.MpesaEvent;

/**
 * Interface for listening to M-Pesa events.
 * Implement this interface to receive notifications about payment events.
 */
@FunctionalInterface
public interface EventListener<T extends MpesaEvent> {

    /**
     * Called when an event of the subscribed type occurs.
     *
     * @param event The event containing payment data
     */
    void onEvent(T event);

    /**
     * Helper method to create a listener from a lambda.
     */
    static <T extends MpesaEvent> EventListener<T> fromConsumer(java.util.function.Consumer<T> consumer) {
        return consumer::accept;
    }
}