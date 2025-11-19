package etu.sprint.web;

import etu.sprint.handler.HandlerAdapter;
import etu.sprint.model.ControllerMethod;
import etu.sprint.util.ClassScanner;
import etu.sprint.util.UrlUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class FrontServlet extends HttpServlet {

    private HandlerAdapter handlerAdapter;

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

            this.handlerAdapter = new HandlerAdapter();

        } catch (Exception e) {
            throw new ServletException("Failed to initialize FrontServlet", e);
        }
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
            pathVariables = UrlUtil.extractPathVariables(path, entry.getKey());
            if (pathVariables != null) {
                controllerMethod = entry.getValue();
                break;
            }
        }

        if (controllerMethod != null) {
            try {
                handlerAdapter.handle(request, response, controllerMethod, pathVariables);
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