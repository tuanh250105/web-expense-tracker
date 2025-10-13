-- Seed sample budgets and one event (idempotent-ish)

-- sample budget weekly
INSERT INTO budgets (user_id, period_type, period_start, period_end, category_id, limit_amount, spent_amount, note)
SELECT 1, 'WEEK', CURRENT_DATE - INTERVAL '6 day', CURRENT_DATE, NULL, 500000, 0, 'Weekly sample'
WHERE NOT EXISTS (
  SELECT 1 FROM budgets WHERE user_id=1 AND period_type='WEEK' AND period_start=CURRENT_DATE - INTERVAL '6 day'
);

-- sample budget monthly
INSERT INTO budgets (user_id, period_type, period_start, period_end, category_id, limit_amount, spent_amount, note)
SELECT 1, 'MONTH', date_trunc('month', CURRENT_DATE), (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month' - INTERVAL '1 day')::date, NULL, 2000000, 0, 'Monthly sample'
WHERE NOT EXISTS (
  SELECT 1 FROM budgets WHERE user_id=1 AND period_type='MONTH' AND period_start=date_trunc('month', CURRENT_DATE)
);

-- sample event
INSERT INTO events (user_id, name, goal_amount, start_date, end_date, status)
SELECT 1, 'Đi du lịch', 3000000, CURRENT_DATE - INTERVAL '3 day', CURRENT_DATE + INTERVAL '10 day', 'ACTIVE'
WHERE NOT EXISTS (
  SELECT 1 FROM events WHERE user_id=1 AND name='Đi du lịch'
);


