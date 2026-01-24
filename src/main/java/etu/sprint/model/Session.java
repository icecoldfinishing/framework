package etu.sprint.model;

import jakarta.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitaire pour gérer les sessions HTTP.
 * Permet aux controllers de récupérer, ajouter et supprimer des attributs de session.
 */
public class Session {
    private HttpSession httpSession;

    public Session(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    /**
     * Récupère un attribut de session par sa clé.
     * @param key La clé de l'attribut
     * @return La valeur de l'attribut, ou null si non trouvé
     */
    public Object get(String key) {
        if (httpSession == null) {
            return null;
        }
        return httpSession.getAttribute(key);
    }

    /**
     * Récupère un attribut de session avec un type spécifique.
     * @param key La clé de l'attribut
     * @param clazz Le type attendu
     * @return La valeur castée au type demandé, ou null si non trouvé
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Ajoute ou modifie un attribut de session.
     * @param key La clé de l'attribut
     * @param value La valeur à stocker
     */
    public void set(String key, Object value) {
        if (httpSession != null) {
            httpSession.setAttribute(key, value);
        }
    }

    /**
     * Supprime un attribut de session.
     * @param key La clé de l'attribut à supprimer
     */
    public void remove(String key) {
        if (httpSession != null) {
            httpSession.removeAttribute(key);
        }
    }

    /**
     * Vérifie si un attribut existe dans la session.
     * @param key La clé de l'attribut
     * @return true si l'attribut existe, false sinon
     */
    public boolean contains(String key) {
        if (httpSession == null) {
            return false;
        }
        return httpSession.getAttribute(key) != null;
    }

    /**
     * Récupère tous les attributs de session sous forme de Map.
     * @return Une Map contenant tous les attributs de session
     */
    public Map<String, Object> getAll() {
        Map<String, Object> sessionMap = new HashMap<>();
        if (httpSession != null) {
            Enumeration<String> attributeNames = httpSession.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String key = attributeNames.nextElement();
                sessionMap.put(key, httpSession.getAttribute(key));
            }
        }
        return sessionMap;
    }

    /**
     * Invalide la session (déconnexion).
     */
    public void invalidate() {
        if (httpSession != null) {
            httpSession.invalidate();
        }
    }

    /**
     * Récupère l'ID de la session.
     * @return L'ID de la session
     */
    public String getId() {
        if (httpSession == null) {
            return null;
        }
        return httpSession.getId();
    }

    /**
     * Récupère la session HTTP sous-jacente (pour usage avancé).
     * @return La HttpSession
     */
    public HttpSession getHttpSession() {
        return httpSession;
    }
}


