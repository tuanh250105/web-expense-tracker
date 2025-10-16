package com.expensemanager.dao;

import com.expensemanager.model.Group;
import com.expensemanager.model.GroupExpense;
import com.expensemanager.model.GroupMember;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DAO (Data Access Object) quản lý các truy vấn liên quan đến Group và GroupMember.
 * Cung cấp các thao tác CRUD cơ bản và các hàm thống kê/phụ trợ.
 */
public class GroupDAO {

    private final EntityManager em;

    // Constructor nhận EntityManager
    public GroupDAO(EntityManager em) {
        this.em = em;
    }

    // Constructor mặc định (sử dụng JpaUtil)
    public GroupDAO() {
        this.em = null;
    }

    private EntityManager getEm() {
        return (em != null) ? em : JpaUtil.getEntityManager();
    }

    private void closeEm(EntityManager e) {
        if (em == null) e.close(); // chỉ close nếu là em do DAO tạo
    }

    /**
     * Lấy danh sách các nhóm mà người dùng thuộc về.
     *
     * @param userId ID người dùng
     * @return Danh sách mảng Object chứa {groupId, name, description, createdById}
     */
    public List<Object[]> findGroupsByUserId(UUID userId) {
        EntityManager e = getEm();
        try {
            String jpql = """
                SELECT g.id, g.name, g.description, g.createdBy.id
                FROM Group g
                JOIN g.groupMembers m
                WHERE m.user.id = :userId
            """;
            return e.createQuery(jpql, Object[].class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            closeEm(e);
        }
    }

    /**
     * Lấy danh sách các thành viên của một nhóm.
     *
     * @param groupId ID nhóm
     * @return Danh sách {userId, fullName}
     */
    public List<Object[]> findMembersByGroupId(UUID groupId) {
        EntityManager e = getEm();
        try {
            String jpql = """
                SELECT u.id, u.fullName
                FROM GroupMember gm
                JOIN gm.user u
                WHERE gm.group.id = :groupId
            """;
            return e.createQuery(jpql, Object[].class)
                    .setParameter("groupId", groupId)
                    .getResultList();
        } finally {
            closeEm(e);
        }
    }

    /**
     * Tính tổng chi tiêu của nhóm.
     *
     * @param groupId ID nhóm
     * @return Tổng tiền (BigDecimal)
     */
    public BigDecimal sumExpensesByGroupId(UUID groupId) {
        EntityManager e = getEm();
        try {
            String jpql = "SELECT SUM(e.amount) FROM GroupExpense e WHERE e.group.id = :groupId";
            TypedQuery<Long> query = e.createQuery(jpql, Long.class);
            query.setParameter("groupId", groupId);
            Long total = query.getSingleResult();
            return (total != null) ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
        } finally {
            closeEm(e);
        }
    }


    /**
     * Tìm Group theo ID.
     */
    public Group findById(UUID groupId) {
        EntityManager e = getEm();
        try {
            return e.find(Group.class, groupId);
        } finally {
            closeEm(e);
        }
    }

    /**
     * Lưu hoặc cập nhật Group.
     */
    public void save(Group group) {
        EntityManager e = getEm();
        try {
            e.getTransaction().begin();
            if (group.getId() == null) {
                e.persist(group);
            } else {
                e.merge(group);
            }
            e.getTransaction().commit();
        } catch (Exception ex) {
            if (e.getTransaction().isActive()) e.getTransaction().rollback();
            throw ex;
        } finally {
            closeEm(e);
        }
    }

    /**
     * Xóa Group.
     */
    public void delete(Group group) {
        EntityManager e = getEm();
        try {
            e.getTransaction().begin();
            if (!e.contains(group)) {
                group = e.merge(group);
            }
            e.remove(group);
            e.getTransaction().commit();
        } catch (Exception ex) {
            if (e.getTransaction().isActive()) e.getTransaction().rollback();
            throw ex;
        } finally {
            closeEm(e);
        }
    }

    /**
     * Lưu thông tin thành viên nhóm.
     */
    public void saveMember(GroupMember member) {
        EntityManager e = getEm();
        try {
            e.getTransaction().begin();
            e.persist(member);
            e.getTransaction().commit();
        } catch (Exception ex) {
            if (e.getTransaction().isActive()) e.getTransaction().rollback();
            throw ex;
        } finally {
            closeEm(e);
        }
    }

    /**
     * Đếm số lượng thành viên trong nhóm.
     */
    public long countMembers(UUID groupId) {
        EntityManager e = getEm();
        try {
            String jpql = "SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId";
            return e.createQuery(jpql, Long.class)
                    .setParameter("groupId", groupId)
                    .getSingleResult();
        } finally {
            closeEm(e);
        }
    }

    /**
     * Xóa thành viên ra khỏi nhóm.
     */
    public void deleteMember(UUID groupId, UUID userId) {
        EntityManager e = getEm();
        try {
            e.getTransaction().begin();
            String jpql = """
                DELETE FROM GroupMember gm
                WHERE gm.group.id = :groupId AND gm.user.id = :userId
            """;
            e.createQuery(jpql)
                    .setParameter("groupId", groupId)
                    .setParameter("userId", userId)
                    .executeUpdate();
            e.getTransaction().commit();
        } catch (Exception ex) {
            if (e.getTransaction().isActive()) e.getTransaction().rollback();
            throw ex;
        } finally {
            closeEm(e);
        }
    }

    /**
     * Kiểm tra xem người dùng có thuộc nhóm hay không.
     */
    public boolean isUserMember(UUID groupId, UUID userId) {
        EntityManager e = getEm();
        try {
            String jpql = """
                SELECT COUNT(gm)
                FROM GroupMember gm
                WHERE gm.group.id = :groupId AND gm.user.id = :userId
            """;
            Long count = e.createQuery(jpql, Long.class)
                    .setParameter("groupId", groupId)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return count > 0;
        } finally {
            closeEm(e);
        }
    }
}
