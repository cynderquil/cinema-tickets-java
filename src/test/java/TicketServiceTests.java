import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

// JUnit and Mockito used for test cases and mocking.
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TicketServiceTests {

    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;
    private TicketServiceImpl ticketService;

    // This method is run prior to each test case. Mock objects are created for the ticket payment service and seat reservation service.
    // The ticket service is then created using these mock objects.
    @BeforeEach
    void setUp() {
        ticketPaymentService = mock(TicketPaymentService.class);
        seatReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    // Test Case 1 - This test calls the service with an invalid account ID, as it is negative. This test should pass if the code throws the exception.
    @Test
    void shouldThrowExceptionForInvalidAccountId() {
        // An error is expected to be thrown when the account ID is invalid (negative).
        assertThrows(InvalidPurchaseException.class, () -> 
            ticketService.purchaseTickets(-1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1))
        );
    }

    // Test Case 2 - This test will check if an exception is thrown when the requested tickets are either null or empty. This test should pass if the code throws the exception.
    @Test
    void shouldThrowExceptionForNoTicketsRequested() {
        assertThrows(InvalidPurchaseException.class, () -> 
            ticketService.purchaseTickets(1L)
        );
    }

    // Test Case 3 - This test will give too many tickets into the request. As the maximum number of tickets is 25, an exception is expected to be thrown with 26 tickets.
    @Test
    void shouldThrowExceptionForTooManyTickets() {
        TicketTypeRequest ticketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);
        assertThrows(InvalidPurchaseException.class, () -> 
            ticketService.purchaseTickets(1L, ticketRequest)
        );
    }

    // Test Case 4 - Checks if an exception will be thrown when infant tickets are requested without adult tickets.
    @Test
    void shouldThrowExceptionForInfantTicketsWithoutAdult() {
        TicketTypeRequest ticketRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
    
        assertThrows(InvalidPurchaseException.class, () -> 
            ticketService.purchaseTickets(1L, ticketRequest)
        );
    }

    // Test Case 5 - Checks if the total payment is calculated correctly when a child ticket, an infant ticket, and an adult ticket are purchased.
    @Test
    void shouldCalculateTotalPaymentCorrectly() {
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest infantTicket = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(1L, adultTicket, childTicket, infantTicket);

        // Verify that the payment service is called with the correct total amount (20 for adult + 10 for child + 0 for infant = 40).
        verify(ticketPaymentService).makePayment(1L, 40);
    }

    // Test Case 6 - Checks if the correct number of seats are reserved when one adult and one child ticket are purchased.
    @Test
    void shouldReserveCorrectNumberOfSeatsForAdultAndChild() {
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
    
        ticketService.purchaseTickets(1L, adultTicket, childTicket);
    
        // Verify that the seat reservation service is called with the correct number of seats (1 for adult + 1 for child = 2).
        verify(seatReservationService).reserveSeat(1L, 2);
    }
}

