package org.example.cinemaseatpicker.controller;

import org.example.cinemaseatpicker.model.Seat;
import org.example.cinemaseatpicker.service.SeatPickerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class SeatPickerController {

    SeatPickerService seatPickerService;

    SeatPickerController(SeatPickerService seatPickerService){
        this.seatPickerService = seatPickerService;
    }

    @GetMapping("/seats")
    public List<Seat> getAllSeats() {
        return seatPickerService.getAllSeats();
    }

    @PostMapping("/order")
    public boolean orderSeats(@RequestBody List<Seat> seats) {
        boolean isValid = seatPickerService.checkSeats(seats);

        if (isValid) {
            seatPickerService.orderSeats(seats);
        }

        return isValid;
    }

    @PostMapping("/setup-test")
    public void setupTestScenario() {
        seatPickerService.setupTestScenario();
    }

}
