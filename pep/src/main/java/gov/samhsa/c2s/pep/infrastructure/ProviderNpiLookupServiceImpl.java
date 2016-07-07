package gov.samhsa.c2s.pep.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tomson.ngassa on 7/7/2016.
 */
@Service
@ConfigurationProperties(prefix = "pep.providers")
public class ProviderNpiLookupServiceImpl {
    private Map<String, String> users = new HashMap<>();

    public Map<String, String> getUsers() {
        return users;
    }
}
