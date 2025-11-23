## Cinema Seat Picker

Validerer biografbesøgenes ordrer, så de ikke skaber isolerede enkelte sæder.




- Java 17+ with Spring Boot
- Maven
- Vanilla JavaScript frontend (via Spring Boot)


## Installation

```bash
git clone https://github.com/ChristofferLundKEA/cinema-seat-picker.git
cd cinema-seat-picker
mvn clean install
```

## Run

```bash
mvn spring-boot:run
```

`http://localhost:8080/html/index.html`

## Kør Tests

```bash
mvn test
```

## Kør A/B Simulation

```bash
mvn test-compile && java -cp "target/test-classes:target/classes" org.example.cinemaseatpicker.SeatPickerSimulation
```

## Evaluering og prompts ligger i /docs