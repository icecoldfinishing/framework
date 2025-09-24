package etu.sprint.framework;

import etu.sprint.framework.utility.AnnotationScanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServlet extends HttpServlet {

    private Map<String, Mapping> urlMappings;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            String packageName = config.getInitParameter("scan-package");
            if (packageName == null || packageName.isEmpty()) {
                throw new ServletException("The 'scan-package' init parameter is missing or empty in web.xml");
            }
            this.urlMappings = AnnotationScanner.scanPackage(packageName);
            // Log for debugging
            config.getServletContext().log("Framework Sprint 1: " + this.urlMappings.size() + " URL mappings found.");
            for(Map.Entry<String, Mapping> entry : this.urlMappings.entrySet()){
                config.getServletContext().log("Framework Sprint 1: Mapping " + entry.getKey() + " to " + entry.getValue().getClassName() + "." + entry.getValue().getMethodName());
            }
        } catch (Exception e) {
            throw new ServletException("Failed to scan for annotations", e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = requestURI.substring(contextPath.length());

        Mapping mapping = this.urlMappings.get(url);

        if (mapping != null) {
            try {
                Class<?> controllerClass = Class.forName(mapping.getClassName());
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                Method method = controllerClass.getMethod(mapping.getMethodName());
                Object result = method.invoke(controllerInstance);

                if (result instanceof ModelView) {
                    ModelView modelView = (ModelView) result;
                    for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/views/" + modelView.getViewName());
                    dispatcher.forward(request, response);
                } else {
                    // Handle cases where the controller returns something else (e.g., a String for REST APIs)
                    response.setContentType("text/html;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.println("<h1>Framework Response</h1>");
                        out.println("<p>Controller returned an unexpected type.</p>");
                        if (result != null) {
                            out.println("<p>" + result.toString() + "</p>");
                        }
                    }
                }

            } catch (Exception e) {
                throw new ServletException("Error invoking method for URL: " + url, e);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No mapping found for this URL: " + url);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "FrontServlet for the custom framework.";
    }
}