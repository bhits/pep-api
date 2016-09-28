package gov.samhsa.c2s.pep.infrastructure;

import java.util.Map;

public interface ProviderNpiLookupService {

    /**
     * @return username -> NPI mappings
     */
    Map<String, String> getUsers();
}
