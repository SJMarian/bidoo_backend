package com.example.bidoo_backend.auction.dto;

import lombok.*;

/**
 * Sent via REST and WebSocket so the frontend can sync its countdown
 * to server time, preventing client-side clock manipulation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerTimeDTO {
    /** Current server time in epoch milliseconds */
    private long serverTimeMs;

    /** ISO-8601 string of the same time, for readability */
    private String serverTimeIso;
}
