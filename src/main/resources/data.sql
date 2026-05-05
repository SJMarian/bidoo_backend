-- ============================================================
-- Bidoo Seed Data
-- All inserts use ON CONFLICT DO UPDATE so they are safe to re-run
-- ============================================================

-- Users (password = "password", stored in plaintext)
INSERT INTO users (id, name, email, phone, password, role) VALUES
(1,'John Doe','john@example.com','01700000001','password','USER'),
(2,'Jane Smith','jane@example.com','01700000002','password','USER'),
(3,'Mike Artist','mike@example.com','01700000003','password','USER'),
(4,'Tech Guru','tech@example.com','01700000004','password','USER'),
(5,'Car Lover','car@example.com','01700000005','password','USER'),
(6,'Sneakerhead','sneaker@example.com','01700000006','password','USER'),
(7,'Luxury Seller','luxury@example.com','01700000007','password','USER'),
(8,'Admin User','admin@example.com','01700000008','password','ADMIN')
ON CONFLICT (id) DO UPDATE SET name=EXCLUDED.name, email=EXCLUDED.email,
  phone=EXCLUDED.phone, password=EXCLUDED.password, role=EXCLUDED.role;

-- Auction items
INSERT INTO auction_items (id,title,category,description,bid_starting_price,minimum_bid_increment,
  bid_increment_type,bids_blocked,start_at,end_at,status,seller_id,
  current_highest_bid,total_bids,created_at,updated_at,currency) VALUES
(1,'Vintage Guitar','Music','A beautiful vintage guitar in excellent condition',
  500,10,'FIXED',false,'2026-04-15 10:00:00','2026-04-30 10:00:00','PENDING',1,0,0,'2026-04-14 12:00:00','2026-04-14 12:00:00','USD'),
(2,'Antique Watch','Collectibles','Rare antique pocket watch from the 1800s',
  1000,50,'FIXED',false,'2026-04-16 14:00:00','2026-05-01 14:00:00','PENDING',2,0,0,'2026-04-14 12:00:00','2026-04-14 12:00:00','USD'),
(3,'Modern Art Painting','Art','Contemporary abstract painting by local artist',
  750,25,'PERCENTAGE',false,'2026-04-14 09:00:00','2026-04-30 09:00:00','ACTIVE',3,937.5,2,'2026-04-14 12:00:00','2026-04-15 10:00:00','USD'),
(4,'Laptop Computer','Electronics','High-performance gaming laptop',
  1200,50,'FIXED',false,'2026-04-14 16:00:00','2026-04-30 16:00:00','ACTIVE',4,1400,4,'2026-04-14 12:00:00','2026-04-15 10:00:00','USD'),
(5,'Sports Car Model','Toys','Detailed scale model of a Ferrari',
  150,5,'FIXED',false,'2026-04-19 11:00:00','2026-04-30 11:00:00','REJECTED',5,0,0,'2026-04-14 12:00:00','2026-04-14 12:00:00','USD'),
(6,'Sneaker Collection','Fashion','Limited edition sneakers, size 10',
  220,15,'FIXED',false,'2026-04-25 13:00:00','2026-05-05 13:00:00','UPCOMING',6,0,0,'2026-04-14 12:00:00','2026-04-14 12:00:00','USD'),
(7,'Designer Handbag','Fashion','Premium leather handbag with original packaging',
  950,35,'PERCENTAGE',false,'2026-04-17 14:00:00','2026-04-30 14:00:00','PAID',7,1250,3,'2026-04-14 12:00:00','2026-04-14 12:00:00','USD')
ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,category=EXCLUDED.category,
  description=EXCLUDED.description,bid_starting_price=EXCLUDED.bid_starting_price,
  minimum_bid_increment=EXCLUDED.minimum_bid_increment,bid_increment_type=EXCLUDED.bid_increment_type,
  bids_blocked=EXCLUDED.bids_blocked,start_at=EXCLUDED.start_at,end_at=EXCLUDED.end_at,
  status=EXCLUDED.status,seller_id=EXCLUDED.seller_id,current_highest_bid=EXCLUDED.current_highest_bid,
  total_bids=EXCLUDED.total_bids,updated_at=EXCLUDED.updated_at,currency=EXCLUDED.currency;

-- Admin action logs
INSERT INTO admin_action_logs (id,auction_id,auction_title,action_type,reason,performed_by,performed_at) VALUES
(1,1,'Vintage Guitar','REJECT','Item appears counterfeit','admin','2026-04-14 12:10:00'),
(2,2,'Antique Watch','CANCEL','Seller requested cancellation','moderator','2026-04-14 12:15:00'),
(3,3,'Modern Art Painting','APPROVE',NULL,'admin','2026-04-14 12:20:00'),
(4,4,'Laptop Computer','BLOCK_BIDS','Suspected bid manipulation','admin','2026-04-14 12:25:00'),
(5,5,'Sports Car Model','REJECT','Duplicate listing detected','superadmin','2026-04-14 12:30:00'),
(6,4,'Laptop Computer','UNBLOCK_BIDS','Investigation complete','admin','2026-04-14 12:45:00'),
(7,3,'Modern Art Painting','MANUAL_CLOSE','Closed early due to review','admin','2026-04-14 13:00:00')
ON CONFLICT (id) DO UPDATE SET auction_id=EXCLUDED.auction_id,auction_title=EXCLUDED.auction_title,
  action_type=EXCLUDED.action_type,reason=EXCLUDED.reason,
  performed_by=EXCLUDED.performed_by,performed_at=EXCLUDED.performed_at;

-- Ensure identity sequence is ahead of seeded ids
SELECT setval(pg_get_serial_sequence('admin_action_logs','id'), (SELECT COALESCE(MAX(id), 1) FROM admin_action_logs), true);

-- Bids
INSERT INTO bids (id,auction_id,bidder_username,amount,placed_at) VALUES
(1,4,'john@example.com',1250,'2026-04-14 16:05:00'),
(2,4,'jane@example.com',1300,'2026-04-14 16:10:00'),
(3,4,'mike@example.com',1350,'2026-04-14 16:15:00'),
(4,4,'tech@example.com',1400,'2026-04-15 09:00:00'),
(5,3,'car@example.com',937.5,'2026-04-14 09:05:00'),
(6,3,'sneaker@example.com',1171.88,'2026-04-14 09:10:00'),
(7,7,'john@example.com',1000,'2026-04-17 14:10:00'),
(8,7,'jane@example.com',1150,'2026-04-17 14:20:00'),
(9,7,'mike@example.com',1250,'2026-04-17 14:35:00')
ON CONFLICT (id) DO UPDATE SET auction_id=EXCLUDED.auction_id,
  bidder_username=EXCLUDED.bidder_username,amount=EXCLUDED.amount,placed_at=EXCLUDED.placed_at;

-- Ensure identity sequence is ahead of seeded ids
SELECT setval(pg_get_serial_sequence('bids','id'), (SELECT COALESCE(MAX(id), 1) FROM bids), true);

-- Orders
INSERT INTO orders (id,auction_id,buyer_id,seller_id,final_price,status,created_at) VALUES
(1,7,1,7,1250,'PAID','2026-04-18 10:00:00')
ON CONFLICT (id) DO UPDATE SET auction_id=EXCLUDED.auction_id,buyer_id=EXCLUDED.buyer_id,
  seller_id=EXCLUDED.seller_id,final_price=EXCLUDED.final_price,
  status=EXCLUDED.status,created_at=EXCLUDED.created_at;

-- Ensure identity sequence is ahead of seeded ids
SELECT setval(pg_get_serial_sequence('orders','id'), (SELECT COALESCE(MAX(id), 1) FROM orders), true);

-- Payment
INSERT INTO payment (id,order_id,user_id,amount,currency,payment_method,gateway,payment_status,created_at,updated_at) VALUES
(1,1,1,1250,'USD','SSLCOMMERZ','SSLCOMMERZ','SUCCESS','2026-04-18 10:01:00','2026-04-18 10:05:00')
ON CONFLICT (id) DO UPDATE SET order_id=EXCLUDED.order_id,user_id=EXCLUDED.user_id,
  amount=EXCLUDED.amount,currency=EXCLUDED.currency,payment_method=EXCLUDED.payment_method,
  gateway=EXCLUDED.gateway,payment_status=EXCLUDED.payment_status,updated_at=EXCLUDED.updated_at;

-- Ensure identity sequence is ahead of seeded ids
SELECT setval(pg_get_serial_sequence('payment','id'), (SELECT COALESCE(MAX(id), 1) FROM payment), true);

-- Payment transaction
INSERT INTO payment_transaction (id,payment_id,gateway_trx_id,transaction_status,created_at) VALUES
(1,1,'TXN-DEMO-2026-001','SUCCESS','2026-04-18 10:05:00')
ON CONFLICT (id) DO UPDATE SET payment_id=EXCLUDED.payment_id,
  gateway_trx_id=EXCLUDED.gateway_trx_id,transaction_status=EXCLUDED.transaction_status,
  created_at=EXCLUDED.created_at;

-- Ensure identity sequence is ahead of seeded ids
SELECT setval(pg_get_serial_sequence('payment_transaction','id'), (SELECT COALESCE(MAX(id), 1) FROM payment_transaction), true);
