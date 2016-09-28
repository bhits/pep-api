package gov.samhsa.c2s.pep.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ConfigurationProperties(prefix = "pep.providers")
public class ProviderNpiLookupServiceImpl implements ProviderNpiLookupService {
    private Map<String, String> users = new HashMap<>();

    @Override
    public Map<String, String> getUsers() {
        return users;
    }
}
