

package app.coronawarn.server.services.distribution.objectstore.client;

/**
 * {@link ObjectStoreOperationFailedException} indicates that a object store operation could not be performed.
 */
public class ObjectStoreOperationFailedException extends RuntimeException {

  /**
   * Constructs a new {@link ObjectStoreOperationFailedException} with the specified message.
   *
   * @param message The detail message.
   */
  public ObjectStoreOperationFailedException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link ObjectStoreOperationFailedException} with the specified message and the {@link Throwable}
   * that cause the object store operation to fail.
   *
   * @param message The detail message.
   * @param cause   The cause.
   */
  public ObjectStoreOperationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
