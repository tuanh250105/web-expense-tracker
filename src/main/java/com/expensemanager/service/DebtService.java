package com.expensemanager.service;

import com.expensemanager.dao.DebtDAO;
import com.expensemanager.model.Debt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DebtService {

    private final DebtDAO debtDAO;
    private final List<Debt> inMemoryDebts;

    private static final UUID SAMPLE_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    public DebtService() {
        this.debtDAO = null;
        this.inMemoryDebts = Collections.synchronizedList(new ArrayList<>());
    }

    public DebtService(DebtDAO dao) {
        this.debtDAO = dao;
        this.inMemoryDebts = null;
    }

    public List<Debt> getAllDebts() {
        return getAllDebts(null);
    }

    public List<Debt> getAllDebts(UUID userId) {
        if (usingDao()) {
            List<Debt> list = (userId == null) ? debtDAO.findAll() : debtDAO.findAllByUser(userId);
            list.forEach(this::normalizeStatus);
            return list;
        } else {
            inMemoryDebts.forEach(this::normalizeStatus);
            synchronized (inMemoryDebts) {
                if (userId == null) return new ArrayList<>(inMemoryDebts);
                return inMemoryDebts.stream()
                        .filter(d -> userId.equals(d.getUserId()))
                        .collect(Collectors.toList());
            }
        }
    }

    public Debt getById(UUID id) {
        if (id == null) return null;
        if (usingDao()) {
            Debt d = debtDAO.findById(id);
            normalizeStatus(d);
            return d;
        } else {
            return inMemoryDebts.stream().filter(d -> id.equals(d.getId())).findFirst().orElse(null);
        }
    }

    public Debt addDebt(Debt debt) {
        if (debt == null) throw new IllegalArgumentException("Debt is null");
        if (usingDao()) {
            if (debt.getStatus() == null) debt.setStatus(Debt.STATUS_PENDING);
            return debtDAO.saveOrUpdate(debt);
        } else {
            if (debt.getId() == null) debt.setId(UUID.randomUUID());
            if (debt.getStatus() == null) debt.setStatus(Debt.STATUS_PENDING);
            if (debt.getUserId() == null) debt.setUserId(SAMPLE_USER_ID);
            inMemoryDebts.add(debt);
            return debt;
        }
    }

    public boolean updateDebt(Debt updated) {
        if (updated == null || updated.getId() == null) return false;
        if (usingDao()) {
            Debt saved = debtDAO.saveOrUpdate(updated);
            return saved != null;
        } else {
            synchronized (inMemoryDebts) {
                for (int i = 0; i < inMemoryDebts.size(); i++) {
                    Debt d = inMemoryDebts.get(i);
                    if (d.getId().equals(updated.getId())) {
                        d.setCreditorName(updated.getCreditorName());
                        d.setAmount(updated.getAmount());
                        d.setDueDate(updated.getDueDate());
                        d.setStatus(updated.getStatus());
                        d.setNote(updated.getNote());
                        if (updated.getUserId() != null) {
                            d.setUserId(updated.getUserId());
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean deleteDebt(UUID id) {
        if (id == null) return false;
        if (usingDao()) {
            return debtDAO.delete(id);
        } else {
            return inMemoryDebts.removeIf(d -> d.getId().equals(id));
        }
    }

    public boolean markAsPaid(UUID id) {
        if (id == null) return false;
        if (usingDao()) {
            return debtDAO.markAsPaid(id);
        } else {
            Debt d = getById(id);
            if (d == null) return false;
            d.setStatus(Debt.STATUS_PAID);
            return true;
        }
    }

    public List<Debt> getOverdueDebts() {
        return getOverdueDebts(null);
    }

    public List<Debt> getOverdueDebts(UUID userId) {
        if (usingDao()) {
            List<Debt> list = (userId == null) ? debtDAO.findOverdue() : debtDAO.findOverdueByUser(userId);
            list.forEach(this::normalizeStatus);
            return list;
        } else {
            return inMemoryDebts.stream()
                    .filter(Debt::isOverdue)
                    .filter(d -> userId == null || userId.equals(d.getUserId()))
                    .sorted(Comparator.comparing(Debt::getDueDate))
                    .collect(Collectors.toList());
        }
    }

    public BigDecimal getTotalUnpaid() {
        return getTotalUnpaid(null);
    }

    public BigDecimal getTotalUnpaid(UUID userId) {
        if (usingDao()) {
            return (userId == null) ? debtDAO.getTotalUnpaid() : debtDAO.getTotalUnpaidByUser(userId);
        } else {
            return inMemoryDebts.stream()
                    .filter(d -> !Debt.STATUS_PAID.equalsIgnoreCase(d.getStatus()))
                    .filter(d -> userId == null || userId.equals(d.getUserId()))
                    .map(Debt::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public BigDecimal getTotalOverdue() {
        return getTotalOverdue(null);
    }

    public BigDecimal getTotalOverdue(UUID userId) {
        if (usingDao()) {
            return (userId == null) ? debtDAO.getTotalOverdue() : debtDAO.getTotalOverdueByUser(userId);
        } else {
            return inMemoryDebts.stream()
                    .filter(d -> Debt.STATUS_OVERDUE.equalsIgnoreCase(d.getStatus()) || d.isOverdue())
                    .filter(d -> userId == null || userId.equals(d.getUserId()))
                    .map(Debt::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public List<Debt> getNearDueDebts(int daysThreshold) {
        return getNearDueDebts(null, daysThreshold);
    }

    public List<Debt> getNearDueDebts(UUID userId, int daysThreshold) {
        if (usingDao()) {
            List<Debt> list = (userId == null) ? debtDAO.findNearDue(daysThreshold) : debtDAO.findNearDueByUser(userId, daysThreshold);
            list.forEach(this::normalizeStatus);
            return list;
        } else {
            LocalDate now = LocalDate.now();
            return inMemoryDebts.stream()
                    .filter(d -> d.getDueDate() != null)
                    .filter(d -> !Debt.STATUS_PAID.equalsIgnoreCase(d.getStatus()))
                    .filter(d -> userId == null || userId.equals(d.getUserId()))
                    .filter(d -> {
                        long days = d.getDaysUntilDue();
                        return days >= 0 && days <= daysThreshold;
                    })
                    .sorted(Comparator.comparing(Debt::getDueDate))
                    .collect(Collectors.toList());
        }
    }

    public List<Debt> getNearDueDebts() {
        return getNearDueDebts(3);
    }

    public List<Debt> getNearDueDebts(UUID userId) {
        return getNearDueDebts(userId, 3);
    }

    public int getNearDueCount() {
        return getNearDueCount(null);
    }

    public int getNearDueCount(UUID userId) {
        return getNearDueDebts(userId).size();
    }

    private boolean usingDao() {
        return debtDAO != null;
    }

    private void normalizeStatus(Debt d) {
        if (d == null) return;
        try {
            if (d.isOverdue()) d.setStatus(Debt.STATUS_OVERDUE);
            else if (d.getStatus() == null) d.setStatus(Debt.STATUS_PENDING);
        } catch (Exception ignored) {
            if (d.getStatus() == null) d.setStatus(Debt.STATUS_PENDING);
        }
    }
}
