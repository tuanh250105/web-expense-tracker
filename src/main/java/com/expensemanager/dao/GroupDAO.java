package com.expensemanager.dao;

import com.expensemanager.model.Group;
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

    private EntityManager em;

    public GroupDAO(EntityManager em) {
        this.em = JpaUtil.getEntityManager();
    }

    /**
     * Lấy danh sách các nhóm mà người dùng thuộc về.
     *
     * @param userId ID người dùng
     * @return Danh sách mảng Object chứa {groupId, name, description, createdById}
     */
    public List<Object[]> findGroupsByUserId(UUID userId) {
        String jpql = """
            SELECT g.id, g.name, g.description, g.createdBy.id
            FROM Group g
            JOIN g.groupMembers m
            WHERE m.user.id = :userId
        """;
        return em.createQuery(jpql, Object[].class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Lấy danh sách các thành viên của một nhóm.
     *
     * @param groupId ID nhóm
     * @return Danh sách {userId, fullName}
     */
    public List<Object[]> findMembersByGroupId(UUID groupId) {
        String jpql = """
            SELECT u.id, u.fullName
            FROM GroupMember gm
            JOIN gm.user u
            WHERE gm.group.id = :groupId
        """;
        return em.createQuery(jpql, Object[].class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    /**
     * Tính tổng chi tiêu của nhóm.
     *
     * @param groupId ID nhóm
     * @return Tổng tiền (BigDecimal)
     */
    public BigDecimal sumExpensesByGroupId(UUID groupId) {
        String jpql = "SELECT SUM(e.amount) FROM GroupExpense e WHERE e.group.id = :groupId";
        TypedQuery<Long> query = em.createQuery(jpql, Long.class); // <-- Sửa ở đây
        query.setParameter("groupId", groupId);

        // getSingleResult() có thể trả về null nếu không có kết quả
        Long totalSum = query.getSingleResult();

        // Chuyển đổi an toàn từ Long sang BigDecimal
        if (totalSum == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(totalSum);
    }

    /**
     * Tìm Group theo ID.
     */
    public Group findById(UUID groupId) {
        return em.find(Group.class, groupId);
    }

    /**
     * Lưu hoặc cập nhật Group.
     */
    public void save(Group group) {
        if (group.getId() == null) {
            em.persist(group);
        } else {
            em.merge(group);
        }
    }

    /**
     * Xóa Group.
     */
    public void delete(Group group) {
        if (!em.contains(group)) {
            group = em.merge(group);
        }
        em.remove(group);
    }

    /**
     * Lưu thông tin thành viên nhóm.
     */
    public void saveMember(GroupMember member) {
        em.persist(member);
    }

    /**
     * Đếm số lượng thành viên trong nhóm.
     */
    public long countMembers(UUID groupId) {
        String jpql = "SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId";
        return em.createQuery(jpql, Long.class)
                .setParameter("groupId", groupId)
                .getSingleResult();
    }

    /**
     * Xóa thành viên ra khỏi nhóm.
     */
    public void deleteMember(UUID groupId, UUID userId) {
        String jpql = """
            DELETE FROM GroupMember gm
            WHERE gm.group.id = :groupId AND gm.user.id = :userId
        """;
        em.createQuery(jpql)
                .setParameter("groupId", groupId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    /**
     * Kiểm tra xem người dùng có thuộc nhóm hay không.
     */
    public boolean isUserMember(UUID groupId, UUID userId) {
        String jpql = """
            SELECT COUNT(gm)
            FROM GroupMember gm
            WHERE gm.group.id = :groupId AND gm.user.id = :userId
        """;
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("groupId", groupId)
                .setParameter("userId", userId)
                .getSingleResult();
        return count > 0;
    }
}
