package etu.sprint.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteMatcher {
    private final Pattern pattern; //REGEX compilé servant à reconnaître l’URL réelle.
    private final List<String> paramNames = new ArrayList<>(); //liste des noms de variables trouvés dans { } (ex: ["id"]).

    // Pattern pour trouver les variables de chemin entre { }
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    public RouteMatcher(String path) {
        this.pattern = compilePattern(path);
    }

    private Pattern compilePattern(String path) {
        StringBuilder regex = new StringBuilder("^");
        Matcher matcher = PARAM_PATTERN.matcher(path);  //outil pour trouver les {param}.
        int lastIndex = 0;

        while (matcher.find()) {
            // Ajoute la partie littérale du chemin avant le paramètre
            regex.append(Pattern.quote(path.substring(lastIndex, matcher.start())));
            
            // Extrait le nom du paramètre et l'ajoute à la liste
            String paramName = matcher.group(1);
            paramNames.add(paramName);
            
            // Ajoute un groupe de capture regex pour la valeur du paramètre
            regex.append("([^/]+)");
            
            lastIndex = matcher.end();
        }
        
        // Ajoute la partie restante du chemin après le dernier paramètre
        regex.append(Pattern.quote(path.substring(lastIndex)));
        regex.append("$"); //garantit un match exact.
        
        return Pattern.compile(regex.toString()); //Compile en Pattern Java
    }

    public Map<String, String> match(String requestPath) {
        Matcher matcher = pattern.matcher(requestPath);
        //vérifier si ce chemin correspond à la route
        if (matcher.matches()) {
            Map<String, String> pathVariables = new HashMap<>();
            for (int i = 0; i < paramNames.size(); i++) {
                // Le groupe 0 est le match complet, les groupes de capture commencent à 1
                pathVariables.put(paramNames.get(i), matcher.group(i + 1));
            }
            return pathVariables;
        }
        return null;
    }
}