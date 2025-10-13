-- Budgets module
-- Idempotent DDL for budgets table and related indexes

CREATE TABLE IF NOT EXISTS budgets (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  period_type VARCHAR(16) NOT NULL CHECK (period_type IN ('WEEK','MONTH')),
  period_start DATE NOT NULL,
  period_end   DATE NOT NULL,
  category_id  BIGINT,
  limit_amount NUMERIC(18,2) NOT NULL,
  spent_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
  note TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_budgets_user_period ON budgets(user_id, period_start, period_end);


