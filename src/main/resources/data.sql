-- Insert test users first
INSERT INTO users (id, name, email, phone, password, role) VALUES
(1, 'John Doe', 'john@example.com', '01700000001', '$2a$10$slYQmyNdGzin7olVN3p5aOnyIjz7WLlHEYBY.QZabfSxC.K0aYQfC', 'USER'),
(2, 'Jane Smith', 'jane@example.com', '01700000002', '$2a$10$slYQmyNdGzin7olVN3p5aOnyIjz7WLlHEYBY.QZabfSxC.K0aYQfC', 'USER'),
(3, 'Mike Artist', 'mike@example.com', '01700000003', '$2a$10$slYQmyNdGzin7olVN3p5aOnyIjz7WLlHEYBY.QZabfSxC.K0aYQfC', 'USER'),
(4, 'Tech Guru', 'tech@example.com', '01700000004', '$2a$10$slYQmyNdGzin7olVN3p5aOnyIjz7WLlHEYBY.QZabfSxC.K0aYQfC', 'USER'),
(5, 'Car Lover', 'car@example.com', '01700000005', '$2a$10$slYQmyNdGzin7olVN3p5aOnyIjz7WLlHEYBY.QZabfSxC.K0aYQfC', 'USER'),
(6, 'Sneakerhead', 'sneaker@example.com', '01700000006', '$2a$10$slYQmyNdGzin7olVN3p5aOnyIjz7WLlHEYBY.QZabfSxC.K0aYQfC', 'USER'),
(7, 'Luxury Seller', 'luxury@example.com', '01700000007', '$2a$10$slYQmyNdGzin7olVN3p5aOnyIjz7WLlHEYBY.QZabfSxC.K0aYQfC', 'USER'),
(8, 'Admin User', 'admin@example.com', '01700000008', '$2a$10$slYQmyNdGzin7olVN3p5aOnyIjz7WLlHEYBY.QZabfSxC.K0aYQfC', 'ADMIN')
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  email = EXCLUDED.email,
  phone = EXCLUDED.phone,
  password = EXCLUDED.password,
  role = EXCLUDED.role;

-- Insert sample auction items with unified schema
INSERT INTO auction_items (id, title, category, description, bid_starting_price, minimum_bid_increment, bid_increment_type, bids_blocked, start_at, end_at, status, seller_id, current_highest_bid, total_bids, created_at, updated_at, currency) VALUES
(1, 'Vintage Guitar', 'Music', 'A beautiful vintage guitar in excellent condition', 500.00, 10.00, 'FIXED', false, '2026-04-15 10:00:00', '2026-04-20 10:00:00', 'PENDING', 1, 0.0, 0, '2026-04-14 12:00:00', '2026-04-14 12:00:00', 'USD'),
(2, 'Antique Watch', 'Collectibles', 'Rare antique pocket watch from the 1800s', 1000.00, 50.00, 'FIXED', false, '2026-04-16 14:00:00', '2026-04-25 14:00:00', 'PENDING', 2, 0.0, 0, '2026-04-14 12:00:00', '2026-04-14 12:00:00', 'USD'),
(3, 'Modern Art Painting', 'Art', 'Contemporary abstract painting by local artist', 750.00, 25.00, 'PERCENTAGE', false, '2026-04-14 09:00:00', '2026-04-22 09:00:00', 'ACTIVE', 3, 937.5, 2, '2026-04-14 12:00:00', '2026-04-15 10:00:00', 'USD'),
(4, 'Laptop Computer', 'Electronics', 'High-performance gaming laptop', 1200.00, 50.00, 'FIXED', false, '2026-04-14 16:00:00', '2026-04-28 16:00:00', 'ACTIVE', 4, 1350.0, 4, '2026-04-14 12:00:00', '2026-04-15 10:00:00', 'USD'),
(5, 'Sports Car Model', 'Toys', 'Detailed scale model of a Ferrari', 150.00, 5.00, 'FIXED', false, '2026-04-19 11:00:00', '2026-04-24 11:00:00', 'REJECTED', 5, 0.0, 0, '2026-04-14 12:00:00', '2026-04-14 12:00:00', 'USD'),
(6, 'Sneaker Collection', 'Fashion', 'Limited edition sneakers, size 10', 220.00, 15.00, 'FIXED', false, '2026-04-20 13:00:00', '2026-04-27 13:00:00', 'UPCOMING', 6, 0.0, 0, '2026-04-14 12:00:00', '2026-04-14 12:00:00', 'USD'),
(7, 'Designer Handbag', 'Fashion', 'Premium leather handbag with original packaging', 950.00, 35.00, 'PERCENTAGE', false, '2026-04-17 14:00:00', '2026-04-24 14:00:00', 'CANCELLED', 7, 0.0, 0, '2026-04-14 12:00:00', '2026-04-14 12:00:00', 'USD')
ON CONFLICT (id) DO UPDATE SET
  title = EXCLUDED.title,
  category = EXCLUDED.category,
  description = EXCLUDED.description,
  bid_starting_price = EXCLUDED.bid_starting_price,
  minimum_bid_increment = EXCLUDED.minimum_bid_increment,
  bid_increment_type = EXCLUDED.bid_increment_type,
  bids_blocked = EXCLUDED.bids_blocked,
  start_at = EXCLUDED.start_at,
  end_at = EXCLUDED.end_at,
  status = EXCLUDED.status,
  seller_id = EXCLUDED.seller_id,
  current_highest_bid = EXCLUDED.current_highest_bid,
  total_bids = EXCLUDED.total_bids,
  updated_at = EXCLUDED.updated_at,
  currency = EXCLUDED.currency;

-- Insert admin action logs
INSERT INTO admin_action_logs (id, auction_id, auction_title, action_type, reason, performed_by, performed_at) VALUES
(1, 1, 'Vintage Guitar', 'REJECT', 'Item appears counterfeit', 'admin', '2026-04-14 12:10:00'),
(2, 2, 'Antique Watch', 'CANCEL', 'Seller requested cancellation', 'moderator', '2026-04-14 12:15:00'),
(3, 3, 'Modern Art Painting', 'APPROVE', NULL, 'admin', '2026-04-14 12:20:00'),
(4, 4, 'Laptop Computer', 'BLOCK_BIDS', 'Suspected bid manipulation', 'admin', '2026-04-14 12:25:00'),
(5, 5, 'Sports Car Model', 'REJECT', 'Duplicate listing detected', 'superadmin', '2026-04-14 12:30:00'),
(6, 4, 'Laptop Computer', 'UNBLOCK_BIDS', 'Investigation complete, bidding resumes', 'admin', '2026-04-14 12:45:00'),
(7, 3, 'Modern Art Painting', 'MANUAL_CLOSE', 'Auction closed early due to policy review', 'admin', '2026-04-14 13:00:00')
ON CONFLICT (id) DO UPDATE SET
  auction_id = EXCLUDED.auction_id,
  auction_title = EXCLUDED.auction_title,
  action_type = EXCLUDED.action_type,
  reason = EXCLUDED.reason,
  performed_by = EXCLUDED.performed_by,
  performed_at = EXCLUDED.performed_at;

-- Insert sample bids for demo
INSERT INTO bids (id, auction_id, bidder_username, amount, placed_at) VALUES
(1, 4, 'John Doe', 1250.00, '2026-04-14 16:05:00'),
(2, 4, 'Jane Smith', 1300.00, '2026-04-14 16:10:00'),
(3, 4, 'Mike Artist', 1350.00, '2026-04-14 16:15:00'),
(4, 4, 'Tech Guru', 1400.00, '2026-04-15 09:00:00'),
(5, 3, 'Car Lover', 937.50, '2026-04-14 09:05:00'),
(6, 3, 'Sneakerhead', 1171.88, '2026-04-14 09:10:00')
ON CONFLICT (id) DO UPDATE SET
  auction_id = EXCLUDED.auction_id,
  bidder_username = EXCLUDED.bidder_username,
  amount = EXCLUDED.amount,
  placed_at = EXCLUDED.placed_at;