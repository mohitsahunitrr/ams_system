package com.precisionhawk.ams.service.aws;

import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.AmazonSNS;
import com.precisionhawk.ams.config.NotificationServicesConfig;
import com.precisionhawk.ams.config.ServicesConfig;
import com.precisionhawk.ams.service.NotificationService;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pchapman
 */
@Named
public class SNSNotificationService implements NotificationService {
    
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Inject
    private NotificationServicesConfig notificationsConfig;
    @Inject
    private ServicesConfig servicesConfig;
    @Inject
    private AmazonSNS snsClient;

    @PostConstruct
    public void postInit() {
        publishCriticalMessage("SNS Notification Service has started.");
    }

    @Override
    public void publishCriticalMessage(String message) {
        String criticalTopic = notificationsConfig.getCriticalNotificationsTopic();
        if (criticalTopic != null && !criticalTopic.isEmpty()) {
            String msg = String.format("CRITICAL MESSAGE FROM Application %s, Environment %s, Version %s: %s", servicesConfig.getAppName(), servicesConfig.getEnvironment(), servicesConfig.getVersion(), message);
            try {
                PublishRequest publishRequest = new PublishRequest(criticalTopic, msg);
                PublishResult publishResult = snsClient.publish(publishRequest);
                LOGGER.debug("SMS Message {}: {} sent to topic {}", publishResult.getMessageId(), msg, criticalTopic);
            } catch (Throwable t) {
                LOGGER.debug("Error publishing SMS Message {} sent to topic {}", msg, criticalTopic, t);
            }
        }
    }
}
