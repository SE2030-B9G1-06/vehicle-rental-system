# Nima Vehicle Rental - Setup

## Requirements

- Java 17
- MySQL 8+

## First run

1. Open MySQL Workbench and run `database_setup.sql`.
2. Set your MySQL password in `src/main/resources/application.properties`, or set the `DB_PASSWORD` environment variable.
3. Open a terminal in the project root.
4. On Windows, run:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

5. Open <http://localhost:8080>.

Do not open `frontend/index.html` by double-clicking it. Spring Boot now serves the frontend and backend together, which keeps login sessions and API connections reliable.

## Staff accounts

| Staff member | Role | Email | Password | Dashboard |
| --- | --- | --- | --- | --- |
| Harshani (Nimashi) | Booking Manager | `Nimashi@rental.com` | `Nimashi123` | Booking management |
| Sanjula | Rental Officer | `Sanjula@rental.com` | `Sanjula123` | Fleet management |
| Sumanawansha | System Admin | `Sumanawansha@rental.com` | `Sumana123` | All management modules |
| Kasthurisingha | Maintenance Supervisor | `Kasthuri@rental.com` | `Kasthuri123` | Maintenance |
| Victor | Finance Officer | `Victor@rental.com` | `Victor123` | Promotions and contracts |
| Kumara | Branch Manager | `Kumara@rental.com` | `Kumara123` | Branch and booking oversight |

The accounts are inserted only when their email does not already exist. Passwords are stored as BCrypt hashes.

## Customer flow

1. Register from the home page.
2. Sign in with the registered email and password.
3. Search or browse available vehicles.
4. Select **View & book**, enter dates and branches, and confirm.
5. Open **My bookings** to review or cancel the reservation.
