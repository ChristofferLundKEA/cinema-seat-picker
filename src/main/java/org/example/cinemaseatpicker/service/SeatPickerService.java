package org.example.cinemaseatpicker.service;

import jakarta.annotation.PostConstruct;
import org.example.cinemaseatpicker.model.Seat;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeatPickerService {


    private final Map<Integer, List<Seat>> seatMap = new HashMap<>();

    @PostConstruct
    public void initSeats() {
        int totalRows = 5;
        int seatsPerRow = 10;

        for (int row = 1; row <= totalRows; row++) {
            List<Seat> seats = new ArrayList<>();

            for (int num = 1; num <= seatsPerRow; num++) {
                seats.add(new Seat(num, row, false));
            }

            seatMap.put(row, seats);
        }
    }

    public List<Seat> getAllSeats() {
        List<Seat> allSeats = new ArrayList<>();
        for (int row = 1; row <= 5; row++) {
            allSeats.addAll(seatMap.get(row));
        }
        return allSeats;
    }

    public void setupTestScenario() {
        // Reset all seats first
        for (int row = 1; row <= 5; row++) {
            for (Seat seat : seatMap.get(row)) {
                seat.setTaken(false);
            }
        }

        // Row 1: Completely full (all taken) - baseline
        for (int i = 0; i < 10; i++) {
            seatMap.get(1).get(i).setTaken(true);
        }

        // Row 2: Only seats 5-6 available (2 seats together) - valid selection test
        for (int i = 0; i < 10; i++) {
            if (i != 4 && i != 5) { // seat 5 and 6 are at index 4 and 5
                seatMap.get(2).get(i).setTaken(true);
            }
        }

        // Row 3: Seats 2, 4, 6, 8 available (isolated singles) - test if we can select when no alternatives exist
        for (int i = 0; i < 10; i++) {
            if (i != 1 && i != 3 && i != 5 && i != 7) { // seats 2, 4, 6, 8
                seatMap.get(3).get(i).setTaken(true);
            }
        }

        // Row 4: Seats 1-4 available (4 consecutive) - test "2 left OR 2 right, not 2 middle" rule
        for (int i = 4; i < 10; i++) { // Mark seats 5-10 as taken
            seatMap.get(4).get(i).setTaken(true);
        }

        // Row 5: Seats 7-10 available (4 at edge) - another "2 left OR 2 right" test
        for (int i = 0; i < 6; i++) { // Mark seats 1-6 as taken
            seatMap.get(5).get(i).setTaken(true);
        }
    }

    public void orderSeats(List<Seat> selectedSeats) {
        for (Seat selectedSeat : selectedSeats) {
            List<Seat> rowSeats = seatMap.get(selectedSeat.getRow());
            for (Seat seat : rowSeats) {
                if (seat.getSeat() == selectedSeat.getSeat()) {
                    seat.setTaken(true);
                    break;
                }
            }
        }
    }

    public boolean checkSeats(List<Seat> selectedSeats) {
        // Validate all seats are from same row
        if (selectedSeats.isEmpty()) return false;

        int firstRow = selectedSeats.get(0).getRow();
        for (Seat seat : selectedSeats) {
            if (seat.getRow() != firstRow) {
                return false; // Reject if seats from different rows
            }
        }

        // Check if this selection creates fragmentation
        boolean createsFragmentation = doesSelectionCreateFragmentation(selectedSeats);

        if (!createsFragmentation) {
            return true; // Good selection, no fragmentation
        }

        // Selection creates fragmentation - check if better alternatives exist
        int requestedCount = selectedSeats.size();
        boolean hasAlternatives = hasValidAlternatives(requestedCount);

        if (hasAlternatives) {
            return false; // Better options exist, reject this selection
        } else {
            return true; // No better options, allow despite fragmentation (cinema is nearly full)
        }

    }

    private boolean doesSelectionCreateFragmentation(List<Seat> selectedSeats) {
        // All seats are from same row (validated in checkSeats)
        int row = selectedSeats.get(0).getRow();

        // Get seat numbers
        List<Integer> selectedSeatNumbers = new ArrayList<>();
        for (Seat seat : selectedSeats) {
            selectedSeatNumbers.add(seat.getSeat());
        }

        // Create simulation for this row
        boolean[] rowState = new boolean[10]; // false = available, true = taken
        List<Seat> rowSeats = seatMap.get(row);

        for (Seat seat : rowSeats) {
            rowState[seat.getSeat() - 1] = seat.isTaken();
        }

        // Apply the selected seats to this row
        for (int seatNum : selectedSeatNumbers) {
            rowState[seatNum - 1] = true;
        }

        // Check for NEW isolated seats created by this selection
        for (int seatNum : selectedSeatNumbers) {
            int index = seatNum - 1;

            // Check left neighbor
            if (index > 0 && !rowState[index - 1]) {
                boolean leftTaken = (index - 1 == 0) || rowState[index - 2];
                if (leftTaken) {
                    return true; // Creates isolated seat on the left
                }
            }

            // Check right neighbor
            if (index < rowState.length - 1 && !rowState[index + 1]) {
                boolean rightTaken = (index + 1 == rowState.length - 1) || rowState[index + 2];
                if (rightTaken) {
                    return true; // Creates isolated seat on the right
                }
            }
        }

        return false; // Does not create fragmentation
    }

    private boolean hasValidAlternatives(int requestedCount) {
        // Check each row for contiguous groups of available seats
        for (int row = 1; row <= 5; row++) {
            List<Integer> contiguousGroups = findContiguousGroups(row);

            for (int groupSize : contiguousGroups) {
                // Valid alternative exists if:
                // - Exactly the requested count (can take all, no fragmentation)
                // - Or 2+ more than requested (can take N, leave 2+, no fragmentation)
                if (groupSize == requestedCount || groupSize >= requestedCount + 2) {
                    return true;
                }
                // If groupSize == requestedCount + 1, taking requestedCount leaves 1 isolated
                // This creates fragmentation, so it's NOT a valid alternative
            }
        }

        return false; // Only found groups of requestedCount+1 or smaller - no better alternatives
    }

    private List<Integer> findContiguousGroups(int row) {
        List<Integer> groups = new ArrayList<>();
        boolean[] rowState = new boolean[10];

        // Get current row state
        for (Seat seat : seatMap.get(row)) {
            rowState[seat.getSeat() - 1] = seat.isTaken();
        }

        // Find all contiguous groups of available seats
        int consecutiveCount = 0;
        for (boolean taken : rowState) {
            if (!taken) {
                consecutiveCount++;
            } else {
                if (consecutiveCount > 0) {
                    groups.add(consecutiveCount);
                    consecutiveCount = 0;
                }
            }
        }

        // Don't forget the last group if row ends with available seats
        if (consecutiveCount > 0) {
            groups.add(consecutiveCount);
        }

        return groups;
    }


}
