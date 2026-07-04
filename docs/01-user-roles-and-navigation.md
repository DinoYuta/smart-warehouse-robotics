# User Roles and Navigation

## Demo accounts

The accounts are defined in `src/main/java/com/warehouse/config/SecurityConfig.java`.

| User | Password | Granted roles | Landing page |
| --- | --- | --- | --- |
| `Admin` | `admin` | `ADMIN`, `MANAGE`, `STAFF` | `/dashboard` |
| `Manage` | `manage` | `MANAGE`, `STAFF` | `/manager/robot-tasks` |
| `Nova001` | `nova001` | `STAFF` | `/staff/pickup-request` |

The hierarchy is implemented by granting higher-level accounts all lower-level roles: **Admin > Manage > Staff**.

The login template displays only `Nova001 / nova001` in its public demo card. Admin and Manager credentials remain available for local review but are intentionally not advertised on the page.

## Access matrix

| Route group | Staff | Manage | Admin |
| --- | :---: | :---: | :---: |
| `/settings` | Yes | Yes | Yes |
| `/staff/**` | Yes | Yes | Yes |
| `/manager/**` | No | Yes | Yes |
| `/`, `/dashboard` | No | No | Yes |
| `/rules/**` | No | No | Yes |
| `/robots/**` | No | No | Yes |
| `/simulation/**` | No | No | Yes |
| `/system-flow/**` | No | No | Yes |

Public access is limited to login, access-denied, static assets, and the favicon. Unauthorized authenticated requests use `/access-denied`.

## Navigation labels

The shared navigation is defined in `src/main/resources/templates/fragments/sidebar.html`.

| Section | Navigation label | Route |
| --- | --- | --- |
| Admin | Dashboard | `/dashboard` |
| Admin | Rule Management | `/rules` |
| Admin | Robot Management | `/robots` |
| Admin | Simulation | `/simulation` |
| Admin | System Flow | `/system-flow` |
| Manager | Robot Task Board | `/manager/robot-tasks` |
| Manager | Rule / Policy Assignment | `/manager/policy-assignment` |
| Staff | Create Pickup Request | `/staff/pickup-request` |
| Staff | My Missions | `/staff/missions` |
| Staff | Live Warehouse Map | `/staff/live-map` |

The Live Map navigation opens a new tab. `/staff/live-map/state` is an API endpoint and does not appear as a menu item.

## Security scope

This security configuration is suitable for a controlled classroom demonstration, not production:

* Users are stored in memory and reset when the application restarts.
* Passwords are encoded with BCrypt when users are created at startup.
* CSRF is disabled because the existing Thymeleaf forms do not include CSRF tokens.
* There is no registration, password recovery, external identity provider, or persistent user database.

![Staff Mobile Login](images/login-staff-mobile.png)

Add the screenshot with this exact filename under `docs/images/`.
