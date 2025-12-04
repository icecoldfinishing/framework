package etu.sprint.util;

import etu.sprint.annotation.AnnotationController;
import etu.sprint.annotation.GetMapping;
import etu.sprint.annotation.PostMapping;
import etu.sprint.annotation.PutMapping;
import etu.sprint.annotation.DeleteMapping;
import etu.sprint.annotation.PatchMapping;
import etu.sprint.annotation.RequestMapping;
import etu.sprint.model.ControllerMethod;
import etu.sprint.model.MethodInfo;
import etu.sprint.model.HttpMethod;
import etu.sprint.model.RouteMatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {

    // Map: URL -> (HttpMethod -> ControllerMethod)
    private final Map<String, Map<HttpMethod, ControllerMethod>> routes = new HashMap<>();
    private final Map<String, List<MethodInfo>> controllerInfo = new HashMap<>();

    public Map<String, Map<HttpMethod, ControllerMethod>> getRoutes() {
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
                String controllerPrefix = ac.value();

                for (Method method : clazz.getDeclaredMethods()) {
                    String path = null;
                    HttpMethod[] httpMethods = null;

                    if (method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping gm = method.getAnnotation(GetMapping.class);
                        path = gm.value();
                        httpMethods = new HttpMethod[]{HttpMethod.GET};
                    } else if (method.isAnnotationPresent(PostMapping.class)) {
                        PostMapping pm = method.getAnnotation(PostMapping.class);
                        path = pm.value();
                        httpMethods = new HttpMethod[]{HttpMethod.POST};
                    } else if (method.isAnnotationPresent(PutMapping.class)) {
                        PutMapping pum = method.getAnnotation(PutMapping.class);
                        path = pum.value();
                        httpMethods = new HttpMethod[]{HttpMethod.PUT};
                    } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                        DeleteMapping dm = method.getAnnotation(DeleteMapping.class);
                        path = dm.value();
                        httpMethods = new HttpMethod[]{HttpMethod.DELETE};
                    } else if (method.isAnnotationPresent(PatchMapping.class)) {
                        PatchMapping pam = method.getAnnotation(PatchMapping.class);
                        path = pam.value();
                        httpMethods = new HttpMethod[]{HttpMethod.PATCH};
                    } else if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping rm = method.getAnnotation(RequestMapping.class);
                        path = rm.value();
                        httpMethods = rm.method();
                        if (httpMethods.length == 0) {
                            // If no method specified in RequestMapping, default to GET
                            httpMethods = new HttpMethod[]{HttpMethod.GET};
                        }
                    }

                    if (path != null && httpMethods != null) {
                        String fullPath = normalizePath(controllerPrefix + "/" + path);
                        ControllerMethod controllerMethod = new ControllerMethod(clazz, method);

                        for (HttpMethod httpMethod : httpMethods) {
                            routes.computeIfAbsent(fullPath, k -> new HashMap<>()) // Get or create inner map for the path
                                  .compute(httpMethod, (k, v) -> {
                                      if (v != null) {
                                          throw new RuntimeException(String.format("Collision detected: URL '%s' with HTTP method '%s' is already mapped.", fullPath, httpMethod));
                                      }
                                      return controllerMethod;
                                  });
                            System.out.println(String.format("Mapped: %s %s -> %s.%s()", httpMethod, fullPath, clazz.getName(), method.getName()));
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            System.err.println("Could not process class: " + className + " " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error during route mapping: " + e.getMessage());
        }
    }

    private String normalizePath(String path) {
        if (path.isEmpty()) return "/";
        String normalized = path.replaceAll("//+", "/"); // Replace multiple slashes with a single one
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

}