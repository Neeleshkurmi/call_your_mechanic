# Call Your Mechanic

Call Your Mechanic is a project to streamline offline mechanic functions. The project is designed to assist mechanics and customers by simplifying workflows, booking, and management of mechanic services in an offline or low-connectivity environment.

## Features

- Mechanic booking and scheduling
- Customer management
- Service history tracking
- Support for low or intermittent internet environments
- Modern frontend and robust backend

## Tech Stack

This project uses a diverse stack primarily based on Java and TypeScript, with other supporting languages:

- **Java**: Backend logic and core application server
- **TypeScript** and **JavaScript**: Modern, type-safe frontend and API contracts
- **HTML/CSS**: Responsive User Interface
- **Dockerfile**: Containerized deployments

## Language Breakdown

- Java – 49.7%
- TypeScript – 35.5%
- JavaScript – 8%
- HTML – 4.6%
- CSS – 2.1%
- Dockerfile – 0.1%

## Getting Started

### Prerequisites

- Java 17+ (for backend services)
- Node.js (for frontend or TypeScript/JavaScript build steps)
- Docker (optional, for containerized deployment)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Neeleshkurmi/call_your_mechanic.git
   cd call_your_mechanic
   ```

2. **Backend Setup:**
   - Navigate to the backend directory (update this if your backend is in a subfolder).
   - Build and run with Maven or Gradle:
     ```bash
     ./mvnw spring-boot:run
     # or for Gradle
     ./gradlew bootRun
     ```

3. **Frontend Setup:**
   - Navigate to the frontend directory (update this if your frontend is in a subfolder).
   - Install dependencies and start the development server:
     ```bash
     npm install
     npm start
     ```

4. **Using Docker** (if you wish to run everything in containers):
   ```bash
   docker-compose up --build
   ```

## Usage

- Access the application via your browser at `http://localhost:3000` (or as configured).
- Log in as a customer or mechanic. (See [docs/USERS.md](docs/USERS.md) if present for default users.)
- Start booking services or managing appointments.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.
See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

For queries or support, open an issue or reach out to [Neeleshkurmi](https://github.com/Neeleshkurmi).
