package com.glizzy.milliondollarpicks.authservice.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsEntityFetcher;

import java.util.Map;

@DgsComponent
public class UserReferenceResolver {

    @DgsEntityFetcher(name = "UserReference")
    public Map<String, Object> resolveUserReference(Map<String, Object> values) {
        // Simply return the reference with the ID
        // The actual data will be fetched by the federation gateway from the user-service
        return values;
    }
}