package etu.sprint.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import etu.sprint.annotation.AnnotationController;
import etu.sprint.annotation.GetMethode;
import etu.sprint.utility.Mapping;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@AnnotationController("/sprint")
public class SprintController {

    @GetMethode("/routes")
    public void listRoutes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        ServletContext context = request.getServletContext();
        Map<String, Mapping> urlMappings = (Map<String, Mapping>) context.getAttribute("urlMappings");

        out.println("<html><head><title>Framework Routes</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
        out.println("table { border-collapse: collapse; width: 60%; }");
        out.println("th, td { border: 1px solid #dddddd; text-align: left; padding: 8px; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println("h1 { color: #333; }");
        out.println("</style>");
        out.println("</head><body>");
        out.println("<h1>Available Routes</h1>");
        
        if (urlMappings == null || urlMappings.isEmpty()) {
            out.println("<p>No routes found.</p>");
        } else {
            out.println("<table>");
            out.println("<tr><th>URL Path</th><th>Controller Class</th><th>Method Name</th></tr>");
            for (Map.Entry<String, Mapping> entry : urlMappings.entrySet()) {
                out.println("<tr>");
                out.println("<td>" + entry.getKey() + "</td>");
                out.println("<td>" + entry.getValue().getClassName() + "</td>");
                out.println("<td>" + entry.getValue().getMethodName() + "</td>");
                out.println("</tr>");
            }
            out.println("</table>");
        }
        
        out.println("</body></html>");
    }
}
