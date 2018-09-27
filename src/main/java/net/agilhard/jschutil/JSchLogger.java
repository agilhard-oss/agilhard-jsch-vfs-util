package net.agilhard.jschutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class JSchLogger implements a Logger for the jsch library.
 */
public class JSchLogger implements net.agilhard.jsch.Logger {

    /** The Logger. */
    private final Logger log = LoggerFactory.getLogger(JSchLogger.class);

    private LogLevel threshold = LogLevel.INFO;

    /**
     * Check if logging is enabled for the given jsch logging level.
     *
     * @param level
     *            the level
     * @return true, if is enabled
     * @see net.agilhard.jsch.Logger#isEnabled(int)
     */
    @Override
    public boolean isEnabled(final int level) {
        boolean b = false;
        if (level >= this.threshold.getLevel()) {
            switch (level) {
            case net.agilhard.jsch.Logger.DEBUG:
                b = this.log.isDebugEnabled();
                break;
            case net.agilhard.jsch.Logger.INFO:
                b = this.log.isInfoEnabled();
                break;
            case net.agilhard.jsch.Logger.WARN:
                b = this.log.isWarnEnabled();
                break;
            case net.agilhard.jsch.Logger.ERROR:
                b = this.log.isErrorEnabled();
                break;
            case net.agilhard.jsch.Logger.FATAL:
                b = this.log.isErrorEnabled();
                break;
            default:
                break;
            }
        }
        return b;
    }

    /**
     * Log a message on the specified jsch logging level.
     *
     * @param level
     *            the level
     * @param message
     *            the message
     * @see net.agilhard.jsch.Logger#log(int, java.lang.String)
     */
    public void log(final LogLevel level, final String message) {
        this.log(level.getLevel(), message);
    }

    /**
     * Log a message on the specified jsch logging level.
     *
     * @param level
     *            the level
     * @param message
     *            the message
     * @see net.agilhard.jsch.Logger#log(int, java.lang.String)
     */
    @Override
    public void log(final int level, final String message) {
        if (level >= this.threshold.getLevel()) {
            switch (level) {
            case net.agilhard.jsch.Logger.DEBUG:
                this.log.debug(message);
                break;
            case net.agilhard.jsch.Logger.INFO:
                this.log.info(message);
                break;
            case net.agilhard.jsch.Logger.WARN:
                this.log.warn(message);
                break;
            case net.agilhard.jsch.Logger.ERROR:
                this.log.error(message);
                break;
            case net.agilhard.jsch.Logger.FATAL:
                this.log.error(message);
                break;
            default:
                break;
            }
        }
    }

    public void setThreshold(final LogLevel threshold) {
        this.threshold = threshold;
    }
}
