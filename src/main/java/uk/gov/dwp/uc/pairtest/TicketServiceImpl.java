package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

public class TicketServiceImpl implements TicketService {
    // Immutable constants for ticket prices and maximum tickets.
    private static final int infantPrice = 0;
    private static final int childPrice  = 15;
    private static final int adultPrice  = 25;
    private static final int maxTickets  = 25; 

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    // Constructor
    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    // Methods are below.
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validatePurchase(accountId, ticketTypeRequests);

        int totalPayment = calculateTotalPayment(ticketTypeRequests);
        int totalSeats = calculateTotalSeats(ticketTypeRequests);

        ticketPaymentService.makePayment(accountId, totalPayment);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    private void validatePurchase(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        // Check if accountId is valid
        if (accountId <= 0) {
            throw new InvalidPurchaseException("Invalid account ID.");
        }

        // Check to see if there are ticket requests. Both null and empty are checked in the case that the user does not enter any tickets.
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("No tickets requested.");
        }

        int infantCount = 0;
        int childCount = 0;
        int adultCount = 0;
        int totalTickets = 0;

        // For loop to go through each ticket request and update variables.
        for (TicketTypeRequest ticketRequest : ticketTypeRequests) {

            // For each ticket checked, add to the total number of tickets.
            totalTickets += ticketRequest.getNoOfTickets();

            // Switch case to add ticket into correct variable depending on type of ticket.
            switch (ticketRequest.getTicketType()) {
                case INFANT:
                    infantCount += ticketRequest.getNoOfTickets();
                    break;
                case CHILD:
                    childCount += ticketRequest.getNoOfTickets();
                    break;
                case ADULT:
                    adultCount += ticketRequest.getNoOfTickets();
                    break;
            }
        }

        // Check to see that maximum ticket number is not exceeded.
        if (totalTickets > maxTickets) {
            throw new InvalidPurchaseException("Too many tickets. 25 is the maximum number of tickets.");
        }

        // Checks are now run to see if any child/infent tickets are present without adult tickets.
        if (adultCount == 0 && (childCount > 0 || infantCount > 0)) {
            throw new InvalidPurchaseException("Infant and child tickets cannot be purchased without an adult ticket.");
        }

        // Check to see if infant tickets do not exceed adult tickets as they sit in laps.
        if (infantCount > adultCount) {
            throw new InvalidPurchaseException("Number of infant tickets cannot exceeds number of adult tickets.");
        }
    }


    private int calculateTotalPayment(TicketTypeRequest... ticketTypeRequests) {
        int totalPayment = 0;

        // For loop to go through each ticket request.
        for(TicketTypeRequest ticketRequest : ticketTypeRequests) {
            // Another switch case to add correct price to variable depending on ticket type.
            switch (ticketRequest.getTicketType()) {
                case INFANT:
                    totalPayment += infantPrice * ticketRequest.getNoOfTickets();
                    break;
                case CHILD:
                    totalPayment += childPrice * ticketRequest.getNoOfTickets();
                    break;
                case ADULT:
                    totalPayment += adultPrice * ticketRequest.getNoOfTickets();
                    break;
            }
        }
        return totalPayment;
    }

    private int calculateTotalSeats(TicketTypeRequest... ticketTypeRequests) {
        int totalSeats = 0;

        // For loop to go through each ticket request.
        for(TicketTypeRequest ticketRequest : ticketTypeRequests) {
            // As infants will sit on laps, only adult and child tickets will be counted towards total seats.
            if(ticketRequest.getTicketType() != TicketTypeRequest.Type.INFANT) {
                totalSeats += ticketRequest.getNoOfTickets();
            }
        }
        return totalSeats;
    }
}
