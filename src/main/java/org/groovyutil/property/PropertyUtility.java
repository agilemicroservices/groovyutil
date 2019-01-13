package org.groovyutil.property;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtility {

    private static Properties properties;

    public static void OpenPropertyFileClassPath(String propertyFile) throws IOException {
        properties = new Properties();
        InputStream inputStream = PropertyUtility.class.getResourceAsStream("/" + propertyFile);
        properties.load(inputStream);
    }

    public static String GetProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

}
