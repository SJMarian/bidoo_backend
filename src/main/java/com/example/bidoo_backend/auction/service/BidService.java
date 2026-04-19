package com.example.bidoo_backend.auction.service;

import com.example.bidoo_backend.auction.dto.BidRequest;
import com.example.bidoo_backend.auction.dto.BidResponse;
import com.example.bidoo_backend.auction.entity.Auction;
import com.example.bidoo_backend.auction.entity.Bid;
import com.example.bidoo_backend.auction.repository.AuctionRepository;
import com.example.bidoo_backend.auction.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public BidResponse placeBid(BidRequest request, Principal principal) {

        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (request.getAmount() <= auction.getCurrentHighestBid()) {
            throw new RuntimeException("Bid must be higher than current highest bid");
        }

        String bidder = principal != null ? principal.getName() : "Guest";

        Bid bid = Bid.builder()
                .amount(request.getAmount())
                .bidTime(LocalDateTime.now())
                .bidderName(bidder)
                .auction(auction)
                .build();

        Bid savedBid = bidRepository.save(bid);

        auction.setCurrentHighestBid(request.getAmount());
        auctionRepository.save(auction);

        messagingTemplate.convertAndSend(
                "/topic/auction/" + auction.getId(),
                savedBid
        );

        return BidResponse.builder()
                .bidId(savedBid.getId())
                .amount(savedBid.getAmount())
                .message("Bid placed successfully")
                .build();
    }
}