package etu.sprint.framework.utility;

import etu.sprint.framework.Mapping;
import etu.sprint.framework.annotation.Controller;
import etu.sprint.framework.annotation.Url;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Cette classe sert à scanner un package donné pour détecter
 * toutes les classes et méthodes annotées avec @Controller et @Url.
 * Elle génère ensuite une Map qui associe une URL (définie dans @Url)
 * à un objet Mapping (qui contient le nom de la classe et le nom de la méthode).
 */
public class AnnotationScanner {

    /**
     * Scanne un package donné et retourne toutes les correspondances URL -> Mapping.
     *
     * @param packageName le nom du package à scanner (ex: "etu.sprint.controllers")
     * @return une Map qui associe l’URL à un Mapping (classe + méthode)
     * @throws Exception si une erreur survient (package introuvable, etc.)
     */
    public static Map<String, Mapping> scanPackage(String packageName) throws Exception {
        // La map finale qui contiendra URL -> Mapping
        Map<String, Mapping> urlMappings = new HashMap<>();

        // On récupère le chargeur de classes actuel
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // On transforme le package en chemin (ex: "etu.sprint.framework" → "etu/sprint/framework")
        String path = packageName.replace('.', '/');

        // On récupère la ressource (dossier du package dans le classpath)
        URL resource = classLoader.getResource(path);

        // Si le package n’existe pas, on lance une erreur
        if (resource == null) {
            throw new IllegalArgumentException("Package not found: " + packageName);
        }

        // On récupère le dossier du package
        File directory = new File(resource.toURI());

        // On parcourt tous les fichiers du dossier
        for (File file : directory.listFiles()) {
            // On ne garde que les fichiers .class (donc les classes compilées)
            if (file.getName().endsWith(".class")) {

                // On construit le nom complet de la classe (package + nom)
                // Exemple: fichier "UserController.class" → "etu.sprint.framework.UserController"
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);

                // On charge la classe en mémoire
                Class<?> clazz = Class.forName(className);

                // On vérifie si la classe est annotée avec @Controller
                if (clazz.isAnnotationPresent(Controller.class)) {

                    // On parcourt toutes les méthodes publiques de la classe
                    for (java.lang.reflect.Method method : clazz.getMethods()) {

                        // Si la méthode est annotée avec @Url
                        if (method.isAnnotationPresent(Url.class)) {

                            // On récupère l’annotation et la valeur de l’URL
                            Url urlAnnotation = method.getAnnotation(Url.class);
                            String url = urlAnnotation.value();

                            // On vérifie si l’URL est déjà utilisée ailleurs
                            if (urlMappings.containsKey(url)) {
                                throw new IllegalStateException("Duplicate URL mapping found: " + url);
                            }

                            // On enregistre l’URL avec le Mapping (classe + méthode)
                            urlMappings.put(url, new Mapping(clazz.getName(), method.getName()));
                        }
                    }
                }
            }
        }

        // On retourne la Map finale : URL -> (classe + méthode)
        return urlMappings;
    }
}
