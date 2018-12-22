package com.precisionhawk.ams.service.oauth;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A JWKSource which gathers JWK from other sources and includes them in a larger
 * list.
 *
 * @author pchapman
 */
public class DelegatingJWKSource implements JWKSource {
    private final Set<JWKSource> delegates = new HashSet<>();
    
    public void add(JWKSource source) {
        delegates.add(source);
    }
    public void addAll(Collection<JWKSource> sources) {
        delegates.addAll(sources);
    }
    public void remove(JWKSource source) {
        delegates.remove(source);
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) throws KeySourceException {
        List<JWK> keys = new LinkedList<>();
        for (JWKSource delegate : delegates) {
            keys.addAll(delegate.get(jwkSelector, context));
        }
        return keys;
    }
}
