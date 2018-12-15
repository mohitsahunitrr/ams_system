package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.webservices.UtilsWebService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Named;
import javax.ws.rs.BadRequestException;

/**
 *
 * @author pchapman
 */
@Named
public class UtilsWebServiceImpl extends AbstractWebService implements UtilsWebService {

    @Override
    public List<String> retrieve(Integer count) {
        ensureExists(count, "The count of UUIDs to generate is required");
        if (count < 1) {
            throw new BadRequestException("The count of UUIDs to generate cannot be less than 1");
        } else if (count > 50) {
            throw new BadRequestException("The count of UUIDs to generate cannot be greater than 50");
        }
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(UUID.randomUUID().toString());
        }
        return list;
    }
    
}
