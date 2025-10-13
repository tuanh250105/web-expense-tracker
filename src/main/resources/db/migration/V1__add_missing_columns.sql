ALTER TABLE public.budgets ADD COLUMN IF NOT EXISTS user_id UUID REFERENCES public.users (id) ON DELETE CASCADE;
ALTER TABLE public.events ADD COLUMN IF NOT EXISTS budget_amount numeric(15,2);
ALTER TABLE public.events ADD COLUMN IF NOT EXISTS user_id UUID REFERENCES public.users (id) ON DELETE CASCADE;

