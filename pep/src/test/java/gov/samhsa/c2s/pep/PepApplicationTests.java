package gov.samhsa.c2s.pep;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@Ignore("Depends on config-server on bootstrap")
public class PepApplicationTests {

    @Test
    public void contextLoads() {
    }
}