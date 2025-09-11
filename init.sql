-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.accounts (
                                 id uuid NOT NULL DEFAULT gen_random_uuid(),
                                 user_id uuid NOT NULL,
                                 name character varying NOT NULL,
                                 balance numeric DEFAULT 0,
                                 currency character varying DEFAULT 'USD'::character varying,
                                 created_at timestamp without time zone DEFAULT now(),
                                 updated_at timestamp without time zone DEFAULT now(),
                                 CONSTRAINT accounts_pkey PRIMARY KEY (id),
                                 CONSTRAINT accounts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.budgets (
                                id uuid NOT NULL DEFAULT gen_random_uuid(),
                                user_id uuid NOT NULL,
                                category character varying NOT NULL,
                                limit_amount numeric NOT NULL,
                                start_date date NOT NULL,
                                end_date date NOT NULL,
                                spent_amount numeric DEFAULT 0,
                                created_at timestamp without time zone DEFAULT now(),
                                updated_at timestamp without time zone DEFAULT now(),
                                CONSTRAINT budgets_pkey PRIMARY KEY (id),
                                CONSTRAINT budgets_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.debts (
                              id uuid NOT NULL DEFAULT gen_random_uuid(),
                              user_id uuid NOT NULL,
                              creditor_name character varying NOT NULL,
                              amount numeric NOT NULL,
                              due_date date,
                              status character varying DEFAULT 'PENDING'::character varying,
                              note text,
                              created_at timestamp without time zone DEFAULT now(),
                              updated_at timestamp without time zone DEFAULT now(),
                              CONSTRAINT debts_pkey PRIMARY KEY (id),
                              CONSTRAINT debts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.group_expense_shares (
                                             id uuid NOT NULL DEFAULT gen_random_uuid(),
                                             expense_id uuid NOT NULL,
                                             user_id uuid NOT NULL,
                                             share_amount numeric NOT NULL,
                                             status character varying DEFAULT 'UNPAID'::character varying,
                                             created_at timestamp without time zone DEFAULT now(),
                                             CONSTRAINT group_expense_shares_pkey PRIMARY KEY (id),
                                             CONSTRAINT group_expense_shares_expense_id_fkey FOREIGN KEY (expense_id) REFERENCES public.group_expenses(id),
                                             CONSTRAINT group_expense_shares_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.group_expenses (
                                       id uuid NOT NULL DEFAULT gen_random_uuid(),
                                       group_id uuid NOT NULL,
                                       paid_by uuid NOT NULL,
                                       amount numeric NOT NULL,
                                       description text,
                                       expense_date timestamp without time zone NOT NULL,
                                       created_at timestamp without time zone DEFAULT now(),
                                       CONSTRAINT group_expenses_pkey PRIMARY KEY (id),
                                       CONSTRAINT group_expenses_paid_by_fkey FOREIGN KEY (paid_by) REFERENCES public.users(id),
                                       CONSTRAINT group_expenses_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id)
);
CREATE TABLE public.group_members (
                                      id uuid NOT NULL DEFAULT gen_random_uuid(),
                                      group_id uuid NOT NULL,
                                      user_id uuid NOT NULL,
                                      role character varying DEFAULT 'MEMBER'::character varying,
                                      joined_at timestamp without time zone DEFAULT now(),
                                      CONSTRAINT group_members_pkey PRIMARY KEY (id),
                                      CONSTRAINT group_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id),
                                      CONSTRAINT group_members_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id)
);
CREATE TABLE public.groups (
                               id uuid NOT NULL DEFAULT gen_random_uuid(),
                               name character varying NOT NULL,
                               description text,
                               created_by uuid NOT NULL,
                               created_at timestamp without time zone DEFAULT now(),
                               CONSTRAINT groups_pkey PRIMARY KEY (id),
                               CONSTRAINT groups_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id)
);
CREATE TABLE public.scheduled_transactions (
                                               id uuid NOT NULL DEFAULT gen_random_uuid(),
                                               user_id uuid NOT NULL,
                                               account_id uuid NOT NULL,
                                               type character varying NOT NULL,
                                               category character varying NOT NULL,
                                               amount numeric NOT NULL,
                                               note text,
                                               schedule_cron character varying NOT NULL,
                                               next_run timestamp without time zone NOT NULL,
                                               active boolean DEFAULT true,
                                               created_at timestamp without time zone DEFAULT now(),
                                               CONSTRAINT scheduled_transactions_pkey PRIMARY KEY (id),
                                               CONSTRAINT scheduled_transactions_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.accounts(id),
                                               CONSTRAINT scheduled_transactions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.transactions (
                                     id uuid NOT NULL DEFAULT gen_random_uuid(),
                                     account_id uuid NOT NULL,
                                     user_id uuid NOT NULL,
                                     type character varying NOT NULL,
                                     category character varying NOT NULL,
                                     amount numeric NOT NULL,
                                     note text,
                                     transaction_date timestamp without time zone NOT NULL,
                                     created_at timestamp without time zone DEFAULT now(),
                                     updated_at timestamp without time zone DEFAULT now(),
                                     CONSTRAINT transactions_pkey PRIMARY KEY (id),
                                     CONSTRAINT transactions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id),
                                     CONSTRAINT transactions_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.accounts(id)
);
CREATE TABLE public.users (
                              id uuid NOT NULL DEFAULT gen_random_uuid(),
                              username character varying NOT NULL UNIQUE,
                              email character varying NOT NULL UNIQUE,
                              password_hash character varying NOT NULL,
                              full_name character varying,
                              role character varying DEFAULT 'USER'::character varying,
                              provider character varying DEFAULT 'LOCAL'::character varying,
                              created_at timestamp without time zone DEFAULT now(),
                              updated_at timestamp without time zone DEFAULT now(),
                              CONSTRAINT users_pkey PRIMARY KEY (id)
);