package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.domain.ResourceMetadata;
import com.precisionhawk.ams.bean.ResourceSearchParams;
import java.util.List;

/**
 * Storage for resource metadata.
 *
 * @author Philip A. Chapman
 */
public interface ResourceMetadataDao {
    
    /**
     * Retrieves the metadata associated with a resource by the resource's
     * unique key.
     * 
     * @param resourceId The unique ID of the resource.
     * @return The resource's metadata.
     * @throws DaoException Indicates an irrecoverable error.
     */
    ResourceMetadata retrieve(String resourceId) throws DaoException;
    
    /**
     * Searches for Resource Metadata that fits all given criteria.  At least one parameter must be non-null.
     * @param params The bean holding filter criteria for the request.
     * @return A list of all matching resource metadata.
     * @throws DaoException Indicates an irrecoverable error.
     */
    List<ResourceMetadata> search(ResourceSearchParams params) throws DaoException;
    
    boolean insert(ResourceMetadata meta) throws DaoException;

    boolean delete(String id) throws DaoException;
    
    boolean update(ResourceMetadata meta) throws DaoException;
}
