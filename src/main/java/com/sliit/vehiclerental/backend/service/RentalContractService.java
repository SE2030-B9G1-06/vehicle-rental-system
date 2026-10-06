package com.sliit.vehiclerental.backend.service;

import com.sliit.vehiclerental.backend.entity.RentalContract;
import com.sliit.vehiclerental.backend.entity.Booking;
import com.sliit.vehiclerental.backend.repository.BookingRepository;
import com.sliit.vehiclerental.backend.repository.RentalContractRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RentalContractService {

    private final RentalContractRepository contractRepository;
    private final BookingRepository bookingRepository;

    public RentalContractService(RentalContractRepository contractRepository, BookingRepository bookingRepository) {
        this.contractRepository = contractRepository;
        this.bookingRepository = bookingRepository;
    }

    // CREATE
    public RentalContract createContract(RentalContract contract) {
        if (contract.getBooking() == null || contract.getBooking().getId() == null) {
            throw new RuntimeException("A confirmed booking is required.");
        }
        Booking booking = bookingRepository.findById(contract.getBooking().getId())
                .orElseThrow(() -> new RuntimeException("Booking not found."));
        if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Only a confirmed booking can receive a rental contract.");
        }
        contract.setBooking(booking);
        return contractRepository.save(contract);
    }

    // READ ALL
    public List<RentalContract> getAllContracts() {
        return contractRepository.findAll();
    }

    // UPDATE
    public RentalContract updateContract(Long id, RentalContract updatedData) {
        RentalContract existing = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Contract not found."));

        existing.setDriverName(updatedData.getDriverName());
        existing.setLicenseNumber(updatedData.getLicenseNumber());
        existing.setIdentityDocument(updatedData.getIdentityDocument());
        existing.setContactDetails(updatedData.getContactDetails()); // Added here!
        existing.setPickupTimestamp(updatedData.getPickupTimestamp());
        existing.setReturnTimestamp(updatedData.getReturnTimestamp());
        existing.setSignature(updatedData.getSignature());
        existing.setStatus(updatedData.getStatus());

        return contractRepository.save(existing);
    }

    // DELETE
    public void deleteContract(Long id) {
        contractRepository.deleteById(id);
    }
}
