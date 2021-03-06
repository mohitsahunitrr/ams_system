package com.precisionhawk.ams.webservices.impl;

import com.precisionhawk.ams.bean.Dimension;
import com.precisionhawk.ams.bean.ImageScaleRequest;
import com.precisionhawk.ams.bean.ResourceSearchParams;
import com.precisionhawk.ams.bean.security.ServicesSessionBean;
import com.precisionhawk.ams.dao.ResourceMetadataDao;
import com.precisionhawk.ams.domain.ResourceMetadata;
import com.precisionhawk.ams.domain.ResourceStatus;
import com.precisionhawk.ams.config.ServicesConfig;
import com.precisionhawk.ams.dao.DaoException;
import com.precisionhawk.ams.repository.RepositoryException;
import com.precisionhawk.ams.repository.ResourceRepository;
import com.precisionhawk.ams.util.CollectionsUtilities;
import com.precisionhawk.ams.util.ImageUtilities;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import com.precisionhawk.ams.webservices.ResourceWebService;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;

/**
 *
 * @author Philip A. Chapman
 */
@Named
public class ResourceWebServiceImpl extends AbstractWebService implements ResourceWebService {

    @Inject protected ResourceMetadataDao resourceDao;
    @Inject protected ResourceRepository repo;
    @Inject protected ServicesConfig config;
    
    @Override
    public void delete(String authToken, String resourceId) {
        ServicesSessionBean sess = lookupSessionBean(authToken);
        ensureExists(resourceId, "The resource ID is required.");
        try {
            ResourceMetadata rmeta = resourceDao.retrieve(resourceId);
            if (rmeta == null) {
                ResourceSearchParams params = new ResourceSearchParams();
                params.setZoomifyId(resourceId);
                rmeta = CollectionsUtilities.firstItemIn(resourceDao.search(params));
                if (rmeta == null) {
                    throw new NotFoundException(String.format("The resource %s was not found.", resourceId));
                } else {
                    // This is a zoomify file.  Make sure user has authorization to do the delte.
                    authorize(sess, rmeta);
                }
                // This is a zoomify file, remove it from the repository, below.
            } else {
                authorize(sess, rmeta);
                resourceDao.delete(resourceId);
            }
            repo.deleteResource(resourceId);
        } catch (DaoException | RepositoryException ex) {
            throw new InternalServerErrorException(String.format("Error deleting resource %s", resourceId));
        }
    }

    @Override
    public ResourceMetadata retrieve(String authToken, String resourceId) {
        ServicesSessionBean sess = lookupSessionBean(authToken);
        ensureExists(resourceId, "The resource ID is required.");
        try {
            return authorize(sess, validateFound(resourceDao.retrieve(resourceId)));
        } catch (DaoException ex) {
            throw new InternalServerErrorException(String.format("Error retrieving resource %s", resourceId));
        }
    }

    @Override
    public List<ResourceMetadata> search(String authToken, ResourceSearchParams params) {
        ServicesSessionBean sess = lookupSessionBean(authToken);
        return _search(sess, params);
    }
    public List<ResourceMetadata> _search(ServicesSessionBean sess, ResourceSearchParams params) {
        ensureExists(params, "The search parameters are required.");
        authorize(sess, params);
        try {
            return authorize(sess, resourceDao.search(params));
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error retrieving resources by search parameters.");
        }
    }

    @Override
    public ResourceMetadata scale(String authToken, String resourceId, ImageScaleRequest scaleRequest) {
        ServicesSessionBean sess = lookupSessionBean(authToken);
        ensureExists(resourceId, "The resource ID is required.");
        ensureExists(scaleRequest, "The image scale request is required.");
        try {
            ResourceMetadata rmeta = resourceDao.retrieve(resourceId);
            if (rmeta == null) {
                throw new NotFoundException(String.format("No image %s found.", resourceId));
            } else if (!rmeta.getContentType().startsWith("image/")) {
                throw new BadRequestException(String.format("The resource %s is not an image.", resourceId));
            }
            authorize(sess, rmeta);
            return createScaledImageFromOriginal(rmeta, scaleRequest);
        } catch (DaoException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Map<String, Boolean> verifyUploadedResources(String authToken, List<String> resourceIDs) {
        ensureExists(resourceIDs, "The resource IDs are required.");
        return repo.verifyExistance(resourceIDs);
    }

    @Override
    public ResourceMetadata insertResourceMetadata(String authToken, ResourceMetadata rmeta) {
        ServicesSessionBean sess = lookupSessionBean(authToken);
        ensureExists(rmeta, "The resource metadata is required.");
        authorize(sess, rmeta);
        if (rmeta.getResourceId() == null) {
            rmeta.setResourceId(UUID.randomUUID().toString());
        }
        try {
            if (resourceDao.insert(rmeta)) {
                LOGGER.debug("Resource {} has been inserted.", rmeta.getResourceId());
                return rmeta;
            } else {
                throw new BadRequestException(String.format("Metadata for resource %s already exists.", rmeta.getResourceId()));
            }
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error persisting pole.", ex);
        }
    }

    @Override
    public ResourceMetadata updateResourceMetadata(String authToken, ResourceMetadata rmeta) {
        ServicesSessionBean sess = lookupSessionBean(authToken);
        ensureExists(rmeta, "The resource metadata is required.");
        ensureExists(rmeta.getResourceId(), "The resource ID is required.");
        authorize(sess, rmeta);
        try {
            ResourceMetadata rm = resourceDao.retrieve(rmeta.getResourceId());
            boolean updated = false;
            if (rm != null) {
                authorize(sess, rm);
                updated = resourceDao.update(rmeta);
            }
            
            if (updated) {
                LOGGER.debug("Resource {} has been updated.", rmeta.getResourceId());
                return rmeta;
            } else {
                throw new NotFoundException(String.format("No metadata for resource %s exists.", rmeta.getResourceId()));
            }
        } catch (DaoException ex) {
            throw new InternalServerErrorException("Error persisting pole.", ex);
        }
    }

    @Override
    public Response downloadResource(String resourceId) {
        ensureExists(resourceId, "The resource IDs are required.");
        try {
            boolean isZoomify = false;
            ResourceMetadata rmeta = resourceDao.retrieve(resourceId);
            if (rmeta == null) {
                // This may be zoomify resource.
                ResourceSearchParams params = new ResourceSearchParams();
                params.setZoomifyId(resourceId);
                rmeta = CollectionsUtilities.firstItemIn(resourceDao.search(params));
                if (rmeta == null) {
                    throw new NotFoundException(String.format("No resource with ID %s exists.", resourceId));
                } else {
                    isZoomify = true;
                }
            }
            // If we reached here, resourceId is a valid resource or zoomify ID.
            URL redirect = repo.retrieveURL(resourceId);
            if (redirect == null) {
                return provideResource(rmeta, isZoomify);
            } else {
                return Response.status(302).header("location", redirect).build();
                // The below returns a 307
//                return Response.temporaryRedirect(redirect.toURI()).build();
            }
        } catch (DaoException | RepositoryException ex) { // | URISyntaxException ex) {
            throw new InternalServerErrorException(String.format("Error retrieving resource %s", resourceId));
        }
    }
    
    private Response provideResource(final ResourceMetadata rmeta, final boolean isZoomify) {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, InternalServerErrorException {
                InputStream is = null;
                String key = isZoomify ? rmeta.getZoomifyId() : rmeta.getResourceId();
                try {
                    is = repo.retrieveResource(key);
                    IOUtils.copy(is, output);
                } catch (RepositoryException ex) {
                    LOGGER.error("Error retrieving resource {}", key, ex);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        };
        String contentType;
        String fileNameHeader;
        if (isZoomify) {
            contentType = "image/zif";
            fileNameHeader = String.format("attachment; filename=\"%s.zif\"", rmeta.getZoomifyId());
        } else {
            contentType = rmeta.getContentType();
            fileNameHeader = String.format("attachment; filename=\"%s\"", rmeta.getName());
        }
        return Response.ok(stream, contentType).header("content-disposition", fileNameHeader).build();
    }

    @Override
    public void uploadResource(String authToken, String resourceId, HttpServletRequest req) {
        ServicesSessionBean sess = lookupSessionBean(authToken);
        ensureExists(resourceId, "Resource ID is required.");
        try {
            String contentType;
            String name;
            ResourceMetadata meta = resourceDao.retrieve(resourceId);
            if (meta == null) {
                // It may be a zoomify image.
                ResourceSearchParams rparms = new ResourceSearchParams();
                rparms.setZoomifyId(resourceId);
                //FIXME: Remove this
                if (resourceId.equals("af6e54f6-8b57-480e-9d8b-f4c4186da60d")) {
                    meta = resourceDao.retrieve("cd6847aa-4e09-4230-9daf-f8a175c25973");
                } else {
                    meta = CollectionsUtilities.firstItemIn(resourceDao.search(rparms));
                }
                if (meta == null) {
                    
                    LOGGER.debug("No metadata for resource {}, upload aborted.", resourceId);
                    throw new NotFoundException(String.format("No metadata for resource %s found.  Data cannot be uploaded.", resourceId));
                } else {
                    // Zoomify image
                    authorize(sess, meta);
                    contentType = "image/zif";
                    name = resourceId + ".zif";
                }
            } else {
                authorize(sess, meta);
                contentType = meta.getContentType();
                name = meta.getName();
            }
                    
            if (ServletFileUpload.isMultipartContent(req)) {
                LOGGER.debug("Data being uploaded for resource {}", resourceId);
                
                // Configure a repository (to ensure a secure temp location is used)
                ServletContext servletContext = req.getSession().getServletContext();
                File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
                DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
                fileItemFactory.setRepository(repository);

                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(fileItemFactory);

                // Parse the request
                List<FileItem> items = upload.parseRequest(req);
                if (items.size() != 1) {
                    LOGGER.error("Multiple parts uploaded for resource {}", resourceId);
                    throw new BadRequestException("It is expected that exactly 1 file will be uploaded");
                } else {
                    FileItem fileitem = items.get(0);
                    // Update metadata
                    InputStream is = null;
                    try {
                        is = fileitem.getInputStream();
                        repo.storeResource(meta, resourceId, name, contentType, is, null);
                        LOGGER.debug("Data for resource {} stored", resourceId);
                        LOGGER.debug("Content type {} and name {} for resource {} stored", contentType, name, resourceId);
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException ioe) {}
                        }
                    }
                }
            } else {
                repo.storeResource(meta, resourceId, name, contentType, req.getInputStream(), null);
            }
        } catch (DaoException | RepositoryException | IOException | FileUploadException ex) {
            throw new InternalServerErrorException(String.format("Unable to store resource %s", resourceId));
        }
    }
    
    private ResourceMetadata createScaledImageFromOriginal(ResourceMetadata originalMD, ImageScaleRequest scaleRequest)
//        throws ServiceException
    {
        ResourceMetadata destMD = null;
        try {
            if (ImageUtilities.ImageType.fromContentType(originalMD.getContentType()) != null)
            {
                byte[] bytes;
                InputStream is = null;
                OutputStream os = null;
                File tmpFile = null;
                try {
                    // Load the image
                    is = repo.retrieveResource(originalMD.getResourceId());
                    if (is == null) {
                        throw new NotFoundException(String.format("The image %s does not exist in the repository.", originalMD.getResourceId()));
                    }
                    tmpFile = File.createTempFile("windams", "image");
                    os = new BufferedOutputStream(new FileOutputStream(tmpFile));
                    IOUtils.copy(is, os);
                    is.close();
                    is = null;
                    os.close();
                    os = null;
                    Scalr.Mode mode = null;
                    if (null != scaleRequest.getScaleOperation()) {
                        switch (scaleRequest.getScaleOperation()) {
                            case ScaleToFit:
                                mode = Scalr.Mode.AUTOMATIC;
                                break;
                            case ScaleToHeight:
                                mode = Scalr.Mode.FIT_TO_HEIGHT;
                                break;
                            case ScaleToSize:
                                mode = Scalr.Mode.FIT_EXACT;
                                break;
                            case ScaleToWidth:
                                mode = Scalr.Mode.FIT_TO_WIDTH;
                                break;
                            default:
                                break;
                        }
                    }
                    if (mode == null) {
                        throw new BadRequestException("Invalid scale operation.");
                    }
                    BufferedImage srcImage = ImageIO.read(tmpFile); // Load image
                    BufferedImage scaledImage = Scalr.resize(srcImage, Scalr.Method.AUTOMATIC, mode, scaleRequest.getWidth().intValue(), scaleRequest.getHeight().intValue());
                    os = new ByteArrayOutputStream();
                    ImageIO.write(scaledImage, scaleRequest.getResultType().name(), os);
                    os.close();
                    bytes = ((ByteArrayOutputStream)os).toByteArray();
                    os = null;
                    destMD = new ResourceMetadata();
                    destMD.setAssetId(originalMD.getAssetId());
                    destMD.setAssetInspectionId(originalMD.getAssetInspectionId());
                    destMD.setOrderNumber(originalMD.getOrderNumber());
                    destMD.setSiteId(originalMD.getSiteId());
                    destMD.setSiteInspectionId(originalMD.getSiteInspectionId());
                    destMD.setContentType(ImageUtilities.ImageType.fromExtension(scaleRequest.getResultType().name()).getContentType());
                    destMD.setLocation(originalMD.getLocation());
                    destMD.setName(originalMD.getName());
                    destMD.setResourceId(UUID.randomUUID().toString());
                    destMD.setSize(new Dimension(Double.valueOf(scaledImage.getWidth()), Double.valueOf(scaledImage.getHeight())));
                    destMD.setSourceResourceId(originalMD.getResourceId());
                    destMD.setStatus(ResourceStatus.Released);
                    destMD.setTimestamp(originalMD.getTimestamp());
                    destMD.setType(scaleRequest.getResourceType());
                    resourceDao.insert(destMD);
                    repo.storeResource(destMD, destMD.getResourceId(), destMD.getName(), destMD.getContentType(), new ByteArrayInputStream(bytes), Long.valueOf(bytes.length));
                } catch (DaoException daoe) {
                    throw new InternalServerErrorException("Error saving scaled image", daoe);
                } catch (IOException ioe) {
                    throw new InternalServerErrorException(String.format("Error scaling the resource %s.", originalMD.getResourceId()), ioe);
                } finally {
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(os);
                    if (tmpFile != null && tmpFile.exists()) {
                        tmpFile.delete();
                    }
                }
            } else {
                throw new InternalServerErrorException(String.format("Resource with mime type %s is not supported for scaling.", originalMD.getContentType()));
            }
        } catch (RepositoryException re) {
            throw new InternalServerErrorException(String.format("Error retrieving the resource %s.", originalMD.getResourceId()), re);
        }
        return destMD;
    }
    
    private static final String DOWNLOAD_PATH = "%s/resource/%s/download";
    
    protected String getResourceDownloadURL(String resourceId, boolean isZoomify) {
        if (resourceId == null || resourceId.isEmpty()) {
            return null;
        }
        String url = null;
        if (isZoomify) {
            // Try to go directly to source.  This is necessary for the S3 repo.
            try {
                url = repo.retrieveURL(resourceId).toExternalForm();
            } catch (RepositoryException ex) {
                LOGGER.error("Error determining direct access URL for resource {}", resourceId, ex);
            }
        }
        if (url == null) {
            url = String.format(DOWNLOAD_PATH, config.getServicesURL(), resourceId);
        }
        return url;
    }
}
