package etu.sprint.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import etu.sprint.annotation.AnnotationController;
import etu.sprint.annotation.GetMethode;
import etu.sprint.utility.Mapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    private final Map<String, Mapping> urlMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();

        List<Class<?>> controllers = Arrays.asList(
            etu.sprint.controller.TestController.class,
            etu.sprint.controller.HelloController.class,
            etu.sprint.controller.SprintController.class
        );

        for (Class<?> controllerClass : controllers) {
            if (controllerClass.isAnnotationPresent(AnnotationController.class)) {
                AnnotationController ac = controllerClass.getAnnotation(AnnotationController.class);
                String prefix = ac.value();

                // Scan methods for @GetMethode
                for (Method method : controllerClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GetMethode.class)) {
                        GetMethode gm = method.getAnnotation(GetMethode.class);
                        String fullPath = prefix + gm.value();
                        
                        // Normalize path to remove trailing slash for consistent mapping
                        String normalizedPath = fullPath.endsWith("/") && fullPath.length() > 1 ? fullPath.substring(0, fullPath.length() - 1) : fullPath;

                        Mapping mapping = new Mapping(controllerClass.getName(), method.getName());
                        urlMappings.put(normalizedPath, mapping);
                        System.out.println("Mapped URL: " + normalizedPath + " -> " + controllerClass.getSimpleName() + "." + method.getName() + "()");
                    }
                }
                 // Check for a default handler method if no specific methods are annotated
                try {
                    // Check for a method named "handle" as a fallback for the controller's base path
                    Method handleMethod = controllerClass.getMethod("handle", HttpServletRequest.class, HttpServletResponse.class);
                    Mapping mapping = new Mapping(controllerClass.getName(), handleMethod.getName());
                    String normalizedPrefix = prefix.endsWith("/") && prefix.length() > 1 ? prefix.substring(0, prefix.length() - 1) : prefix;
                    if (!urlMappings.containsKey(normalizedPrefix)) {
                         urlMappings.put(normalizedPrefix, mapping);
                         System.out.println("Mapped URL: " + normalizedPrefix + " -> " + controllerClass.getSimpleName() + "." + handleMethod.getName() + "()");
                    }
                } catch (NoSuchMethodException e) {
                    // No default "handle" method, which is fine.
                }
            }
        }
        // Make the mappings available to other parts of the application (like SprintController)
        getServletContext().setAttribute("urlMappings", urlMappings);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getRequestURI().substring(request.getContextPath().length());
        // Normalize path to remove trailing slash for consistent lookup
        String normalizedPath = path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
        
        Mapping mapping = urlMappings.get(normalizedPath);

        if (mapping != null) {
            try {
                Class<?> controllerClass = Class.forName(mapping.getClassName());
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                Method method = controllerClass.getMethod(mapping.getMethodName(), HttpServletRequest.class, HttpServletResponse.class);
                method.invoke(controllerInstance, request, response);
            } catch (Exception e) {
                throw new ServletException("Error invoking controller for path " + path, e);
            }
        } else {
            handleFileRequest(request, response, path);
        }
    }

    private void handleFileRequest(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException, ServletException {
        
        String fullPath = "/views" + (path.startsWith("/") ? "" : "/") + path;


        if (path.startsWith("/")) path = path.substring(1);
        if (path.startsWith("views/")) path = path.substring("views/".length());
        if (path.isEmpty()) path = "index.html";

        if (!path.endsWith(".html") && !path.endsWith(".jsp")) {
            if (fileExists(request, "/views/" + path + ".html")) {
                path = path + ".html";
            } else if (fileExists(request, "/views/" + path + ".jsp")) {
                path = path + ".jsp";
            }
        }

        String fullPath = "/views/" + path;

        if (fileExists(request, fullPath)) {
             if (path.endsWith(".jsp")) {
                RequestDispatcher dispatcher = request.getRequestDispatcher(fullPath);
                dispatcher.forward(request, response);
            } else {
                serveStaticFile(request, response, fullPath);
            }
        } else {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("<h2>Requested resource not found for URL: " + path + "</h2>");
            }
        }
    }
    
    private boolean fileExists(HttpServletRequest request, String relativePath) {
        String realPath = getServletContext().getRealPath(relativePath);
        if (realPath == null) return false;
        File file = new File(realPath);
        return file.exists() && file.isFile();
    }

    private void serveStaticFile(HttpServletRequest request, HttpServletResponse response, String relativePath)
            throws IOException {
        try (java.io.InputStream is = getServletContext().getResourceAsStream(relativePath)) {
            if (is != null) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found: " + relativePath);
            }
        }
    }
}
