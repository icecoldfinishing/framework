package etu.sprint.web;

import etu.sprint.handler.HandlerAdapter;
import etu.sprint.model.ControllerMethod;
import etu.sprint.model.HttpMethod;
import etu.sprint.model.RouteMatcher;
import etu.sprint.util.ClassScanner;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.annotation.MultipartConfig;

@MultipartConfig
public class FrontServlet extends HttpServlet {

    private HandlerAdapter handlerAdapter;
    // Map: URL Pattern -> (HttpMethod -> ControllerMethod)
    private Map<String, Map<HttpMethod, ControllerMethod>> routes;
    private final Map<String, RouteMatcher> routeMatchers = new HashMap<>(); // To store compiled regex patterns

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            String controllerPackage = getServletConfig().getInitParameter("controller-package");
            if (controllerPackage == null || controllerPackage.isEmpty()) {
                throw new ServletException("Initialization parameter 'controller-package' is not set.");
            }
            ClassScanner scanner = new ClassScanner();
            scanner.scan(controllerPackage);

            // Store the scanned routes
            this.routes = scanner.getRoutes();

            // Pre-compile all unique URL patterns from routes for efficient matching
            for (String urlPattern : routes.keySet()) {
                routeMatchers.put(urlPattern, new RouteMatcher(urlPattern));
            }
            
            // Keep controller info for debugging or introspection
            ServletContext servletContext = getServletContext();
            servletContext.setAttribute("controllerInfo", scanner.getControllerInfo());

            this.handlerAdapter = new HandlerAdapter();

            // Log all mapped routes at startup
            System.out.println("\n--- Mapped Routes ---");
            routes.forEach((pathPattern, methodMap) -> {
                methodMap.forEach((method, controllerMethod) -> {
                    System.out.println(String.format("[%%s] %%s -> %%s.%%s()", method, pathPattern, controllerMethod.controllerClass.getName(), controllerMethod.method.getName()));
                });
            });
            System.out.println("----------------------\n");

        } catch (Exception e) {
            throw new ServletException("Failed to initialize FrontServlet", e);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.isEmpty()) {
            path = "/";
        } else if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        HttpMethod requestMethod = HttpMethod.valueOf(request.getMethod().toUpperCase());

        ControllerMethod controllerMethod = null;
        Map<String, String> pathVariables = null;
        String matchedUrlPattern = null;

        // Find a matching URL pattern first
        for (Map.Entry<String, RouteMatcher> entry : routeMatchers.entrySet()) {
            Map<String, String> currentPathVariables = entry.getValue().match(path);
            if (currentPathVariables != null) {
                matchedUrlPattern = entry.getKey();
                pathVariables = currentPathVariables;
                break;
            }
        }

        if (matchedUrlPattern != null) {
            Map<HttpMethod, ControllerMethod> methodHandlers = routes.get(matchedUrlPattern);
            if (methodHandlers != null && methodHandlers.containsKey(requestMethod)) {
                controllerMethod = methodHandlers.get(requestMethod);
            } else {
                // 405 Method Not Allowed
                response.setContentType("text/plain;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                if (methodHandlers != null) {
                    String allowedMethods = methodHandlers.keySet().stream()
                            .map(Enum::toString)
                            .collect(Collectors.joining(", "));
                    response.setHeader("Allow", allowedMethods);
                    response.getWriter().println(String.format("Method %%s not allowed for URL %%s. Allowed methods: %%s", requestMethod, path, allowedMethods));
                } else {
                    response.getWriter().println(String.format("No handlers found for URL %%s", path));
                }
                return;
            }
        } else {
            // Check if it's a static resource handled by default servlet
            try {
                if (getServletContext().getResource(path) != null) {
                   getServletContext().getNamedDispatcher("default").forward(request, response);
                   return;
                }
            } catch (Exception e) {
                // Ignore and proceed to 404
            }

            // 404 Not Found
            response.setContentType("text/plain;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("URL inconnu pour cette url: " + path);
            return;
        }

        if (controllerMethod != null) {
            try {
                handlerAdapter.handle(request, response, controllerMethod, pathVariables);
            } catch (Exception e) {
                // Log the error for better debugging
                e.printStackTrace();
                throw new ServletException("Erreur lors de l'execution de la methode du controleur", e);
            }
        } else {
            // This case should ideally not be reached if previous logic is correct
            response.setContentType("text/plain;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Internal server error: No controller method found for " + requestMethod + " " + path);
        }
    }
}
