package etu.sprint.web;

import etu.sprint.model.ControllerMethod;
import etu.sprint.model.MethodInfo;
import etu.sprint.util.ClassScanner;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FrontServlet extends HttpServlet {

    private Map<String, ControllerMethod> routeMap;
    private Map<String, List<MethodInfo>> controllerInfo;

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
            this.routeMap = scanner.getRouteMap();
            this.controllerInfo = scanner.getControllerInfo();
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

        ControllerMethod controllerMethod = routeMap.get(path);

        if (controllerMethod != null) {
            response.setContentType("text/html;charset=UTF-8");
            java.io.PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\">");
            out.println("<head>");
            out.println("    <meta charset=\"UTF-8\">");
            out.println("    <title>Controller Info</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("    <p>URL Path: " + path + "</p>");

            String controllerName = controllerMethod.controllerClass.getName();
            List<MethodInfo> methods = controllerInfo.get(controllerName);

            out.println("    <p>Controller Class: " + controllerName + "</p>");

            out.println("    <p>Methods:</p>");
            if (methods != null) {
                for (MethodInfo methodInfo : methods) {
                    out.println("    <p>- " + methodInfo.getSignature() + "</p>");
                }
            } else {
                out.println("    <p>No methods found for this controller.</p>");
            }
            out.println("</body>");
            out.println("</html>");
        } else {
            response.setContentType("text/plain;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("URL inconnu pour cette url: " + path);
        }
    }
}