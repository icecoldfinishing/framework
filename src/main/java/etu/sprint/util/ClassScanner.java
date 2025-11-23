package etu.sprint.util;

import etu.sprint.annotation.AnnotationController;
import etu.sprint.annotation.GetMethode;
import etu.sprint.model.ControllerMethod;
import etu.sprint.model.MethodInfo;
import etu.sprint.model.Route;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {

    private final List<Route> routes = new ArrayList<>();
    private final Map<String, List<MethodInfo>> controllerInfo = new HashMap<>();

    public List<Route> getRoutes() {
        return routes;
    }

    public Map<String, List<MethodInfo>> getControllerInfo() {
        return controllerInfo;
    }

    public void scan(String basePackage) throws IOException, ClassNotFoundException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = basePackage.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if ("file".equals(resource.getProtocol())) {
                scanDirectory(new File(resource.toURI()), basePackage);
            } else if ("jar".equals(resource.getProtocol())) {
                scanJar(resource);
            }
        }
    }

    private void scanDirectory(File directory, String packageName) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                processClass(className);
            }
        }
    }

    private void scanJar(URL resource) throws IOException, ClassNotFoundException {
        JarURLConnection jarURLConnection = (JarURLConnection) resource.openConnection();
        JarFile jarFile = jarURLConnection.getJarFile();
        String packagePath = jarURLConnection.getEntryName();

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.startsWith(packagePath) && entryName.endsWith(".class") && !entry.isDirectory()) {
                String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                processClass(className);
            }
        }
    }

    private void processClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(AnnotationController.class)) {
                List<MethodInfo> methods = new ArrayList<>();
                for (Method method : clazz.getDeclaredMethods()) {
                    methods.add(new MethodInfo(method));
                }
                controllerInfo.put(clazz.getName(), methods);

                AnnotationController ac = clazz.getAnnotation(AnnotationController.class);
                String prefix = ac.value();
                boolean methodAnnotated = false;

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GetMethode.class)) {
                        methodAnnotated = true;
                        GetMethode gm = method.getAnnotation(GetMethode.class);
                        String fullPath = prefix + gm.value();

                        if (fullPath.endsWith("/") && fullPath.length() > 1) {
                            fullPath = fullPath.substring(0, fullPath.length() - 1);
                        }
                        
                        routes.add(new Route(fullPath, new ControllerMethod(clazz, method)));
                    }
                }

                if (!methodAnnotated) {
                    try {
                        Method handleMethod = clazz.getMethod("handle", HttpServletRequest.class, HttpServletResponse.class);
                        String normalizedPrefix = prefix;
                        if (normalizedPrefix.endsWith("/") && normalizedPrefix.length() > 1) {
                            normalizedPrefix = normalizedPrefix.substring(0, normalizedPrefix.length() - 1);
                        }
                        routes.add(new Route(normalizedPrefix, new ControllerMethod(clazz, handleMethod)));
                    } catch (NoSuchMethodException e) {
                        // No handle method
                    }
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            System.err.println("Could not process class: " + className + " " + e.getMessage());
        }
    }
}
