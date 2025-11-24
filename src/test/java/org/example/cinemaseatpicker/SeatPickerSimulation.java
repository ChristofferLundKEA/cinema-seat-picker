package org.example.cinemaseatpicker;

import org.example.cinemaseatpicker.model.Seat;

import java.util.*;

public class SeatPickerSimulation {

    private static final int TOTAL_ROWS = 10;
    private static final int SEATS_PER_ROW = 10;

    // Simulation result class to track metrics
    public static class SimulationResult {
        private final String simulationType;
        private int singleIsolatedSeats;
        private int groupsPlaced;
        private int groupsRejected;
        private int totalSeatsOccupied;
        private double utilizationPercentage;
        private double fragmentationRate;

        public SimulationResult(String simulationType) {
            this.simulationType = simulationType;
        }

        public void calculateMetrics() {
            int totalSeats = TOTAL_ROWS * SEATS_PER_ROW;
            this.utilizationPercentage = (totalSeatsOccupied * 100.0) / totalSeats;
            this.fragmentationRate = totalSeatsOccupied > 0
                ? (singleIsolatedSeats * 100.0) / totalSeatsOccupied
                : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                """
                === %s ===
                Groups placed: %d
                Groups rejected: %d
                Total seats occupied: %d
                Cinema utilization: %.2f%%
                Single isolated seats: %d
                Fragmentation rate: %.2f%% (isolated/occupied)
                """,
                simulationType, groupsPlaced, groupsRejected,
                totalSeatsOccupied, utilizationPercentage, singleIsolatedSeats,
                fragmentationRate
            );
        }

        // Getters
        public int getSingleIsolatedSeats() { return singleIsolatedSeats; }
        public double getFragmentationRate() { return fragmentationRate; }
    }

    // Helper class to represent a contiguous block of seats
    private static class SeatBlock {
        int row;
        int startSeat;
        int size;

        SeatBlock(int row, int startSeat, int size) {
            this.row = row;
            this.startSeat = startSeat;
            this.size = size;
        }
    }

    /**
     * NAIVE SIMULATION: Randomly selects from available contiguous blocks
     */
    public SimulationResult runNaiveSimulation(List<Integer> groupSequence) {
        Map<Integer, List<Seat>> seatMap = initializeCinema();
        SimulationResult result = new SimulationResult("Naive (Random Block Selection)");

        for (int groupSize : groupSequence) {
            // Find all suitable blocks
            List<SeatBlock> suitableBlocks = findAllSuitableBlocks(seatMap, groupSize);

            if (suitableBlocks.isEmpty()) {
                result.groupsRejected++;
                continue;
            }

            // Randomly pick a block
            SeatBlock chosenBlock = suitableBlocks.get((int) (Math.random() * suitableBlocks.size()));

            // Randomly place group within the block
            int maxStartPosition = chosenBlock.startSeat + (chosenBlock.size - groupSize);
            int startPosition = chosenBlock.startSeat + (int) (Math.random() * (chosenBlock.size - groupSize + 1));

            // Mark seats as taken
            for (int i = 0; i < groupSize; i++) {
                int seatNumber = startPosition + i;
                seatMap.get(chosenBlock.row).get(seatNumber - 1).setTaken(true);
            }

            result.groupsPlaced++;
            result.totalSeatsOccupied += groupSize;
        }

        // Calculate metrics
        result.singleIsolatedSeats = countSingleIsolatedSeats(seatMap);
        result.calculateMetrics();

        return result;
    }

    /**
     * ALGORITHM SIMULATION: Uses anti-fragmentation logic
     */
    public SimulationResult runAlgorithmSimulation(List<Integer> groupSequence) {
        Map<Integer, List<Seat>> seatMap = initializeCinema();
        SimulationResult result = new SimulationResult("Algorithm (Anti-Fragmentation)");

        for (int groupSize : groupSequence) {
            List<Seat> selectedSeats = findBestSeatsWithAlgorithm(seatMap, groupSize);

            if (selectedSeats == null) {
                result.groupsRejected++;
                continue;
            }

            // Mark seats as taken
            for (Seat seat : selectedSeats) {
                seatMap.get(seat.getRow()).get(seat.getSeat() - 1).setTaken(true);
            }

            result.groupsPlaced++;
            result.totalSeatsOccupied += groupSize;
        }

        // Calculate metrics
        result.singleIsolatedSeats = countSingleIsolatedSeats(seatMap);
        result.calculateMetrics();

        return result;
    }

    /**
     * Finds the best seats using anti-fragmentation algorithm
     */
    private List<Seat> findBestSeatsWithAlgorithm(Map<Integer, List<Seat>> seatMap, int groupSize) {
        // Try to find a valid placement that doesn't create fragmentation
        for (int row = 1; row <= TOTAL_ROWS; row++) {
            List<Integer> contiguousGroups = findContiguousGroupsInRow(seatMap, row);

            for (int groupIdx = 0; groupIdx < contiguousGroups.size(); groupIdx++) {
                int availableSize = contiguousGroups.get(groupIdx);

                // Check if this block is suitable according to algorithm rules:
                // 1. Exactly fits the group (no fragmentation)
                // 2. Leaves 2 or more seats (no single seat created)
                if (availableSize == groupSize || availableSize >= groupSize + 2) {
                    // Find the actual starting position of this block
                    int startSeat = findBlockStartPosition(seatMap, row, groupIdx);

                    // Create seat selection
                    List<Seat> selectedSeats = new ArrayList<>();
                    for (int i = 0; i < groupSize; i++) {
                        selectedSeats.add(new Seat(startSeat + i, row, false));
                    }

                    // Validate with fragmentation check
                    if (!doesSelectionCreateFragmentation(seatMap, selectedSeats)) {
                        return selectedSeats;
                    }
                }
            }
        }

        // If no ideal placement found, check if we should allow fragmentation
        // (cinema is nearly full, no better alternatives)
        for (int row = 1; row <= TOTAL_ROWS; row++) {
            List<SeatBlock> blocks = findContiguousBlocksInRow(seatMap, row);
            for (SeatBlock block : blocks) {
                if (block.size >= groupSize) {
                    List<Seat> selectedSeats = new ArrayList<>();
                    for (int i = 0; i < groupSize; i++) {
                        selectedSeats.add(new Seat(block.startSeat + i, row, false));
                    }
                    return selectedSeats;
                }
            }
        }

        return null; // No placement found
    }

    /**
     * Finds all contiguous blocks that can accommodate the group size
     */
    private List<SeatBlock> findAllSuitableBlocks(Map<Integer, List<Seat>> seatMap, int groupSize) {
        List<SeatBlock> suitableBlocks = new ArrayList<>();

        for (int row = 1; row <= TOTAL_ROWS; row++) {
            List<SeatBlock> rowBlocks = findContiguousBlocksInRow(seatMap, row);
            for (SeatBlock block : rowBlocks) {
                if (block.size >= groupSize) {
                    suitableBlocks.add(block);
                }
            }
        }

        return suitableBlocks;
    }

    /**
     * Finds contiguous blocks in a specific row (returns SeatBlock objects with positions)
     */
    private List<SeatBlock> findContiguousBlocksInRow(Map<Integer, List<Seat>> seatMap, int row) {
        List<SeatBlock> blocks = new ArrayList<>();
        List<Seat> rowSeats = seatMap.get(row);

        int consecutiveCount = 0;
        int startSeat = -1;

        for (int i = 0; i < rowSeats.size(); i++) {
            if (!rowSeats.get(i).isTaken()) {
                if (consecutiveCount == 0) {
                    startSeat = i + 1; // Seat numbers are 1-indexed
                }
                consecutiveCount++;
            } else {
                if (consecutiveCount > 0) {
                    blocks.add(new SeatBlock(row, startSeat, consecutiveCount));
                    consecutiveCount = 0;
                }
            }
        }

        // Don't forget the last block
        if (consecutiveCount > 0) {
            blocks.add(new SeatBlock(row, startSeat, consecutiveCount));
        }

        return blocks;
    }

    /**
     * Finds contiguous group sizes in a row (just sizes, not positions)
     */
    private List<Integer> findContiguousGroupsInRow(Map<Integer, List<Seat>> seatMap, int row) {
        List<Integer> groups = new ArrayList<>();
        List<Seat> rowSeats = seatMap.get(row);

        int consecutiveCount = 0;
        for (Seat seat : rowSeats) {
            if (!seat.isTaken()) {
                consecutiveCount++;
            } else {
                if (consecutiveCount > 0) {
                    groups.add(consecutiveCount);
                    consecutiveCount = 0;
                }
            }
        }

        if (consecutiveCount > 0) {
            groups.add(consecutiveCount);
        }

        return groups;
    }

    /**
     * Finds the starting position of the Nth contiguous block in a row
     */
    private int findBlockStartPosition(Map<Integer, List<Seat>> seatMap, int row, int blockIndex) {
        List<Seat> rowSeats = seatMap.get(row);
        int currentBlockIdx = 0;
        int consecutiveCount = 0;
        int startSeat = -1;

        for (int i = 0; i < rowSeats.size(); i++) {
            if (!rowSeats.get(i).isTaken()) {
                if (consecutiveCount == 0) {
                    startSeat = i + 1;
                }
                consecutiveCount++;
            } else {
                if (consecutiveCount > 0) {
                    if (currentBlockIdx == blockIndex) {
                        return startSeat;
                    }
                    currentBlockIdx++;
                    consecutiveCount = 0;
                }
            }
        }

        // Last block
        if (consecutiveCount > 0 && currentBlockIdx == blockIndex) {
            return startSeat;
        }

        return -1;
    }

    /**
     * Checks if a selection creates fragmentation (adapted from SeatPickerService)
     */
    private boolean doesSelectionCreateFragmentation(Map<Integer, List<Seat>> seatMap, List<Seat> selectedSeats) {
        int row = selectedSeats.get(0).getRow();

        // Get seat numbers
        List<Integer> selectedSeatNumbers = new ArrayList<>();
        for (Seat seat : selectedSeats) {
            selectedSeatNumbers.add(seat.getSeat());
        }

        // Create simulation for this row
        boolean[] rowState = new boolean[SEATS_PER_ROW];
        List<Seat> rowSeats = seatMap.get(row);

        for (Seat seat : rowSeats) {
            rowState[seat.getSeat() - 1] = seat.isTaken();
        }

        // Apply the selected seats
        for (int seatNum : selectedSeatNumbers) {
            rowState[seatNum - 1] = true;
        }

        // Check for isolated seats created by this selection
        for (int seatNum : selectedSeatNumbers) {
            int index = seatNum - 1;

            // Check left neighbor
            if (index > 0 && !rowState[index - 1]) {
                boolean leftTaken = (index - 1 == 0) || rowState[index - 2];
                if (leftTaken) {
                    return true;
                }
            }

            // Check right neighbor
            if (index < rowState.length - 1 && !rowState[index + 1]) {
                boolean rightTaken = (index + 1 == rowState.length - 1) || rowState[index + 2];
                if (rightTaken) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Counts the number of single isolated seats in the cinema
     */
    private int countSingleIsolatedSeats(Map<Integer, List<Seat>> seatMap) {
        int count = 0;

        for (int row = 1; row <= TOTAL_ROWS; row++) {
            List<Seat> rowSeats = seatMap.get(row);

            for (int i = 0; i < rowSeats.size(); i++) {
                if (!rowSeats.get(i).isTaken()) {
                    // Check if this empty seat is isolated
                    boolean leftTaken = (i == 0) || rowSeats.get(i - 1).isTaken();
                    boolean rightTaken = (i == rowSeats.size() - 1) || rowSeats.get(i + 1).isTaken();

                    if (leftTaken && rightTaken) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    /**
     * Initializes an empty cinema
     */
    private Map<Integer, List<Seat>> initializeCinema() {
        Map<Integer, List<Seat>> seatMap = new HashMap<>();

        for (int row = 1; row <= TOTAL_ROWS; row++) {
            List<Seat> seats = new ArrayList<>();
            for (int num = 1; num <= SEATS_PER_ROW; num++) {
                seats.add(new Seat(num, row, false));
            }
            seatMap.put(row, seats);
        }

        return seatMap;
    }

    /**
     * Runs both simulations and compares results
     */
    public void runComparison(List<Integer> groupSequence) {
        System.out.println("=".repeat(60));
        System.out.println("CINEMA SEAT PICKER A/B TEST SIMULATION");
        System.out.println("=".repeat(60));
        System.out.println("Group sequence: " + groupSequence);
        System.out.println("Total groups: " + groupSequence.size());
        System.out.println();

        SimulationResult naiveResult = runNaiveSimulation(groupSequence);
        SimulationResult algorithmResult = runAlgorithmSimulation(groupSequence);

        System.out.println(naiveResult);
        System.out.println(algorithmResult);

        System.out.println("=".repeat(60));
        System.out.println("COMPARISON");
        System.out.println("=".repeat(60));
        System.out.println(String.format("Fragmentation rate - Naive: %.2f%%, Algorithm: %.2f%%",
            naiveResult.getFragmentationRate(), algorithmResult.getFragmentationRate()));
        System.out.println(String.format("Isolated seats reduced: %d → %d (-%d seats)",
            naiveResult.getSingleIsolatedSeats(), algorithmResult.getSingleIsolatedSeats(),
            naiveResult.getSingleIsolatedSeats() - algorithmResult.getSingleIsolatedSeats()));
        System.out.println();
    }

    // Example usage
    public static void main(String[] args) {
        SeatPickerSimulation simulation = new SeatPickerSimulation();

        // Single comparison with a specific sequence
        List<Integer> testSequence = Arrays.asList(3, 2, 4, 2, 5, 3, 2, 4, 3, 2, 4, 2, 3, 5, 2, 2, 3, 4, 2, 3,
                                                     4, 3, 2, 5, 3, 2, 4, 2, 3, 2, 3, 2, 4, 2, 3, 2);
        simulation.runComparison(testSequence);
    }
}
