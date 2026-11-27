# Todo API — Spring Boot / PostgreSQL / Render

Projet pédagogique Java 21, Spring Boot 3.5.8, Maven, JPA et PostgreSQL. API publique sans authentification : ne stocker aucune donnée personnelle et supprimer le service après les essais. Les dates sont au format ISO AAAA-MM-JJ. Aucun frontend nécessaire.

## 1. Structure

- `controller/TaskController.java` : routes CRUD.
- `controller/PingController.java` : test HTTP sans requête SQL.
- `service/TaskService.java` : interface métier.
- `service/TaskServiceImpl.java` : transactions, conversion modèle/DTO.
- `repository/TaskRepository.java` : JpaRepository.
- `model/Task.java` : id, tache, date.
- `dto/TaskDto.java` : tache, date uniquement, validation.
- `database/create-local.sql` : création du rôle et de la base locale avec psql.
- `database/schema.sql` : création de la table.
- `src/main/resources/schema.sql` : initialisation automatique au démarrage.
- `Dockerfile` : build Maven et exécution Java 21, mémoire JVM limitée.

## 2. Tester localement (facultatif)

Sous Debian/WSL, avec PostgreSQL déjà installé, ne pas démarrer un autre serveur sur le port 5432. Installer Java 21 et Maven si nécessaire. Vérifier `java -version`, `mvn -version`, `sudo systemctl status postgresql`.

Depuis la racine du projet :

```bash
sudo -u postgres psql -v ON_ERROR_STOP=1 -f "$PWD/database/create-local.sql"
```

Choisir un mot de passe lorsque psql le demande. Ce script est à exécuter une seule fois. Si le rôle ou la base existe déjà, réutiliser les identifiants existants ou choisir un autre nom ; ne pas supprimer une base existante. Si postgres ne peut pas lire le dossier, copier les deux scripts SQL dans un dossier accessible.

```bash
export DB_URL='jdbc:postgresql://localhost:5432/todo'
export DB_USER=todo
read -rsp 'Mot de passe PostgreSQL : ' DB_PASSWORD; echo
export DB_PASSWORD
mvn clean test
mvn spring-boot:run
```

Ouvrir http://localhost:8080/api/ping puis http://localhost:8080/api/tasks. La liste initiale est `[]`. La table est créée automatiquement si elle manque ; Hibernate vérifie ensuite le schéma sans supprimer les données.

## 3. Publier le projet sur GitHub ou GitLab

Créer un dépôt vide, puis depuis la racine du projet :

```bash
git init
git add .
git commit -m "Initial Todo API"
git branch -M main
git remote add origin URL_DE_TON_DEPOT
git push -u origin main
```

Remplacer URL_DE_TON_DEPOT par l'adresse réelle. Ne jamais ajouter les mots de passe au dépôt. Si le dépôt contient déjà un README, le cloner d'abord et copier les fichiers dedans.

## 4. Créer PostgreSQL sur Render

1. Ouvrir https://dashboard.render.com/ et créer un compte.
2. New > PostgreSQL (les libellés peuvent évoluer).
3. Nom : `todo-db`, base : `todo`, utilisateur : `todo` si ces champs sont disponibles.
4. Choisir une région, par exemple Frankfurt si disponible, et le plan Free.
5. Créer la base et attendre le statut Available.
6. Dans les informations de connexion, relever **Internal Database URL**, hostname interne, port, database, username et password. Garder ces valeurs privées.

Render crée déjà la base et le rôle : ne pas exécuter `create-local.sql` sur Render. Spring Boot exécutera automatiquement `schema.sql`. Si souhaité, utiliser psql avec l'URL externe fournie par Render pour exécuter manuellement `database/schema.sql` (ne pas partager cette URL) :

```bash
read -rsp 'External Database URL : ' DATABASE_URL; echo
psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f database/schema.sql
unset DATABASE_URL
```

L'offre PostgreSQL gratuite est temporaire (30 jours selon les conditions discutées) ; vérifier le plan affiché avant création et exporter les données avant expiration. Voir https://render.com/docs/free et https://render.com/pricing.

## 5. Déployer le backend sur Render

1. New > Web Service, connecter GitHub ou GitLab et sélectionner le dépôt.
2. Nom : `todo-api-demo` (ou un nom unique disponible).
3. Branch : `main`. Runtime/Language : Docker. Root directory : vide si pom.xml est à la racine. Dockerfile path : `./Dockerfile`.
4. Région : exactement la même que PostgreSQL. Instance : Free.
5. Dans Environment, ajouter les variables suivantes :

| Variable | Valeur |
|---|---|
| DB_URL | `jdbc:postgresql://HOST_INTERNE:5432/todo` |
| DB_USER | utilisateur indiqué par Render |
| DB_PASSWORD | mot de passe indiqué par Render |

Remplacer HOST_INTERNE et todo par les valeurs réelles. Ne pas coller directement une URL `postgresql://user:password@host/db` dans DB_URL : JDBC exige le préfixe `jdbc:postgresql://`. Pour une connexion externe, utiliser le hostname externe et `?sslmode=require` (préférer la vérification du certificat en production). La connexion interne est privilégiée ici. Render définit PORT automatiquement ; l'application l'utilise et écoute sur 0.0.0.0. Aucun build command ou start command supplémentaire n'est nécessaire en mode Docker.

6. Dans Advanced, configurer Health Check Path : `/api/ping`.
7. Create Web Service / Deploy. Consulter Logs : le build Maven doit réussir, puis Tomcat doit démarrer et Hikari se connecter à PostgreSQL.
8. Copier l'URL HTTPS réelle affichée en haut du service. Exemple uniquement : `https://todo-api-demo.onrender.com` (non réservée, non déployée par l'assistant).

## 6. Tester le CRUD

Dans un terminal Bash, remplacer l'URL par celle du tableau de bord. Pour un test local utiliser `http://localhost:8080`.

```bash
BASE='https://TON-SERVICE.onrender.com'
curl -i "$BASE/api/ping"
curl -i "$BASE/api/tasks"
curl -i -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{"tache":"Tester Render","date":"2026-11-27"}'
```

POST renvoie 201 Created, un JSON sans ID et un en-tête `Location: /api/tasks/1` (1 est un exemple). Relever l'ID réel dans Location, puis :

```bash
ID=1 # remplacer par l'identifiant réel
curl -i "$BASE/api/tasks/$ID"
curl -i -X PUT "$BASE/api/tasks/$ID" -H 'Content-Type: application/json' -d '{"tache":"Tester PostgreSQL","date":"2026-11-28"}'
curl -i "$BASE/api/tasks"
curl -i -X DELETE "$BASE/api/tasks/$ID"
curl -i "$BASE/api/tasks/$ID"
```

| Méthode | Route | Résultat attendu |
|---|---|---|
| GET | /api/ping | 200 OK |
| GET | /api/tasks | 200, tableau JSON |
| GET | /api/tasks/{id} | 200 ou 404 |
| POST | /api/tasks | 201 + Location |
| PUT | /api/tasks/{id} | 200 ou 404 |
| DELETE | /api/tasks/{id} | 204 ou 404 |

POST/PUT avec tâche vide, date absente ou invalide : 400. Dans Postman, sélectionner la méthode, saisir l'URL, Body > raw > JSON pour POST/PUT. Une collection Postman est fournie ; modifier sa variable baseUrl et taskId (ou créer une tâche pour la renseigner automatiquement).

Le navigateur permet directement les GET. Pour les autres méthodes utiliser Postman ou curl. La liste ne contient volontairement pas d'identifiant : pour retrouver les IDs d'anciennes tâches, utiliser PostgreSQL (`SELECT id, tache, date FROM tasks ORDER BY id;`). Pour un vrai frontend CRUD, il serait préférable d'exposer un identifiant ou un lien par tâche.

## 7. Vérifier la persistance

Créer une tâche, redémarrer le Web Service depuis Render, puis refaire GET /api/tasks. La tâche doit toujours être présente tant que la base existe. Le stockage est PostgreSQL, pas le disque du conteneur.

## 8. Mise en veille et dépannage

Le premier appel après inactivité peut être lent. `/api/ping` ne fait pas de requête SQL ; il peut servir à un contrôle de disponibilité externe, sous réserve des conditions Render. Il ne prévient pas l'expiration de la base gratuite.

- Connection refused : vérifier le hostname, le port, la région et le statut de la base.
- Authentication failed : vérifier DB_USER et DB_PASSWORD.
- JDBC URL invalid : vérifier le préfixe jdbc:postgresql://.
- Port scan timeout : vérifier PORT et les logs de démarrage.
- Out of memory : consulter les métriques ; le Dockerfile limite le heap à 256 Mo mais la mémoire totale JVM est supérieure.
- 404 : vérifier /api/tasks et l'identifiant réel.
- 400 : vérifier JSON et format de date.

## 9. Sauvegarde et nettoyage

Avant expiration, exporter via l'URL externe et un client pg_dump compatible avec la version du serveur : `pg_dump --format=custom --file=todo.dump "$DATABASE_URL"`. Garder la sauvegarde hors du dépôt. Supprimer le Web Service et la base de test quand ils ne sont plus nécessaires. Vérifier les quotas et tarifs avant toute activation payante.

## 10. Limites de vérification

Le code et les scripts ont été préparés pour ce scénario. Le build Maven et le déploiement réel n'ont pas été exécutés dans l'environnement de génération (Maven et moteur de conteneurs indisponibles). Les URL publiques sont des exemples ; l'URL réelle sera attribuée par Render après déploiement. Les intitulés de l'interface et les offres peuvent évoluer.
