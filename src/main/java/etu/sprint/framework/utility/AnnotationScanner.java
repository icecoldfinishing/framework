package etu.sprint.framework.utility;

import etu.sprint.framework.Mapping;
import etu.sprint.framework.annotation.Controller;
import etu.sprint.framework.annotation.Url;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AnnotationScanner {

    public static Map<String, Mapping> scanPackage(String packageName) throws Exception {
        Map<String, Mapping> urlMappings = new HashMap<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            throw new IllegalArgumentException("Package not found: " + packageName);
        }

        File directory = new File(resource.toURI());

        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Controller.class)) {
                    for (java.lang.reflect.Method method : clazz.getMethods()) {
                        if (method.isAnnotationPresent(Url.class)) {
                            Url urlAnnotation = method.getAnnotation(Url.class);
                            String url = urlAnnotation.value();

                            if (urlMappings.containsKey(url)) {
                                throw new IllegalStateException("Duplicate URL mapping found: " + url);
                            }

                            urlMappings.put(url, new Mapping(clazz.getName(), method.getName()));
                        }
                    }
                }
            }
        }

        return urlMappings;
    }
}
