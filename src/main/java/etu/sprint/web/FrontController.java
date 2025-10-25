package etu.sprint.web;
import java.io.*;
import java.util.*;
import java.lang.reflect.Method;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import etu.sprint.annotation.AnnotationController;
import etu.sprint.annotation.GetMethode;

public class FrontController extends HttpServlet {

    // Map of controller classes keyed by class-level prefix
    private final Map<String, Class<?>> routeMap = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();

        // Manually list controller classes (can be automated later)
        List<Class<?>> controllers = Arrays.asList(
            etu.sprint.controller.TestController.class,
            etu.sprint.controller.HelloController.class
        );

        for (Class<?> controller : controllers) {
            if (controller.isAnnotationPresent(AnnotationController.class)) {
                AnnotationController ac = controller.getAnnotation(AnnotationController.class);
                String prefix = ac.value();
                routeMap.put(prefix, controller);
                System.out.println("Mapped route: " + prefix + " -> " + controller.getName());
            }
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.isEmpty() || path.equals("/")) path = "/index";

        boolean handled = false;

        for (Map.Entry<String, Class<?>> entry : routeMap.entrySet()) {
            String prefix = entry.getKey();
            Class<?> controllerClass = entry.getValue();

            if (path.startsWith(prefix)) {
                try {
                    Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                    for (Method method : controllerClass.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(GetMethode.class)) {
                            GetMethode gm = method.getAnnotation(GetMethode.class);
                            String fullPath = prefix + gm.value();

                            // normalize trailing slashes
                            String normalizedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                            String normalizedFullPath = fullPath.endsWith("/") ? fullPath.substring(0, fullPath.length() - 1) : fullPath;

                            if (normalizedPath.equals(normalizedFullPath)) {
                                method.invoke(controllerInstance, request, response);
                                handled = true;
                                return; // request handled
                            }
                        }
                    }

                    // fallback to handle() method if exists
                    if (!handled) {
                        try {
                            controllerClass.getMethod("handle", HttpServletRequest.class, HttpServletResponse.class)
                                           .invoke(controllerInstance, request, response);
                            handled = true;
                            return;
                        } catch (NoSuchMethodException ex) {
                            // ignore, fallback to file serving
                        }
                    }

                } catch (Exception e) {
                    throw new ServletException("Error invoking controller for path " + path, e);
                }
            }
        }

        if (!handled) {
            handleFileRequest(request, response, path);
        }
    }

    private void handleFileRequest(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException, ServletException {

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
            } else if (path.endsWith(".html")) {
                serveHtml(request, response, fullPath);
            }
        } else {
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

    private void serveHtml(HttpServletRequest request, HttpServletResponse response, String relativePath)
            throws IOException {
        String realPath = getServletContext().getRealPath(relativePath);
        File file = new File(realPath);

        response.setContentType("text/html;charset=UTF-8");

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             PrintWriter out = response.getWriter()) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
        }
    }
}