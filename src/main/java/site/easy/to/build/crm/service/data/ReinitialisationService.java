package site.easy.to.build.crm.service.data;

import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ReinitialisationService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void resetDatabase() {
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
    
        List<String> tables = entityManager.createNativeQuery(
            "SELECT table_name FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() " +  
            "AND table_name NOT IN ('users', 'oauth_users', 'user_profile', 'roles', 'user_roles')"
        ).getResultList();
    
        for (String table : tables) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
        }
    
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }
}
