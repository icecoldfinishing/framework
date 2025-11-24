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

Sprint7
Sprint 7 — Reformulation claire et complète

Objectif principal :
Gérer et différencier les méthodes HTTP (GET, POST, PUT, DELETE, PATCH, etc.) pour une même URL. Permettre d’associer plusieurs handlers à la même route selon la méthode HTTP et dispatcher correctement (200, 404, 405, etc.).

Comportement attendu

Mapping: la table de routes devient Map<String, Map<HttpMethod, Handler>>

clé = URL (ex: /employe/123)

valeur = map méthode HTTP → handler (méthode de controller)

Annotations côté développeur: supporter @GetMapping, @PostMapping, @PutMapping, @DeleteMapping, et une @RequestMapping(method = ...) générique.

Dispatching: à la réception d’une requête:

on recherche la route (matching, y compris routes dynamiques /etudiant/{id} déjà gérées par sprint précédent),

si route inconnue → 404 Not Found,

si route connue mais méthode non implémentée → 405 Method Not Allowed (et retourner la liste des méthodes autorisées dans l’entête Allow),

sinon appeler le handler correspondant.

Bindings & corps:

pour GET: paramètres via query string / path variables / request params (comme implémenté auparavant),

pour POST / PUT / PATCH: parser le body (form-url-encoded, JSON si voulu) et binder aux paramètres de la méthode (réutiliser le mécanisme du sprint 6bis).

Priorité et collisions: si plusieurs méthodes pointent vers la même combinaison URL+HTTP method → erreur au démarrage (détecter et refuser doublons).

Logs / debugging: au démarrage, afficher les routes chargées avec méthodes associées (ex : GET /employe , POST /employe).

Tests / erreurs: cas de test pour:

URL inconnue → 404,

URL connue / méthode non implémentée → 405 + Allow,

URL connue / bonne méthode → exécution correcte,

collision annotation détectée au scan → fail build / init.

Critères d’acceptation (AC)

AC1: Le framework accepte annoter deux méthodes différentes sur la même URL tant qu’elles utilisent des méthodes HTTP différentes (ex: @GetMapping("/user") + @PostMapping("/user")).

AC2: Une requête GET /resource invoque seulement le handler GET et pas le POST.

AC3: Requête sur URL connue mais méthode non supportée renvoie 405 et en-tête Allow listant les méthodes valides.

AC4: Les doublons exacts (même URL + même méthode) sont détectés lors du scan et provoquent une erreur d’initialisation.

AC5: Le binding du body pour POST/PUT fonctionne avec les mêmes règles de nom/annotation que sprint 6bis.

Tâches techniques (implémentation)

Model route: remplacer la HashMap<String, Handler> par HashMap<String, HashMap<HttpMethod, Handler>>.

Annotations: créer/traiter @GetMapping, @PostMapping, @PutMapping, @DeleteMapping, et enrichir @RequestMapping(method=...).

Scanner: lors du scan automatique, pour chaque méthode annotée on extrait l’URL et la méthode HTTP puis on insère dans la map; vérifier collisions.

Dispatcher: modifier FrontServlet (ou équivalent) pour extraire HttpMethod (req.getMethod()), chercher handler, appliquer règles 404/405, invoquer handler.

Body parser: ajouter parser simple pour application/x-www-form-urlencoded et (optionnel) application/json pour binder aux paramètres.

Tests unitaires / intégration: simuler requêtes pour tous les AC.

Logs route map: afficher la liste des routes chargées au démarrage.