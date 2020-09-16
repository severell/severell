package com.severell.core.database.migrations;

public class MigrationException extends Throwable {

    private final MigrationExceptionType type;
    private final String additionalMessage;

    enum MigrationExceptionType {
        RELATIONEXISTS("Relation Already Exists: "),
        UNKNOWN("Error: ");

        private String reason;
        MigrationExceptionType(String s) {
            this.reason = s;
        }

        public String getReason() {
            return reason;
        }
    }

    public MigrationException(MigrationExceptionType type) {
        this.type = type;
        this.additionalMessage = "";
    }

    public MigrationException(MigrationExceptionType type, String additionMessage) {
        this.type = type;
        this.additionalMessage = additionMessage;
    }

    @Override
    public String getMessage() {
        return type.getReason() + " " + additionalMessage;
    }
}
