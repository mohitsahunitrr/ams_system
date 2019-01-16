package com.precisionhawk.ams.support.aws;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import javax.inject.Provider;

/**
 * A factory for AWS SNS client.
 *
 * @author pchapman
 */
public class SNSClientFactory extends AwsClientFactory implements Provider<AmazonSNS> {
    
    private static final Object LOCK = new Object();

    private AmazonSNS client;
    
    @Override
    public AmazonSNS get() {
        synchronized (LOCK) {
            if (client == null) {
                LOGGER.info("Instantiating S3 client to for region %s.", getRegion());
                AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard();
                builder.setCredentials(createCredentials());
                if (getRegion() != null && getRegion().length() > 0) {
                    builder.setRegion(getRegion());
                }
                client = builder.build();
            }
        }
        return client;
    }
}
