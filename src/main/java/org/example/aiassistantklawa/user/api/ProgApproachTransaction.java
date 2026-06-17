//package org.example.aiassistantklawa.user.api;
//
//import jakarta.transaction.Transaction;
//import jakarta.transaction.TransactionManager;
//import org.example.aiassistantklawa.user.domain.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.TransactionStatus;
//
//@Configuration
//public class ProgApproachTransaction {
//    private final PlatformTransactionManager platformTransactionManager;
//    @Autowired
//    ProgApproachTransaction(PlatformTransactionManager platformTransactionManager) {
//        this.platformTransactionManager = platformTransactionManager;
//    }
//    public void update(User user) {
//        TransactionStatus transactionStatus
//        = platformTransactionManager.getTransaction(null);
//
//        try{
//
//            platformTransactionManager.commit(transactionStatus);
//        }
//        catch (Exception e){
//            platformTransactionManager.rollback(transactionStatus);
//        }
//
//    }
//}
