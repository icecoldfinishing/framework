package etu.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer les paramètres de type Session dans les méthodes de controller.
 * Permet au framework d'injecter automatiquement l'objet Session pour gérer la session HTTP.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Session {
}

