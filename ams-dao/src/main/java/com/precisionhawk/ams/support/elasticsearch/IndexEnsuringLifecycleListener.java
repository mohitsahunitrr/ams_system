package com.precisionhawk.ams.support.elasticsearch;

import org.elasticsearch.client.Client;
import us.pcsw.es.util.ClientLifecycleListener;
import us.pcsw.es.util.ESUtils;

/**
 *
 * @author Philip A. Chapman
 */
public abstract class IndexEnsuringLifecycleListener implements ClientLifecycleListener {

    @Override
    public void clientConnectionOpened(Client client) {
        for (String index : getIndexes()) {
            ESUtils.prepareIndex(client, index, null);
        }
    }

    @Override
    public void prepareForClose(Client arg0) {}

    @Override
    public void afterClose() {}
    
    public abstract String[] getIndexes();
}
