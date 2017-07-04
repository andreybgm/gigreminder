package io.github.andreybgm.gigreminder.repository.error;

public class DataCorruptedException extends RuntimeException {
    public DataCorruptedException() {
    }

    public DataCorruptedException(String message) {
        super(message);
    }
}
