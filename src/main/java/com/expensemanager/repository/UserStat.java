package com.expensemanager.repository;

public class UserStat {
    public Object period;
    public Long count;

    public UserStat(Object period, Long count) {
        this.period = period;
        this.count = count;
    }

    public Object getPeriod() { return period; }
    public Long getCount() { return count; }
}