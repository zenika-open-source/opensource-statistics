package fr.zenika.opensource.stats.exception;

public class DatabaseException extends Exception {

    public DatabaseException(Exception exception) {
        super(exception);
    }
}
