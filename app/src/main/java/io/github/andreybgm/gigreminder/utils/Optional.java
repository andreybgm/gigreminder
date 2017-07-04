package io.github.andreybgm.gigreminder.utils;

public class Optional<T> {

    private final T value;

    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    public static <T> Optional<T> empty() {
        return new Optional<>();
    }

    public Optional(T value) {
        this.value = value;
    }

    public Optional() {
        this.value = null;
    }

    public boolean isPresent() {
        return value != null;
    }

    public T getValue() {
        if (!isPresent()) {
            throw new IllegalStateException("The value isn't present");
        }

        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Optional<?> optional = (Optional<?>) o;

        return value != null ? value.equals(optional.value) : optional.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Optional{" +
                "value=" + value +
                '}';
    }
}
