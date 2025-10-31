package etu.sprint.web;

import etu.sprint.annotation.AnnotationController;
import etu.sprint.annotation.GetMethode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FrontController extends HttpServlet {

    private static class ControllerMethod {
        final Class<?> controllerClass;
        final Method method;

        ControllerMethod(Class<?> controllerClass, Method method) {
            this.controllerClass = controllerClass;
            this.method = method;
        }
    }

    private final Map<String, ControllerMethod> routeMap = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            scanControllers("etu.sprint.controller");
        } catch (Exception e) {
            throw new ServletException("Failed to initialize FrontController", e);
        }
    }

    private void scanControllers(String basePackage) throws IOException, ClassNotFoundException, URISyntaxException {
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

                        routeMap.put(fullPath, new ControllerMethod(clazz, method));
                        System.out.println("Mapped route: " + fullPath + " -> " + clazz.getName() + "#" + method.getName());
                    }
                }

                if (!methodAnnotated) {
                    try {
                        Method handleMethod = clazz.getMethod("handle", HttpServletRequest.class, HttpServletResponse.class);
                        String normalizedPrefix = prefix;
                        if (normalizedPrefix.endsWith("/") && normalizedPrefix.length() > 1) {
                            normalizedPrefix = normalizedPrefix.substring(0, normalizedPrefix.length() - 1);
                        }
                        routeMap.put(normalizedPrefix, new ControllerMethod(clazz, handleMethod));
                        System.out.println("Mapped route (handle): " + normalizedPrefix + " -> " + clazz.getName() + "#handle");
                    } catch (NoSuchMethodException e) {
                        // No handle method
                    }
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            System.err.println("Could not process class: " + className + " " + e.getMessage());
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        ControllerMethod controllerMethod = routeMap.get(path);

        if (controllerMethod != null) {
            try {
                Object controllerInstance = controllerMethod.controllerClass.getDeclaredConstructor().newInstance();
                controllerMethod.method.invoke(controllerInstance, request, response);
            } catch (Exception e) {
                throw new ServletException("Error invoking controller for path " + path, e);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("URL inconnu pour cette url: " + path);
        }
    }
}