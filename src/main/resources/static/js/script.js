async function loadSeats() {
    const response = await fetch("http://localhost:8080/seats");
    const seats = await response.json();

    const theater = document.getElementById("theater");
    theater.innerHTML = "";

    // Group seats by row
    const seatsByRow = {};
    seats.forEach(seat => {
        if (!seatsByRow[seat.row]) {
            seatsByRow[seat.row] = [];
        }
        seatsByRow[seat.row].push(seat);
    });

    // Create rows
    for (let rowNum = 1; rowNum <= 5; rowNum++) {
        const rowDiv = document.createElement("div");
        rowDiv.className = "row";

        const label = document.createElement("div");
        label.className = "row__label";
        label.textContent = rowNum;
        rowDiv.appendChild(label);

        const seatsDiv = document.createElement("div");
        seatsDiv.className = "seats";

        const rowSeats = seatsByRow[rowNum] || [];
        rowSeats.sort((a, b) => a.seat - b.seat);

        rowSeats.forEach(seat => {
            const seatDiv = document.createElement("div");
            seatDiv.className = "seat";
            seatDiv.dataset.id = `${seat.row}-${seat.seat}`;

            if (seat.taken) {
                seatDiv.classList.add("seat--taken");
            }

            seatDiv.addEventListener("click", () => {
                if (seatDiv.classList.contains("seat--taken")) return;
                seatDiv.classList.toggle("seat--selected");
                console.log("Valgt sæde:", seatDiv.dataset.id);
            });

            seatsDiv.appendChild(seatDiv);
        });

        rowDiv.appendChild(seatsDiv);
        theater.appendChild(rowDiv);
    }
}

document.addEventListener("DOMContentLoaded", loadSeats);

document.getElementById("setupTestButton").addEventListener("click", async () => {
    const confirmed = confirm("Dette vil nulstille alle sæder og opsætte test data. Fortsæt?");

    if (!confirmed) return;

    await fetch("http://localhost:8080/setup-test", {
        method: "POST"
    });

    // Reload seats to show new state
    await loadSeats();
});

document.getElementById("orderButton").addEventListener("click", async () => {
    const selected = [...document.querySelectorAll(".seat--selected")]
        .map(s => ({
            row: parseInt(s.dataset.id.split("-")[0]),
            seat: parseInt(s.dataset.id.split("-")[1]),
        }));

    if (selected.length === 0) {
        alert("Vælg venligst mindst ét sæde");
        return;
    }

    console.log("Valgte sæder:", selected);

    const response = await fetch("http://localhost:8080/order", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(selected)
    });

    const isValid = await response.json();
    console.log("Bestilling resultat:", isValid);

    if (isValid) {
        alert("Sæder bestilt!");
        // Reload seats to show the newly taken seats
        await loadSeats();
    } else {
        alert("Dit valg efterlader enkelte pladser. Vælg venligst andre sæder.");
    }
});