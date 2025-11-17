package etu.sprint.web;

import etu.sprint.annotation.RequestParam;
import etu.sprint.model.ControllerMethod;
import etu.sprint.model.ModelView;
import etu.sprint.util.ClassScanner;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class FrontServlet extends HttpServlet {

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

            ServletContext servletContext = getServletContext();
            servletContext.setAttribute("routeMap", scanner.getRouteMap());
            servletContext.setAttribute("controllerInfo", scanner.getControllerInfo());

        } catch (Exception e) {
            throw new ServletException("Failed to initialize FrontServlet", e);
        }
    }

    private Map<String, String> extractPathVariables(String requestPath, String routePattern) {
        Map<String, String> pathVariables = new HashMap<>();
        String[] requestParts = requestPath.split("/");
        String[] routeParts = routePattern.split("/");

        if (requestParts.length != routeParts.length) {
            return null;
        }

        for (int i = 0; i < routeParts.length; i++) {
            String routePart = routeParts[i];
            String requestPart = requestParts[i];

            if (routePart.startsWith("{") && routePart.endsWith("}")) {
                String varName = routePart.substring(1, routePart.length() - 1);
                pathVariables.put(varName, requestPart);
            } else if (!routePart.equals(requestPart)) {
                return null;
            }
        }
        return pathVariables;
    }

    private Object convertStringValue(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType == String.class) {
            return value;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        ServletContext servletContext = getServletContext();
        Map<String, ControllerMethod> routeMap = (Map<String, ControllerMethod>) servletContext.getAttribute("routeMap");

        ControllerMethod controllerMethod = null;
        Map<String, String> pathVariables = null;

        for (Map.Entry<String, ControllerMethod> entry : routeMap.entrySet()) {
            pathVariables = extractPathVariables(path, entry.getKey());
            if (pathVariables != null) {
                controllerMethod = entry.getValue();
                break;
            }
        }

        if (controllerMethod != null) {
            try {
                Object controllerInstance = controllerMethod.controllerClass.getDeclaredConstructor().newInstance();
                Method method = controllerMethod.method;
                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    String paramName;
                    if (parameter.isAnnotationPresent(RequestParam.class)) {
                        paramName = parameter.getAnnotation(RequestParam.class).value();
                    } else {
                        paramName = parameter.getName();
                    }

                    String paramValue = pathVariables.get(paramName);
                    args[i] = convertStringValue(paramValue, parameter.getType());
                }

                Object returnValue = method.invoke(controllerInstance, args);

                if (returnValue instanceof String) {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println(returnValue);
                } else if (returnValue instanceof ModelView) {
                    ModelView mv = (ModelView) returnValue;

                    for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getView());
                    dispatcher.forward(request, response);
                }

            } catch (Exception e) {
                throw new ServletException("Erreur lors de l'execution de la methode du controleur", e);
            }
        } else {
            response.setContentType("text/plain;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("URL inconnu pour cette url: " + path);
        }
    }
}