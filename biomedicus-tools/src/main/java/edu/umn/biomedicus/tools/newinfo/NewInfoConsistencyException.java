package edu.umn.biomedicus.tools.newinfo;

/**
 * Represents an inconsistency between data in the new information output and the original document.
 */
class NewInfoConsistencyException extends Exception {
    NewInfoConsistencyException() {
    }

    NewInfoConsistencyException(String message) {
        super(message);
    }

    NewInfoConsistencyException(String message, Throwable cause) {
        super(message, cause);
    }

    NewInfoConsistencyException(Throwable cause) {
        super(cause);
    }

    NewInfoConsistencyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
