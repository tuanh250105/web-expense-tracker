package com.expensemanager.repository;

public class UserStat {
    public String period;
    public long count;

    public UserStat(String period, long count) {
        this.period = period;
        this.count = count;
    }

    public String getPeriod() { return period; }
    public long getCount() { return count; }
}