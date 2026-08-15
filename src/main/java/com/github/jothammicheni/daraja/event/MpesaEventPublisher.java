package com.github.jothammicheni.daraja.event;

import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.dto.StkPushResponse;
import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;
import com.github.jothammicheni.daraja.listener.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Publishes M-Pesa events to registered listeners.
 * Thread-safe and can be used in both Spring and non-Spring applications.
 */
public class MpesaEventPublisher {

    // Thread-safe storage for listeners by event type
    private final Map<Class<? extends MpesaEvent>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();
    private static volatile MpesaEventPublisher instance;

    /**
     * Get the singleton instance (optional - can also instantiate directly).
     */
    public static MpesaEventPublisher getInstance() {
        if (instance == null) {
            synchronized (MpesaEventPublisher.class) {
                if (instance == null) {
                    instance = new MpesaEventPublisher();
                }
            }
        }
        return instance;
    }

    /**
     * Register a listener for a specific event type.
     */
    public <T extends MpesaEvent> void registerListener(Class<T> eventClass, EventListener<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>())
                .add(listener);
    }

    /**
     * Register a listener using a lambda (simpler API).
     */
    public <T extends MpesaEvent> void on(Class<T> eventClass, java.util.function.Consumer<T> consumer) {
        registerListener(eventClass, consumer::accept);
    }

    /**
     * Remove a listener for a specific event type.
     */
    public <T extends MpesaEvent> void removeListener(Class<T> eventClass, EventListener<T> listener) {
        List<EventListener<?>> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    /**
     * Publish an event to all registered listeners.
     */
    @SuppressWarnings("unchecked")
    public <T extends MpesaEvent> void publish(T event) {
        Class<?> eventClass = event.getClass();
        List<EventListener<?>> eventListeners = listeners.get(eventClass);

        if (eventListeners != null && !eventListeners.isEmpty()) {
            for (EventListener<?> listener : eventListeners) {
                try {
                    ((EventListener<T>) listener).onEvent(event);
                } catch (Exception e) {
                    // Log but don't propagate - one listener shouldn't break others
                    System.err.println("Error in event listener: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Convenience method: publish a webhook payload as the appropriate event.
     * This is the main method used in the webhook handler.
     */
    public void publishWebhookEvent(WebhookPayload payload) {
        if (payload == null) {
            return;
        }

        // Determine event type based on result code
        if (payload.isSuccess()) {
            publish(new PaymentSuccessEvent(payload));
        } else if ("1032".equals(payload.getResultCode())) {
            publish(new PaymentCancelledEvent(payload));
        } else {
            publish(new PaymentFailedEvent(payload));
        }
    }

    /**
     * Convenience method: publish a payment initiated event.
     */
    public void publishInitiatedEvent(StkPushRequest request, StkPushResponse response) {
        if (response != null && response.isAccepted()) {
            publish(new PaymentInitiatedEvent(request, response));
        }
    }

    /**
     * Clear all listeners (useful for testing).
     */
    public void clearAllListeners() {
        listeners.clear();
    }

    /**
     * Get the number of registered listeners for an event type.
     */
    public <T extends MpesaEvent> int getListenerCount(Class<T> eventClass) {
        List<EventListener<?>> eventListeners = listeners.get(eventClass);
        return eventListeners != null ? eventListeners.size() : 0;
    }

    /**
     * Get total listeners across all event types.
     */
    public int getTotalListenerCount() {
        return listeners.values().stream().mapToInt(List::size).sum();
    }
}