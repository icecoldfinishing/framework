package etu.sprint.web;

import etu.sprint.handler.HandlerAdapter;
import etu.sprint.model.ControllerMethod;
import etu.sprint.model.Route;
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

    private HandlerAdapter handlerAdapter;
    private List<Route> routes;

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

            // Stocke la liste des routes
            this.routes = scanner.getRoutes(); 
            
            // Garde les informations du contrôleur pour d'éventuels besoins de débogage ou d'introspection
            ServletContext servletContext = getServletContext();
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

        ControllerMethod controllerMethod = null;
        Map<String, String> pathVariables = null;

        // Itère sur les routes et utilise le matching REGEX
        for (Route route : this.routes) {
            pathVariables = route.match(path);
            if (pathVariables != null) {
                controllerMethod = route.getControllerMethod();
                break;
            }
        }

        if (controllerMethod != null) {
            try {
                handlerAdapter.handle(request, response, controllerMethod, pathVariables);
            } catch (Exception e) {
                // Log l'erreur pour un meilleur débogage
                e.printStackTrace(); 
                throw new ServletException("Erreur lors de l'execution de la methode du controleur", e);
            }
        } else {
            response.setContentType("text/plain;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("URL inconnu pour cette url: " + path);
        }
    }
}