-- Insert test auctions with upcoming and active states
INSERT INTO auctions (title, description, seller_id, starting_price, current_highest_bid, highest_bidder_id, start_time, end_time, state, created_at) VALUES
('PlayStation 5 Console', 'Brand new PS5 with all accessories, sealed box', 1, 350.00, 0.00, null, CURRENT_TIMESTAMP + INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '1 day', 'UPCOMING', CURRENT_TIMESTAMP),
('iPhone 15 Pro Max', 'Latest model iPhone 15 Pro Max 1TB Space Black', 2, 1200.00, 0.00, null, CURRENT_TIMESTAMP + INTERVAL '30 minutes', CURRENT_TIMESTAMP + INTERVAL '6 hours', 'UPCOMING', CURRENT_TIMESTAMP),
('MacBook Pro 16"', 'M3 Max 16-inch MacBook Pro 48GB RAM', 1, 2500.00, 2400.00, 3, CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP + INTERVAL '4 hours', 'ACTIVE', CURRENT_TIMESTAMP),
('Gaming PC Setup', 'High-end gaming PC with RTX 4080, i9-13900K', 2, 3000.00, 2950.00, 5, CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '2 hours', 'ACTIVE', CURRENT_TIMESTAMP),
('DJI Air 3S Drone', 'DJI Air 3S with carrying case and accessories', 1, 900.00, 850.00, 2, CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '3 hours', 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;
