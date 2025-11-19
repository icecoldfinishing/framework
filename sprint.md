SPRINT 1
Framework sans Spring Boot. Un projet framework + un projet test séparé (framework en JAR). Git: branche sprint1. FrontServlet interceptant toutes les requêtes via web.xml. 3 annotations (classe, attribut, méthode). Classe utilitaire pour lire les annotations. FrontServlet affiche l’URL. Projet test utilise le JAR, pas de servlet dupliqué. Objectif: peu importe l’URL, le framework répond via FrontServlet.

SPRINT 2 (URL mapping)
Support uniquement des URLs annotées. HashMap url -> (class, method). Scan du classpath. Détection @Controller + méthode annotée. URL non annotée = 404.

SPRINT 3 (scan auto)
Scan automatique au démarrage dans init(). Récupère toutes les classes annotées. Liste des URL supportées. Si URL inconnue → message d’erreur.

SPRINT 4 (execution)
Exécution dynamique de la méthode associée à l’URL. Si le retour est String, affichage avec PrintWriter.

SPRINT 4bis (ModelView)
Ajout de ModelView(String view). Si retour ModelView → forward JSP (dispatcher). Si retour String → PrintWriter. Exemple: return new ModelView("test.jsp").

SPRINT 5 (MVC complet)
Permet au développeur de renvoyer une vue depuis un controller (ex: listEmploye → JSP). Le framework doit gérer ce workflow.

SPRINT 6 (form)
Formulaire HTML → controller. Ex: save() appelé automatiquement quand les données arrivent.

MERGE (URL dynamique)
Support des routes REST comme /etudiant/{id}. Extraction automatique du paramètre dans la méthode. Si nom identique → assignation automatique. Sinon null sauf si @RequestParam.

SPRINT 6bis (paramètres avancés)
Gestion de {} sans request.getParameter(). Mapping automatique d’arguments par nom ou annotation. @RequestParam prioritaire.

SPRINT 6tier
On prend le id via navigateur si meme nom le variable et sur l url donc on doit faire un petit regex pour cette gestion