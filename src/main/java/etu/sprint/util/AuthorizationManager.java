package etu.sprint.util;

import etu.sprint.annotation.Authorized;
import etu.sprint.model.Session;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Gestionnaire d'autorisation pour vérifier les rôles et l'authentification.
 * Supporte la configuration via web.xml et fichier properties.
 */
public class AuthorizationManager {
    
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_ANONYM = "anonym";
    private static final String ROLE_ALL = "all";
    private static final String ROLE_AUTHENTICATED = "authenticated"; // any authenticated user
    
    private static final String SESSION_USER_KEY = "user";
    private static final String SESSION_ROLE_KEY = "role";
    
    private Properties configProperties;
    private ServletContext servletContext;
    
    public AuthorizationManager(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.configProperties = new Properties();
        loadConfiguration();
    }
    
    /**
     * Charge la configuration depuis web.xml et/ou un fichier properties.
     */
    private void loadConfiguration() {
        // Charger depuis un fichier properties si disponible
        try {
            InputStream is = servletContext.getResourceAsStream("/WEB-INF/auth.properties");
            if (is != null) {
                configProperties.load(is);
                is.close();
            }
        } catch (IOException e) {
            // Fichier properties optionnel, on continue sans
        }
        
        // Les paramètres peuvent aussi être dans web.xml via init-param
        // On les récupérera via le ServletContext si nécessaire
    }
    
    /**
     * Vérifie si l'utilisateur est autorisé à accéder à la méthode.
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @param methodAnnotation L'annotation @Authorized de la méthode
     * @return true si autorisé, false sinon
     * @throws IOException En cas d'erreur lors de l'écriture de la réponse
     */
    public boolean isAuthorized(HttpServletRequest request, HttpServletResponse response, 
                                Authorized methodAnnotation) throws IOException {
        if (methodAnnotation == null) {
            // Pas d'annotation = accès libre
            return true;
        }
        
        String[] allowedRoles = methodAnnotation.value();
        if (allowedRoles.length == 0 || (allowedRoles.length == 1 && allowedRoles[0].equals(ROLE_ALL))) {
            // "all" signifie pas de restriction
            return true;
        }
        
        Set<String> allowedRolesSet = new HashSet<>(Arrays.asList(allowedRoles));
        
        // Récupérer la session
        jakarta.servlet.http.HttpSession httpSession = request.getSession(false);
        Session session = httpSession != null ? new Session(httpSession) : null;
        
        // Vérifier si l'utilisateur est authentifié
        boolean isAuthenticated = isAuthenticated(session);
        String userRole = getUserRole(session);
        
        // Vérifier les rôles autorisés
        if (allowedRolesSet.contains(ROLE_ALL)) {
            return true;
        }
        
        if (allowedRolesSet.contains(ROLE_ANONYM)) {
            // "anonym" signifie que seuls les utilisateurs non authentifiés peuvent accéder
            if (!isAuthenticated) {
                return true;
            } else {
                sendUnauthorizedResponse(response, "Cette ressource est réservée aux utilisateurs non authentifiés");
                return false;
            }
        }
        
        if (allowedRolesSet.contains(ROLE_AUTHENTICATED)) {
            // Any authenticated user regardless of specific role
            if (isAuthenticated) {
                return true;
            } else {
                sendUnauthorizedResponse(response, "Authentification requise");
                return false;
            }
        }

        if (allowedRolesSet.contains(ROLE_ADMIN)) {
            // "admin" nécessite authentification et rôle admin
            if (!isAuthenticated) {
                sendUnauthorizedResponse(response, "Authentification requise");
                return false;
            }
            if (ROLE_ADMIN.equals(userRole)) {
                return true;
            } else {
                sendForbiddenResponse(response, "Accès refusé : rôle admin requis");
                return false;
            }
        }
        
        // Vérifier si le rôle de l'utilisateur est dans la liste des rôles autorisés
        if (isAuthenticated && userRole != null && allowedRolesSet.contains(userRole)) {
            return true;
        }
        
        // Par défaut, si l'utilisateur n'a pas le bon rôle
        if (!isAuthenticated) {
            sendUnauthorizedResponse(response, "Authentification requise");
        } else {
            sendForbiddenResponse(response, "Accès refusé : rôle insuffisant");
        }
        return false;
    }
    
    /**
     * Vérifie si l'utilisateur est authentifié.
     * @param session La session de l'utilisateur
     * @return true si authentifié, false sinon
     */
    private boolean isAuthenticated(Session session) {
        if (session == null) {
            return false;
        }
        Object user = session.get(SESSION_USER_KEY);
        return user != null;
    }
    
    /**
     * Récupère le rôle de l'utilisateur depuis la session.
     * @param session La session de l'utilisateur
     * @return Le rôle de l'utilisateur, ou null si non authentifié
     */
    private String getUserRole(Session session) {
        if (session == null) {
            return null;
        }
        Object role = session.get(SESSION_ROLE_KEY);
        if (role != null) {
            return role.toString();
        }
        // Si pas de rôle explicite mais utilisateur authentifié, on peut retourner un rôle par défaut
        // ou null selon la configuration
        String defaultRole = configProperties.getProperty("default.role", null);
        return defaultRole;
    }
    
    /**
     * Envoie une réponse 401 Unauthorized.
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("<h1>401 Unauthorized</h1><p>" + message + "</p>");
    }
    
    /**
     * Envoie une réponse 403 Forbidden.
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("<h1>403 Forbidden</h1><p>" + message + "</p>");
    }
    
    /**
     * Méthode utilitaire pour définir l'utilisateur dans la session (pour l'authentification).
     * @param session La session
     * @param username Le nom d'utilisateur
     * @param role Le rôle de l'utilisateur
     */
    public static void setAuthenticatedUser(Session session, String username, String role) {
        if (session != null) {
            session.set(SESSION_USER_KEY, username);
            session.set(SESSION_ROLE_KEY, role);
        }
    }
    
    /**
     * Méthode utilitaire pour déconnecter l'utilisateur.
     * @param session La session
     */
    public static void logout(Session session) {
        if (session != null) {
            session.remove(SESSION_USER_KEY);
            session.remove(SESSION_ROLE_KEY);
        }
    }
    
    /**
     * Récupère le nom d'utilisateur depuis la session.
     * @param session La session
     * @return Le nom d'utilisateur ou null
     */
    public static String getCurrentUser(Session session) {
        if (session != null) {
            Object user = session.get(SESSION_USER_KEY);
            return user != null ? user.toString() : null;
        }
        return null;
    }
    
    /**
     * Récupère le rôle de l'utilisateur depuis la session.
     * @param session La session
     * @return Le rôle ou null
     */
    public static String getCurrentUserRole(Session session) {
        if (session != null) {
            Object role = session.get(SESSION_ROLE_KEY);
            return role != null ? role.toString() : null;
        }
        return null;
    }

    /**
     * Récupère la liste des rôles configurés pour l'application.
     * Source: WEB-INF/auth.properties -> custom.roles=role1,role2
     * @return Ensemble des rôles disponibles; défaut: ["user", "admin"].
     */
    public Set<String> getConfiguredRoles() {
        Set<String> roles = new HashSet<>();
        String configured = configProperties.getProperty("custom.roles", null);
        if (configured != null && !configured.isBlank()) {
            Arrays.stream(configured.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(roles::add);
        }
        if (roles.isEmpty()) {
            roles.add("user");
            roles.add("admin");
        }
        return roles;
    }

    /**
     * Récupère le rôle par défaut depuis la configuration, sinon "user".
     */
    public String getDefaultRole() {
        String def = configProperties.getProperty("default.role", null);
        return (def != null && !def.isBlank()) ? def.trim() : "user";
    }
}


