package etu.sprint.web;

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

    private boolean matchUrl(String requestPath, String routePattern) {
        String[] requestParts = requestPath.split("/");
        String[] routeParts = routePattern.split("/");

        if (requestParts.length != routeParts.length) {
            return false;
        }

        for (int i = 0; i < routeParts.length; i++) {
            String routePart = routeParts[i];
            String requestPart = requestParts[i];

            if (!routePart.startsWith("{") && !routePart.endsWith("}") && !routePart.equals(requestPart)) {
                return false;
            }
        }
        return true;
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

        for (Map.Entry<String, ControllerMethod> entry : routeMap.entrySet()) {
            if (matchUrl(path, entry.getKey())) {
                controllerMethod = entry.getValue();
                break;
            }
        }

        if (controllerMethod != null) {
            try {
                Object controllerInstance = controllerMethod.controllerClass.getDeclaredConstructor().newInstance();
                Object returnValue = controllerMethod.method.invoke(controllerInstance);

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