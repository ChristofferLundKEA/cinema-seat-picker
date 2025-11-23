# Cinema Seat Picker - Development Prompts
Claude code

---

### Dynamic Seat Loading
> first i want it so that the frontend is updated "on load" with the 10x10 grid from backend, so that if a seat is taken/already ordered, it shows in frontend.

---

### Alternative UX Approach
> would the process of validating be better if the user chooses the amount of desired seats in a dropdown before hand and then only get shown allowed seats that dont create fragmentation?

---

### Final Implementation Decision
> real time sounds inecessary complicated? im satisfied with the "check" button actually
>
> otherwise the approach sounds good

---

### Test Data Setup Button
> for easy manual testing, create a button on frondend that initializes a nearly full cinema that facilitates most and best edge case testing posibilities for checkSeats()

---

### Fragmentation Logic Issue
> this seem wierd. its supposed to only check for isolated seats around the selected seats, no?
>
> right now it checks all the rows, and since there already is single seats on row 3, is will always send false flag? even though i picked seats on back row with all seats avalable

---

### Adjacent Seats Only Check
> this code needs to check instead of the entire row, only until next seat that is taken. as it is now it checks the whole row, and if a single seat is already further down the row, it false flags?
> correct?

---

### Allow Fragmentation When No Alternatives
> in this block, and the one with if (rightTaken) should first check all rows and see if there is a combination of seats elsewhere that does not create fragmentation before returning false. if there is no other valid seats in the cinama it shoild allow fragmentation and allow the booking
>
> thoughts?

---

### Contiguous Seats Check
> in findvalidcombinations why is it checking for fragmentation. it should be enough to just check for # of selected seats somewhere in the cinema that is connected? thoughts? yes or no?

> yes but this still allow for false flags. if two groups of 4 seats are avalable and the user picks 3 seats. then there will be two places where they can sit, but both creates fragmentation.
> will this solution that i present still work in that case?

---

### Refined Validation Logic
> im not sure i understand. as i understand it findvalidcombination should only run if the selected seats creates fragmentation.
>
> it should then loop through all rows and see if other rows has exactly #ofselectedseats or #selectedseats +1 (like the one the user is already trying to pick)
>
> if it only finds continously seats >= #ofselectedseats +1 then allow for the order anyway, even though it creates fragmentation. that way ive checked that all the rest of the posibilities also creates fragmentation?
>
> correct? thoughts?

---

### A/B Test Simulation Design
> i need to create an A/B test/simulation.
> one that is naive and without rules plug people into seats until there is no more space in the theater.
> My idea is to create functionality that finds all the blocks of contigiuos seats in the cinema that is the same size as the requested group size or larger and then pick a random one from this list of blocks. This is to not just loop over all the rows until i find a suitable spot - that will not simulate human behaviour really well and thus not create fragmentation.
>
> and another simulation that follows my algoritm lowering singe fragmentation.
>
> the goal is afterwards to collect data (how many single seats) and see how much my algorithm has improved the problem
>
> my idea is that this is supposed to happen in the SeatPickerSimulation.java in the test folder
>
> thoughts and ideas?

---

### Unit Tests for SeatPickerService
> I am going to create Unit tests for SeatPickerService in SeatPickerServiceTest.
>
> I think these cases are good to test: (let me know if im missing something)
> - No fragmentation (normal scenario, everything should be good), should return true
> - Users selection creates fragmentation and there is better alternatives, should return false
> - Users selection creates fragmentation but there is no better alternatives, should return true
> - Seats are already occupied, should return false
>
> For these tests i imagine some helper functions would ease the writing and reading of the tests.
> I'm thinking functions like:
> - initCinema()
> - select(row, seatsNumbers)
> - markSeatsAsTaken(row, seatNumbers)
> - fillRowExcept(row, seatsThatShouldNotBeOccupied)
>
> again - let me know if im missing either an important test or a helperfunction that could decrease clutter in testcode

---

### Test Case Review - No Alternatives Issue
> in testAllowWhenNoAlternatives ln 203, you are saying there is no better alternatives, but there are. row 2 with only one seat, creates no fragmentation?

---

### Test Case Review - Larger Alternative Issue
> in testRejectWhenLargerAlternativeExists, ln 173 you are arranging two scenarios that both should pass checseats, both row 1 and 2.
> I've made an edit, see if you agree with my change

---