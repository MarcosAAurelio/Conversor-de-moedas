package com.marcos.conversordemoedas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConversorDeMoedasApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
    }

    @Test
    void h2ConsoleIsDisabledByDefault() {
        assertThat(environment.getProperty("spring.h2.console.enabled", Boolean.class)).isFalse();
    }
}
