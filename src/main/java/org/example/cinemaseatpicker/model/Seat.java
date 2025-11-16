package org.example.cinemaseatpicker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Seat {
    private int seat;
    private int row;
    private boolean isTaken;
}
