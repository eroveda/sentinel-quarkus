package com.sentinel.auditor;

import com.sentinel.auditor.service.SentinelService;
import com.sentinel.auditor.model.AuditReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SentinelServiceTests {
    @Autowired private SentinelService sentinelService;

    @Test
    void testMaliciousPattern() {
        AuditReport report = sentinelService.audit("rm -rf /");
        assertTrue(report.score() > 5);
    }
}
