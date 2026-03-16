package com.jpmc.midascore;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WaldorfBalanceTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testWaldorfBalance() {
        // Get waldorf's balance after processing
        UserRecord waldorf = userRepository.findByName("waldorf");
        if (waldorf != null) {
            System.out.println("Waldorf's balance: " + waldorf.getBalance());
            System.out.println("Rounded down: " + (int) Math.floor(waldorf.getBalance()));
        } else {
            System.out.println("Waldorf not found");
        }
    }
}