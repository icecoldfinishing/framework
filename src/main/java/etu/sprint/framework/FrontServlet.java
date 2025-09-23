package etu.sprint.framework;

import etu.sprint.framework.utility.AnnotationScanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
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
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String requestURI = request.getRequestURI();
            String contextPath = request.getContextPath();
            String url = requestURI.substring(contextPath.length());

            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet FrontServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Framework Response</h1>");
            out.println("<p><b>Requested URL:</b> " + url + "</p>");

            Mapping mapping = this.urlMappings.get(url);

            if (mapping != null) {
                out.println("<p style='color:green;'><b>SUCCESS:</b> URL found.</p>");
                out.println("<p><b>Mapped to Controller:</b> " + mapping.getClassName() + "</p>");
                out.println("<p><b>Mapped to Method:</b> " + mapping.getMethodName() + "</p>");
            } else {
                out.println("<p style='color:red;'><b>ERROR 404:</b> No mapping found for this URL.</p>");
            }

            out.println("</body>");
            out.println("</html>");
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