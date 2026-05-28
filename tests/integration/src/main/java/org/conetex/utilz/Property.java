package org.conetex.utilz;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Property {

    public static String resolveProperty(Properties props, String key) {
        // 1. JVM system property ( -Dname=value )
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) {
            return sys;
        }

        // 2. Environment variable
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env;
        }

        // 3. properties file
        String val = props.getProperty(key);
        if (val != null && !val.isBlank()) {
            return val;
        }

        throw new IllegalStateException("Missing property: " + key);
    }

    public static Properties loadProperties(Class<?> clazz, String resource) throws IOException {
        Properties p = new Properties();
        try (InputStream in = clazz
                .getClassLoader()
                .getResourceAsStream(resource)) {
            p.load(in);
        }
        return p;
    }

}
