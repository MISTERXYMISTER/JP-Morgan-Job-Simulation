package com.jpmc.midascore.component;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;

@Component
public class KafkaListenerComponent {

    private static final Logger logger = LoggerFactory.getLogger(KafkaListenerComponent.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RestTemplate restTemplate;

    // Static list to store the received transactions (for test inspection)
    public static final List<Transaction> RECEIVED_TRANSACTIONS = new ArrayList<>();

    /**
     * Listens for messages on the configured Kafka topic.
     * The framework automatically deserializes the JSON message body into a Transaction object.
     */
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    @Transactional
    public void listen(Transaction transaction) {
        // Place your debugger breakpoint here!
        RECEIVED_TRANSACTIONS.add(transaction);
        logger.info("Received transaction with amount: {}", transaction.getAmount());

        // Validate sender and recipient exist
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender == null || recipient == null) {
            logger.warn("Invalid transaction: sender or recipient not found");
            return;
        }

        // Validate sender has sufficient balance
        if (sender.getBalance() < transaction.getAmount()) {
            logger.warn("Invalid transaction: insufficient balance");
            return;
        }

        // Call incentive API
        String incentiveUrl = "http://localhost:8080/incentive";
        Incentive incentive = restTemplate.postForObject(incentiveUrl, transaction, Incentive.class);
        
        if (incentive == null) {
            logger.warn("Failed to get incentive from API");
            incentive = new Incentive(0.0f);
        }

        float incentiveAmount = incentive.getAmount();
        logger.info("Received incentive amount: {}", incentiveAmount);

        // Process valid transaction
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

        // Save updated user balances
        userRepository.save(sender);
        userRepository.save(recipient);

        // Create and save transaction record
        TransactionRecord transactionRecord = TransactionRecord.fromTransaction(transaction, sender, recipient, incentiveAmount);
        transactionRepository.save(transactionRecord);

        logger.info("Transaction processed successfully: {}", transactionRecord);
    }
}
