SPRINT 1
Framework sans Spring Boot. Un projet framework + un projet test séparé (framework en JAR). Git: branche sprint1. FrontServlet interceptant toutes les requêtes via web.xml. 3 annotations (classe, attribut, méthode). Classe utilitaire pour lire les annotations. FrontServlet affiche l’URL. Projet test utilise le JAR, pas de servlet dupliqué. Objectif: peu importe l’URL, le framework répond via FrontServlet.

SPRINT 2 (URL mapping)
Support uniquement des URLs annotées. HashMap url -> (class, method). Scan du classpath. Détection @Controller + méthode annotée. URL non annotée = 404.

SPRINT 3 (scan auto)
Scan automatique au démarrage dans init(). Récupère toutes les classes annotées. Liste des URL supportées. Si URL inconnue → message d’erreur.

SPRINT 3bis
Gestion de {variable dynamique} sur l url , permission aux frontServlet de get la variable

SPRINT 4 (execution)
Exécution dynamique de la méthode associée à l’URL. Si le retour est String, affichage avec PrintWriter.

SPRINT 4bis (ModelView)
Ajout de ModelView(String view). Si retour ModelView → forward JSP (dispatcher). Si retour String → PrintWriter. Exemple: return new ModelView("test.jsp").

SPRINT 5 (MVC complet)
Permet au développeur de renvoyer une vue depuis un controller (ex: listEmploye → JSP). Le framework doit gérer ce workflow.

SPRINT 6 (form)
Url code http , injection directe si variable url meme que dans la fonction de controller
MERGE (URL dynamique)
Support des routes REST comme /etudiant/{id}. Extraction automatique du paramètre dans la méthode. Si nom identique → assignation automatique. Sinon null sauf si @RequestParam.

SPRINT 6bis (paramètres avancés)
Gestion de {} sans request.getParameter(). Mapping automatique d’arguments par nom ou annotation. @RequestParam prioritaire.

SPRINT 6tier
On prend le id via navigateur si meme nom le variable et sur l url donc on doit faire un petit regex pour cette gestion

SPRINT 7
Objectif principal :
Gérer et différencier les méthodes HTTP (GET, POST, PUT, DELETE, PATCH, etc.) pour une même URL. Permettre d’associer plusieurs handlers à la même route selon la méthode HTTP et dispatcher correctement (200, 404, 405, etc.).

SPRINT 8
Objectif :
Poursuivre le développement du Sprint 6 en traitant les données envoyées depuis la vue vers le contrôleur.
Détails :
    Si le contrôleur reçoit une carte de type Map<String, Object>, le FrontServlet doit gérer cette carte plutôt que des chaînes de caractères lors de l'invocation de la méthode.
    Lorsqu'une Map est détectée dans le FrontServlet, une nouvelle carte est créée en boucle via getValues() pour ajouter les valeurs.
    Par exemple, dans le contrôleur, nous aurons une méthode définie comme :
    public ModelView(Map<String, Object> data)
Si nous avons un formulaire avec une méthode annotée @PostMapping (comme save), le FrontServlet identifie que c’est cette méthode qui est à invoquer.
Il vérifie ensuite les paramètres de la méthode : si une Map<String, Object> est présente, il boucle à travers les entrées de la carte pour récupérer les valeurs.
Les valeurs sont considérées comme des clés, et une nouvelle HashMap est créée pour remplir les données correspondantes pour chaque ligne.
Les données de la requête sont ainsi transférées dans cette nouvelle carte, sans avoir besoin de caster, mais en les copiant simplement.
Nous nous orientons vers l'utilisation de Value Objects au lieu de simples chaînes de caractères afin de permettre le traitement des checkboxes et d'autres formats de données.

SPRINT 8bis
La methode cote view vers controller ne pass plus par dataMap mais directement object par exemple function(Object obj)

SPRINT 9
Objectif du sprint :
Permettre au framework de gérer des méthodes de controller qui retournent automatiquement du JSON, afin d’exposer une API REST.
1 Nouvelle annotation : @RestAPI (ou @WebAPI)
On crée une annotation au niveau méthode.
Une méthode annotée REST ne renvoie pas de ModelView, mais directement des données (objets, listes, map, etc.).
Le framework doit détecter cette annotation lors du scan (comme pour les URL du sprint précédent).
2️ Gestion du retour JSON
Lorsque la méthode annotée @RestAPI est invoquée :
Si le type de retour ≠ ModelView, alors le framework :
Convertit automatiquement le retour en JSON.
Envoie la réponse JSON dans le HttpServletResponse (writer).
Si le type de retour est ModelView, on ne le convertit pas (car ce n’est pas REST).
3️ Format JSON standardisé
Pour respecter un format REST cohérent, le framework construit un objet JSON avec 4 champs :
{
  "status": "success" ou "error",
  "code": 200, 400, 404, 500,
  "message": "description textuelle",
  "data": ... // objet ou liste retournée
}
Fonctionnement dans le FrontServlet
Lors du traitement de la requête :
Retrouver la méthode contrôleur correspondante via l’URL + la méthode HTTP (Sprint 7).
Vérifier si la méthode a l’annotation @RestAPI.
Si OUI :
Exécuter la méthode.
Sérialiser son retour en JSON.
Écrire directement dans la réponse sans passer par un JSP.
Si NON :
Gérer comme dans sprint4/5 (ModelView + JSP).