package etu.sprint.web;

import etu.sprint.model.ControllerMethod;
import etu.sprint.model.MethodInfo;
import etu.sprint.util.ClassScanner;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
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

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        ServletContext servletContext = getServletContext();
        Map<String, ControllerMethod> routeMap = (Map<String, ControllerMethod>) servletContext.getAttribute("routeMap");

        ControllerMethod controllerMethod = routeMap.get(path);

        if (controllerMethod != null) {
            try {
                Object controllerInstance = controllerMethod.controllerClass.getDeclaredConstructor().newInstance();
                Object returnValue = controllerMethod.method.invoke(controllerInstance);

                if (returnValue instanceof String) {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println(returnValue);
                }
                // D'autres types de retour (ex: ModelView) pourront être gérés ici
                
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