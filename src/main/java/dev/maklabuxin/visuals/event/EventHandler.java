package dev.maklabuxin.visuals.event;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Простая шина: модуль держит поля типа {@link EventListener},
 * при включении они регистрируются через {@link #register(Object)}.
 */
public class EventHandler {
    private final List<EventListener<Event>> listeners = new CopyOnWriteArrayList<>();

    @SuppressWarnings("unchecked")
    public void register(Object owner) {
        for (Field field : owner.getClass().getDeclaredFields()) {
            if (!EventListener.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                EventListener<Event> listener = (EventListener<Event>) field.get(owner);
                if (listener != null && !listeners.contains(listener)) listeners.add(listener);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void unregister(Object owner) {
        for (Field field : owner.getClass().getDeclaredFields()) {
            if (!EventListener.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                listeners.remove((EventListener<Event>) field.get(owner));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <T extends Event> T call(T event) {
        for (EventListener<Event> listener : listeners) {
            listener.call(event);
        }
        return event;
    }
}
