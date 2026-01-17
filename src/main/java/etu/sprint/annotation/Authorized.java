package etu.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour protéger les méthodes de controller selon les rôles.
 * Les rôles possibles sont : "admin", "anonym", "all"
 * - "admin" : nécessite un utilisateur authentifié avec le rôle admin
 * - "anonym" : accessible uniquement aux utilisateurs non authentifiés
 * - "all" : accessible à tous (pas de restriction)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Authorized {
    /**
     * Les rôles autorisés pour accéder à cette méthode.
     * Par défaut, "all" signifie que tout le monde peut accéder.
     * @return Tableau des rôles autorisés
     */
    String[] value() default {"all"};
}

