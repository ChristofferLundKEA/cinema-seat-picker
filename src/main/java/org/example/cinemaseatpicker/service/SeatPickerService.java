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
        int totalRows = 10;
        int seatsPerRow = 10;

        for (int row = 1; row <= totalRows; row++) {
            List<Seat> seats = new ArrayList<>();

            for (int num = 1; num <= seatsPerRow; num++) {
                seats.add(new Seat(num, row, false));
            }

            seatMap.put(row, seats);
        }
        seatMap.get(4).get(4).setTaken(true);
    }

    public List<Seat> getAllSeats() {
        List<Seat> allSeats = new ArrayList<>();
        for (int row = 1; row <= 10; row++) {
            allSeats.addAll(seatMap.get(row));
        }
        return allSeats;
    }

    public boolean checkSeats(List<Seat> seats) {
        return false;
    }

    //TODO: tage en liste af alle sæder og sæder som er bestilt
    // input: hvor mange sæder skal den finde
    // output: liste af valgmuligheder
}
