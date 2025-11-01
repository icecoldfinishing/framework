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

public class FrontController extends HttpServlet {

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
            throw new ServletException("Failed to initialize FrontController", e);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if ("/framework-info".equals(path)) {
            response.setContentType("text/html;charset=UTF-8");
            request.setAttribute("controllerInfo", controllerInfo);
            request.getRequestDispatcher("/views/framework-info.jsp").forward(request, response);
            return;
        }

        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        ControllerMethod controllerMethod = routeMap.get(path);

        if (controllerMethod != null) {
            response.setContentType("text/plain;charset=UTF-8");
            java.io.PrintWriter out = response.getWriter();

            out.println("URL: " + path);
            String controllerName = controllerMethod.controllerClass.getName();
            List<MethodInfo> methods = controllerInfo.get(controllerName);

            out.println(controllerName);
            if (methods != null) {
                for (MethodInfo methodInfo : methods) {
                    out.println(methodInfo.getSignature());
                }
            }
        } else {
            response.setContentType("text/plain;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("URL inconnu pour cette url: " + path);
        }
    }
}