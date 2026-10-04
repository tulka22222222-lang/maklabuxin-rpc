package dev.maklabuxin.visuals.event;

@FunctionalInterface
public interface EventListener<T extends Event> {
    void call(T event);
}
