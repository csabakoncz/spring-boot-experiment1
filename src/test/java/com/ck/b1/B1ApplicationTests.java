package com.ck.b1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest()
class B1ApplicationTests {

    @Test
    void contextLoads() {
    }

}
