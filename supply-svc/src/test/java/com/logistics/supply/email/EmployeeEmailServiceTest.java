package com.logistics.supply.email;

import com.logistics.supply.enums.EmailType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmployeeEmailServiceTest {

    @Autowired
    EmployeeEmailService employeeEmailService;

    @Test
    void shouldSendMail() throws Exception{
        employeeEmailService.sendMail("derrickagyemang12@gmail.com", EmailType.EMPLOYEE_DISABLED, "derrickagyemang12@outlook.com");
    }
}