# OpsConsole

Enterprise DevOps operations dashboard built with Spring Boot 3, Thymeleaf, and H2.

## Features

- Dashboard with system health monitoring and uptime chart
- System Health — actuator probes, register monitors, grid/list views
- System Admin — SSH service control (start/stop/restart) and properties editing (dev mode simulates SSH)
- User Admin — roles, tab access, user CRUD
- Activity feed — logins, health changes, admin actions
- Dev login or Azure AD (OAuth2)

## Requirements

- Java 21
- Maven 3.9+

## Run locally

```bash
mvn spring-boot:run
```

Open http://localhost:8080

**Dev login** (`opsconsole.auth.mode=dev`):

| Email | Password | Role |
|-------|----------|------|
| admin@opsconsole.local | Admin@123 | Administrator |
| tester@opsconsole.local | Tester@123 | Tester |
| monitoring@opsconsole.local | Monitoring@123 | Monitoring |

Run only **one** app instance — the H2 file database (`data/opsconsole.mv.db`) cannot be opened by multiple processes.

## Configuration

See `src/main/resources/application.yml` for health monitors, admin servers/services (YAML seed), and Azure AD profile.

Live SSH: set `opsconsole.admin.mode=live` and `OPS_SSH_KEY_PATH` to your private key path.

## Tests

```bash
mvn test
```

Tests use an in-memory H2 database and do not require the app to be stopped.

## License

Internal use.
