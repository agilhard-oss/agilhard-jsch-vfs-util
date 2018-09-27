package net.agilhard.jschutil;

import net.agilhard.jsch.Logger;

public enum LogLevel {

    DEBUG(Logger.DEBUG),
    INFO(Logger.INFO),
    WARN(Logger.WARN),
    ERROR(Logger.ERROR),
    FATAL(Logger.FATAL);

    private final int level;

    private LogLevel(final int level) {
        this.level = level;
    }

    @SuppressWarnings("hiding")
    public LogLevel value(final int level) {
        LogLevel logLevel = null;
        for (final LogLevel l : LogLevel.values()) {
            if (l.level == level) {
                logLevel = l;
                break;
            }
        }
        return logLevel;
    }

    public int getLevel() {
        return this.level;
    }
}
