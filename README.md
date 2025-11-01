Framework
Contient toute la logique du framework : FrontServlet, AnnotationScanner, Mapping, annotations (@Controller, @Url, etc.).
Tu développes le moteur de ton mini Spring MVC ici.

Projet de test
Contient uniquement :
- Les contrôleurs d’exemple (DeptController, UserController…)
- Les vues (.jsp)
- Le web.xml qui configure FrontServlet
- Sert à tester que ton framework fonctionne

L’idée est de séparer le moteur (framework) de l’application réelle (test), exactement comme Spring Boot est séparé de ton application métier.