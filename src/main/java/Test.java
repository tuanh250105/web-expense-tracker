
import jakarta.persistence.*;

public class Test {
    public static void main(String[] args) {
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("BudgetBuddyUnit");
            EntityManager em = emf.createEntityManager();

            em.getTransaction().begin();
            Long count = em.createQuery("SELECT COUNT(r) FROM RewardPrize r", Long.class).getSingleResult();
            System.out.println("✅ Kết nối Supabase OK. RewardPrize hiện có: " + count);
            em.getTransaction().commit();

            em.close();
            emf.close();
        } catch (Exception e) {
            System.err.println("❌ Lỗi kết nối:");
            e.printStackTrace();
        }
    }
}
