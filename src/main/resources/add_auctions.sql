-- Add upcoming and active auctions
INSERT INTO auctions (title, description, seller_id, starting_price, current_highest_bid, highest_bidder_id, start_time, end_time, state, created_at) VALUES
('PlayStation 5 Console', 'Brand new PS5 with all accessories', 1, 350.00, 0.00, null, CURRENT_TIMESTAMP + INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '1 day', 'UPCOMING', CURRENT_TIMESTAMP),
('iPhone 15 Pro', 'Latest model iPhone 15 Pro Max', 2, 800.00, 0.00, null, CURRENT_TIMESTAMP + INTERVAL '30 minutes', CURRENT_TIMESTAMP + INTERVAL '6 hours', 'UPCOMING', CURRENT_TIMESTAMP),
('MacBook Pro 16', 'M3 Max 16-inch MacBook Pro', 1, 1500.00, 1450.00, 3, CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP + INTERVAL '1 minutes', 'ACTIVE', CURRENT_TIMESTAMP),
('Gaming PC Setup', 'High-end gaming PC with RTX 4080', 2, 2000.00, 1950.00, 5, CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '2 hours', 'ACTIVE', CURRENT_TIMESTAMP),
('DJI Drone Pro', 'DJI Air 3S with carrying case', 1, 600.00, 580.00, 2, CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '3 hours', 'ACTIVE', CURRENT_TIMESTAMP);
