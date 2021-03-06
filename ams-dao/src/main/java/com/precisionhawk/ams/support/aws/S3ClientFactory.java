package com.precisionhawk.ams.support.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import javax.inject.Named;
import javax.inject.Provider;

/**
 *
 * @author Philip A. Chapman
 */
@Named
public class S3ClientFactory extends AwsClientFactory implements Provider<AmazonS3> {
    
    private static final Object LOCK = new Object();

    private AmazonS3 client;
    
    @Override
    public AmazonS3 get() {
        synchronized (LOCK) {
            if (client == null) {
                LOGGER.info("Instantiating S3 client to for region %s.", getRegion());
                AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
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
