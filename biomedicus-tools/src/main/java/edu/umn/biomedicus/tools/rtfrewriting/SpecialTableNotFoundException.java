package edu.umn.biomedicus.tools.rtfrewriting;

/**
 * Thrown when a special table is not found.
 */
public class SpecialTableNotFoundException extends Exception {
    SpecialTableNotFoundException() {
    }

    SpecialTableNotFoundException(String message) {
        super(message);
    }

    SpecialTableNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    SpecialTableNotFoundException(Throwable cause) {
        super(cause);
    }

    SpecialTableNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
