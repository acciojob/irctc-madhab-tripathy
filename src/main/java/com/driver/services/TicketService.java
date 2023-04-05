package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity

        // Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        int numberOfSeatsWantToBook = bookTicketEntryDto.getNoOfSeats();
        // all booked tickets from train
        List<Ticket> ticketList = train.getBookedTickets();
        // all passengers booked the tickets for that train
        int bookedSeats = 0;
        List<Integer> passengers = bookTicketEntryDto.getPassengerIds();
        for(Ticket t1 : ticketList) {
            List<Passenger> passengerList = t1.getPassengersList();
            for (Integer i : passengers) {
                Passenger currentPassenger = passengerRepository.findById(i).get();
                if (passengerList.contains(currentPassenger)) {
                    bookedSeats += currentPassenger.getBookedTickets().size();
                    break;
                }
            }
        }
        train.setNoOfSeats(train.getNoOfSeats() - bookedSeats);

        if(train.getNoOfSeats() - numberOfSeatsWantToBook < 0){
            throw new Exception("Less tickets are available");
        }
        String[] routes = train.getRoute().split(",");
        int startStation = 0;
        int endStation = 0;
        boolean fromStationAvailable = false;
        boolean toStationAvailable = false;
        for(int i = 0; i < routes.length; i++){
            if(routes[i].equals(String.valueOf(bookTicketEntryDto.getFromStation()))){
                startStation = i;
                fromStationAvailable = true;
            }
            if(routes[i].equals(String.valueOf(bookTicketEntryDto.getToStation()))){
                endStation = i;
                toStationAvailable = true;
            }
        }
        if(!fromStationAvailable || !toStationAvailable){
            throw new Exception("Invalid stations");
        }
        // now seats are available we can book a tickets
        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();

        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(endStation-startStation * 300);
        ticket.setTrain(train);
        train.getBookedTickets().add(ticket);
        ticket.getPassengersList().add(passenger);
        passenger.getBookedTickets().add(ticket);
        trainRepository.save(train);
        Ticket updateTicket = ticketRepository.save(ticket);
       return updateTicket.getTicketId();
    }
}
