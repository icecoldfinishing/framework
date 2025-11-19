package etu.sprint.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
    private final ControllerMethod controllerMethod;
    private final Pattern pattern;
    private final List<String> paramNames = new ArrayList<>();

    // Pattern pour trouver les variables de chemin comme {id}
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    public Route(String path, ControllerMethod controllerMethod) {
        this.controllerMethod = controllerMethod;
        this.pattern = compilePattern(path);
    }

    private Pattern compilePattern(String path) {
        StringBuilder regex = new StringBuilder("^");
        Matcher matcher = PARAM_PATTERN.matcher(path);
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
        regex.append("$");
        
        return Pattern.compile(regex.toString());
    }

    public ControllerMethod getControllerMethod() {
        return controllerMethod;
    }

    public Map<String, String> match(String requestPath) {
        Matcher matcher = pattern.matcher(requestPath);
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
