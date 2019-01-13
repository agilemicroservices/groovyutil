package org.groovyutil.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtility {

    private static final Logger logger = LoggerFactory.getLogger("script.logger");

    public static void debug(String logFormatString, Object... args) {
        logger.debug(logFormatString, args);
    }

    public static void info(String logFormatString, Object... args) {
        logger.info(logFormatString, args);
    }

    public static void warn(String logFormatString, Object... args) {
        logger.warn(logFormatString, args);
    }

    public static void error(String logFormatString, Object... args) {
        logger.error(logFormatString, args);
    }
}
