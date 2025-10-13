package com.expensemanager.service;

import com.expensemanager.dao.DebtDAO;
import com.expensemanager.model.Debt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DebtService - service xử lý nghiệp vụ về Debt.
 * - Nếu có DebtDAO thì dùng DAO (kết nối DB).
 * - Nếu không có/khởi tạo DAO thất bại thì fallback về in-memory list.
 */
public class DebtService {

    private final DebtDAO debtDAO;
    private final List<Debt> inMemoryDebts;

    private static final UUID SAMPLE_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    /**
     * Constructor mặc định: cố gắng tạo DebtDAO (dùng DB). Nếu lỗi -> fallback in-memory.
     */
    public DebtService() {
        DebtDAO dao = null;
        List<Debt> mem = null;
        try {
            dao = new DebtDAO(); // DebtDAO mặc định sẽ sử dụng JpaUtil nếu cần
        } catch (Throwable t) {
            // Nếu không tạo được DAO (ví dụ JPA chưa cấu hình), fallback về in-memory
            dao = null;
            mem = Collections.synchronizedList(new ArrayList<>());
        }
        this.debtDAO = dao;
        this.inMemoryDebts = mem;
    }

    /**
     * Constructor nhận DebtDAO. Nếu dao == null sẽ cố gắng tạo mới.
     */
    public DebtService(DebtDAO dao) {
        DebtDAO created = dao;
        List<Debt> mem = null;
        if (created == null) {
            try {
                created = new DebtDAO();
            } catch (Throwable t) {
                created = null;
                mem = Collections.synchronizedList(new ArrayList<>());
            }
        }
        this.debtDAO = created;
        this.inMemoryDebts = mem;
    }

    public List<Debt> getAllDebts() {
        return getAllDebts(null);
    }

    public List<Debt> getAllDebts(UUID userId) {
        if (usingDao()) {
            List<Debt> list = (userId == null) ? safeCallFindAll() : safeCallFindAllByUser(userId);
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
            Debt d = safeCallFindById(id);
            normalizeStatus(d);
            return d;
        } else {
            synchronized (inMemoryDebts) {
                return inMemoryDebts.stream().filter(d -> id.equals(d.getId())).findFirst().orElse(null);
            }
        }
    }

    public Debt addDebt(Debt debt) {
        if (debt == null) throw new IllegalArgumentException("Debt is null");
        if (usingDao()) {
            if (debt.getStatus() == null) debt.setStatus(Debt.STATUS_PENDING);
            return safeCallSaveOrUpdate(debt);
        } else {
            if (debt.getId() == null) debt.setId(UUID.randomUUID());
            if (debt.getStatus() == null) debt.setStatus(Debt.STATUS_PENDING);
            if (debt.getUserId() == null) debt.setUserId(SAMPLE_USER_ID);
            synchronized (inMemoryDebts) {
                inMemoryDebts.add(debt);
            }
            return debt;
        }
    }

    public boolean updateDebt(Debt updated) {
        if (updated == null || updated.getId() == null) return false;
        if (usingDao()) {
            Debt saved = safeCallSaveOrUpdate(updated);
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
            return safeCallDelete(id);
        } else {
            synchronized (inMemoryDebts) {
                return inMemoryDebts.removeIf(d -> d.getId().equals(id));
            }
        }
    }

    public boolean markAsPaid(UUID id) {
        if (id == null) return false;
        if (usingDao()) {
            return safeCallMarkAsPaid(id);
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
            List<Debt> list = (userId == null) ? safeCallFindOverdue() : safeCallFindOverdueByUser(userId);
            list.forEach(this::normalizeStatus);
            return list;
        } else {
            synchronized (inMemoryDebts) {
                return inMemoryDebts.stream()
                        .filter(Debt::isOverdue)
                        .filter(d -> userId == null || userId.equals(d.getUserId()))
                        .sorted(Comparator.comparing(Debt::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList());
            }
        }
    }

    public BigDecimal getTotalUnpaid() {
        return getTotalUnpaid(null);
    }

    public BigDecimal getTotalUnpaid(UUID userId) {
        if (usingDao()) {
            return (userId == null) ? safeCallGetTotalUnpaid() : safeCallGetTotalUnpaidByUser(userId);
        } else {
            synchronized (inMemoryDebts) {
                return inMemoryDebts.stream()
                        .filter(d -> !Debt.STATUS_PAID.equalsIgnoreCase(d.getStatus()))
                        .filter(d -> userId == null || userId.equals(d.getUserId()))
                        .map(Debt::getAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        }
    }

    public BigDecimal getTotalOverdue() {
        return getTotalOverdue(null);
    }

    public BigDecimal getTotalOverdue(UUID userId) {
        if (usingDao()) {
            return (userId == null) ? safeCallGetTotalOverdue() : safeCallGetTotalOverdueByUser(userId);
        } else {
            synchronized (inMemoryDebts) {
                return inMemoryDebts.stream()
                        .filter(d -> Debt.STATUS_OVERDUE.equalsIgnoreCase(d.getStatus()) || d.isOverdue())
                        .filter(d -> userId == null || userId.equals(d.getUserId()))
                        .map(Debt::getAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        }
    }

    public List<Debt> getNearDueDebts(int daysThreshold) {
        return getNearDueDebts(null, daysThreshold);
    }

    public List<Debt> getNearDueDebts(UUID userId, int daysThreshold) {
        if (usingDao()) {
            List<Debt> list = (userId == null) ? safeCallFindNearDue(daysThreshold) : safeCallFindNearDueByUser(userId, daysThreshold);
            list.forEach(this::normalizeStatus);
            return list;
        } else {
            synchronized (inMemoryDebts) {
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

    /* --------------------------
       Wrapper an toàn gọi vào DAO
       (tránh ném exception thẳng lên controller).
       Nếu DAO lỗi thì log và trả fallback.
       -------------------------- */
    private List<Debt> safeCallFindAll() {
        try {
            return debtDAO.findAll();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<Debt> safeCallFindAllByUser(UUID userId) {
        try {
            return debtDAO.findAllByUser(userId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Debt safeCallFindById(UUID id) {
        try {
            return debtDAO.findById(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Debt safeCallSaveOrUpdate(Debt d) {
        try {
            return debtDAO.saveOrUpdate(d);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean safeCallDelete(UUID id) {
        try {
            return debtDAO.delete(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean safeCallMarkAsPaid(UUID id) {
        try {
            return debtDAO.markAsPaid(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<Debt> safeCallFindOverdue() {
        try {
            return debtDAO.findOverdue();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<Debt> safeCallFindOverdueByUser(UUID userId) {
        try {
            return debtDAO.findOverdueByUser(userId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<Debt> safeCallFindNearDue(int days) {
        try {
            return debtDAO.findNearDue(days);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<Debt> safeCallFindNearDueByUser(UUID userId, int days) {
        try {
            return debtDAO.findNearDueByUser(userId, days);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private BigDecimal safeCallGetTotalUnpaid() {
        try {
            return debtDAO.getTotalUnpaid();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal safeCallGetTotalUnpaidByUser(UUID userId) {
        try {
            return debtDAO.getTotalUnpaidByUser(userId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal safeCallGetTotalOverdue() {
        try {
            return debtDAO.getTotalOverdue();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal safeCallGetTotalOverdueByUser(UUID userId) {
        try {
            return debtDAO.getTotalOverdueByUser(userId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }
}
