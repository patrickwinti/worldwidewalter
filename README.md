# Worldwide Walter

Worldwide Walter is a game that requires players to make witty and convincing responses to various prompts, with the
objective of fooling the other players into selecting their response as the one coming from the Sphinx. The role of the
Sphinx is assigned to one of the players and changes throughout the game.

## Development

### Links

* Latest version of the game: [Worldwide Walter](http://wwww.worldwidewalter.ch)
* Swagger: http://localhost:8080/swagger-ui.html
* All decisions made up during team meetings can be found in the [guidelines](docs/decisions/guidelines.md)
* Generic testing list: [Testing checklist](GameDevelopmentTestingChecklist.md)
* All diagrams: [UML diagrams](docs/diagrams)

The development will be done in two week iterations. Developers will need two reviewers to pass a PR, and no code can be
merged without having proper testing implemented.

### To deploy the web app

**For IntellJ**

1. Select `clean install` configuration
2. Select either `run fat jar` or `run World Wide Walter` configurations
3. Open in browser [Worldwide Walter](http://localhost:8080)

**Without IntellJ**

1. Run maven target `clean install -f pom.xml`
2. Run in your terminal in the project directiory `java -jar backend/target/backend-{VERSIOn}-SNAPSHOT.jar`

### API contract

`openapi.json` (repo root) is the source of truth for the REST API. The frontend generates
its DTOs and HTTP services from it automatically during the build (into the git-ignored
`frontend/src/app/api/`). After changing a controller or a backend `dto/` class, regenerate
and commit the spec:

```bash
cd backend && mvn -Dopenapi.generate=true test -Dtest=OpenApiSpecTest
```

`OpenApiSpecTest` fails the build if the committed `openapi.json` is out of date.

## Project Management

* JIRA: https://worldwidewalter.atlassian.net/jira/software/projects/WWW/boards/1
* Estimation guidelines for JIRA story points can be found
  here: [JIRA story points guidelines](docs/decisions/jira_estimation.md)
