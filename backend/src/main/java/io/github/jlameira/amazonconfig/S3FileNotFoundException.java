package io.github.jlameira.amazonconfig;

public class S3FileNotFoundException extends S3StorageException {
    public S3FileNotFoundException(String message) {
        super(message);
    }

    public S3FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
