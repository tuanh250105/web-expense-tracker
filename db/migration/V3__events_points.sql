-- Events & Points module
-- Idempotent DDL for events, points, event_transactions

CREATE TABLE IF NOT EXISTS events (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  goal_amount NUMERIC(18,2),
  start_date DATE,
  end_date DATE,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS points (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  score INT NOT NULL DEFAULT 0,
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS event_transactions (
  id BIGSERIAL PRIMARY KEY,
  event_id BIGINT NOT NULL,
  transaction_id BIGINT NOT NULL,
  UNIQUE(event_id, transaction_id)
);


