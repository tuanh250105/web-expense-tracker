package com.expensemanager.dao;

import com.expensemanager.model.Group;
import com.expensemanager.model.GroupMember;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GroupDAO {
    private final EntityManager em;

    public GroupDAO(EntityManager em) {
        this.em = em;
    }

    public List<Object[]> findGroupsByUserId(UUID userId) {
        String jpql = "SELECT g.id, g.name, g.description, g.createdBy.id FROM Group g JOIN g.groupMembers m WHERE m.user.id = :userId";
        return em.createQuery(jpql, Object[].class).setParameter("userId", userId).getResultList();
    }

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


    public BigDecimal sumExpensesByGroupId(UUID groupId) {
        String jpql = "SELECT SUM(ge.amount) FROM GroupExpense ge WHERE ge.group.id = :groupId";
        TypedQuery<BigDecimal> query = em.createQuery(jpql, BigDecimal.class);
        query.setParameter("groupId", groupId);
        return Optional.ofNullable(query.getSingleResult()).orElse(BigDecimal.ZERO);
    }

    public Group findById(UUID groupId) {
        return em.find(Group.class, groupId);
    }

    public void save(Group group) {
        if (group.getId() == null) {
            em.persist(group);
        } else {
            em.merge(group);
        }
    }

    public void delete(Group group) {
        em.remove(group);
    }

    public void saveMember(GroupMember member) {
        em.persist(member);
    }

    public long countMembers(UUID groupId) {
        String jpql = "SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId";
        return em.createQuery(jpql, Long.class).setParameter("groupId", groupId).getSingleResult();
    }

    public void deleteMember(UUID groupId, UUID userId) {
        String jpql = "DELETE FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId";
        em.createQuery(jpql).setParameter("groupId", groupId).setParameter("userId", userId).executeUpdate();
    }

    public boolean isUserMember(UUID groupId, UUID userId) {
        String jpql = "SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId";
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("groupId", groupId)
                .setParameter("userId", userId)
                .getSingleResult();
        return count > 0;
    }
}