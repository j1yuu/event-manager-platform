# Event Manager platform

### Stack
- Spring Boot (Java 25)
- Hibernate, Jpa, Liquibase, Postgresql

### Pre-config
Create `.env` file. Example could be found in `.env.example`.

### Dev
You can start this application by running following commands:

```bash
make docker-up
make dev-up
```

### Docs

#### Manual
To find documentation, you can go to `/docs/openapi` directory and go through API reference defined in OpenAPI specification.

#### Web
If you want to inspect documentation in swagger-ui, run application and go to the `/swagger-ui/index.html` endpoint.