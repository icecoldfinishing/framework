package etu.sprint.web;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = requestURI.substring(contextPath.length());

        // 🔹 Ressources statiques
        if (url.startsWith("/html/") || url.startsWith("/views/") || url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".png")) {
            try (java.io.InputStream is = getServletContext().getResourceAsStream(url)) {
                if (is != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        response.getOutputStream().write(buffer, 0, bytesRead);
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ressource non trouvée : " + url);
                }
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de la lecture de la ressource : " + url);
            }
            return;
        }

        // 🔹 URL inconnue → 404
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<h2>URL inconnue pour cette URL : " + url + "</h2>");
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    @Override
    public String getServletInfo() {
        return "FrontController for the custom framework.";
    }
}
