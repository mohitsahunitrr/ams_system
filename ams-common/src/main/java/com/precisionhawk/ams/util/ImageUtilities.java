/*
 * All rights reserved.
 */

package com.precisionhawk.ams.util;

import com.precisionhawk.ams.bean.Dimension;
import com.precisionhawk.ams.bean.GeoPoint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.imageio.ImageIO;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Rotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public final class ImageUtilities {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtilities.class);
    
    public enum ImageType {
        GIF("image/gif", "GIF", new String[]{"GIF"}), JPG("image/jpeg", "JPG", new String[]{"JPG", "JPEG"}), PNG("image/png", "PNG", new String[]{"PNG"});
        
        private final String contentType;
        private final String[] extensions;
        private final String iOUtilsIdentifier;
        
        private ImageType(String contentType, String iOUtilsIdentifier, String[] extensions) {
            this.contentType = contentType;
            this.extensions = extensions;
            this.iOUtilsIdentifier = iOUtilsIdentifier;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public String getIOUtilsIdentifier() {
            return iOUtilsIdentifier;
        }
        
        public static ImageType fromContentType(String contentType) {
            for (ImageType it : ImageType.values()) {
                if (it.contentType.equals(contentType)) {
                    return it;
                }
            }
            return null;
        }
        
        public static ImageType fromExtension(String extension) {
            for (ImageType it : ImageType.values()) {
                for (String ext : it.extensions) {
                    if (ext.equalsIgnoreCase(extension)) {
                        return it;
                    }
                }
            }
            return null;
        }
        
        public String toExtension() {
            return extensions[0];
        }
    }
    
    // Do not instantiate this class.  Resistance is futile.
    private ImageUtilities() {}
    
    private static final double MIN_PERC = .25;
    
    @Deprecated
    public static void crop(String resourceId, InputStream inputStream, OutputStream outputStream, ImageType imageType, Point topLeft, Rectangle rect) throws IOException {
        BufferedImage src = ImageIO.read(inputStream);
        LOGGER.debug(
            "{}: Image width is {}, height is {}", resourceId, src.getWidth(), src.getHeight()
        );
        
        // Figure out percentages and keep the aspect ratio.  We also want to enforce a maximum zoom.
        Double zoomPerc;
        double heightPerc = (double)rect.height / (double)src.getHeight();
        double widthPerc = (double)rect.width / (double)src.getWidth();
        if (heightPerc > widthPerc) {
            zoomPerc = heightPerc;
        } else if (widthPerc > heightPerc) {
            zoomPerc = widthPerc;
        } else {
            zoomPerc = widthPerc;
        }
        if (zoomPerc < MIN_PERC) {
            // Don't zoom past minimum
            zoomPerc = MIN_PERC;
        }
        LOGGER.debug(
            "{}: Requested top left is ({}, {}) width is {}, height is {} width percent is {}, height percent is {}",
            new Object[]{resourceId, rect.x, rect.y, rect.width, rect.height, widthPerc, heightPerc}
        );
        
        // Figure out our new boundaries making sure we center on the old bounds as much as possible.
        int height = (int)(src.getHeight() * zoomPerc);
        int width = (int)(src.getWidth() * zoomPerc);
        // Calculate center of original
        int centerX = rect.x + (int)(rect.width/2);
        int centerY = rect.y + (int)(rect.height/2);
        // Adjust for new zoom ratio
        int minX = centerX - (int)(width/2);
        int minY = centerY - (int)(height/2);
        if (minX < 0) {
            minX = 0;
        } else if (minX + width > src.getWidth()) {
            minX = src.getWidth() - width;
        }
        if (minY < 0) {
            minY = 0;
        } else if (minY + height > src.getHeight()) {
            minY = src.getHeight() - height;
        }
        rect.height = height;
        rect.width = width;
        rect.x = minX;
        rect.y = minY;
        LOGGER.debug(
            "{}: Calculated top left is ({}, {}) width is {}, height is {}, scale percent is {}",
            new Object[]{resourceId, rect.x, rect.y, rect.width, rect.height, zoomPerc}
        );

        // Crop the image
        BufferedImage dest = src.getSubimage(minX, minY, width, height);
        ImageIO.write(dest, imageType.getIOUtilsIdentifier(), outputStream);
    }

    @Deprecated
    public static void scale(InputStream inputStream, OutputStream outputStream, ImageType imageType, int targetWidth, int targetHeight) throws IOException {
        BufferedImage bi = ImageIO.read(inputStream);
        bi = scale(bi, targetWidth, targetHeight);
        ImageIO.write(bi, imageType.getIOUtilsIdentifier(), outputStream);
    }
    
    @Deprecated
    public static BufferedImage scale(BufferedImage sbi, int targetWidth, int targetHeight) {
        double fHeight = targetHeight / sbi.getHeight();
        double fWidth = targetWidth / sbi.getWidth();
        BufferedImage dbi = new BufferedImage(targetWidth, targetHeight, sbi.getType());
        Graphics2D g = dbi.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
        g.drawRenderedImage(sbi, at);
        return dbi;
    }
    
    public static GeoPoint getLocation(TiffImageMetadata metadata) throws ImageReadException, IOException {
        if (metadata == null) {
            return null;
        }
        TiffImageMetadata.GPSInfo gpsInfo = metadata.getGPS();
        if (gpsInfo != null) {
            GeoPoint p = new GeoPoint();
            p.setLatitude(gpsInfo.getLatitudeAsDegreesNorth());
            p.setLongitude(gpsInfo.getLongitudeAsDegreesEast());
            TiffField field = metadata.findField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
            if (field != null) {
                p.setAltitude(field.getDoubleValue());
            }
            return p;
        } else {
            return null;
        }
    }

    public static Dimension getSize(ImageInfo iinfo) throws ImageReadException, IOException {
        Dimension d = new Dimension();
        d.setHeight(Double.valueOf(iinfo.getHeight()));
        d.setWidth(Double.valueOf(iinfo.getWidth()));
        return d;
    }
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSZ");
    private static final DateTimeFormatter DATE_TIME_FORMATTER2 = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
    private static final TagInfoAscii[] TIMESTAMP_FIELDS = {ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, TiffTagConstants.TIFF_TAG_DATE_TIME};
    public static ZonedDateTime getTimestamp(TiffImageMetadata metadata, ZoneId defaultZoneId) throws ImageReadException {
        if (metadata == null) {
            return null;
        }
        ZonedDateTime result = null;
        TiffField field;
        for (int i = 0; result == null && i < TIMESTAMP_FIELDS.length; i++) {
            field = metadata.findField(TIMESTAMP_FIELDS[i]);
            if (field != null) {
                String s = field.getStringValue();
                if (s != null && (!s.isEmpty())) {
                    try {
                        result = getTimestamp(s);
                    } catch (DateTimeParseException e) {
                        if (defaultZoneId != null) {
                            result = getTimestamp(s, defaultZoneId);
                        }
                    }
                    if (result == null) {
                        LOGGER.warn("Unable to parse the value {} as a timestamp.", s);
                    }
                }
            }
        }
        return result;
    }
    
    // Camera model is 0x010f
    private static final int CAMERA_MODEL = 0x010f;
    public static String getCameraMake(TiffImageMetadata metadata) throws ImageReadException {
        if (metadata == null) {
            return null;
        }
        // Camera make
        for (TiffField field : metadata.getAllFields()) {
            if (CAMERA_MODEL == field.getTag()) {
                return new String(field.getByteArrayValue()).trim();
//                return field.getStringValue();
            }
        }
        return null;
    }
    
    private static ZonedDateTime getTimestamp(String value) throws DateTimeParseException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(value, DATE_TIME_FORMATTER);
    }
    
    private static ZonedDateTime getTimestamp(String value, ZoneId zoneId) throws DateTimeParseException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        LocalDateTime ldt = LocalDateTime.parse(value, DATE_TIME_FORMATTER2);
        return ZonedDateTime.of(ldt, zoneId);
    }
    
    public static TiffImageMetadata retrieveExif(File file)
        throws IOException, ImageReadException
    {
        TiffImageMetadata exif;
        ImageMetadata metadata = Imaging.getMetadata(file);
        if (metadata instanceof JpegImageMetadata) {
            exif = ((JpegImageMetadata)metadata).getExif();
        } else if (metadata instanceof TiffImageMetadata) {
            exif = (TiffImageMetadata)metadata;
        } else {
            exif = null;
        }
        return exif;
    }
    
    public static File rotateIfNecessary(TiffImageMetadata metadata, File infile, ImageType type) throws ImageReadException, IOException {
        // First, try to find the orientation field
        Short orientation = readImageOrientation(metadata);
        if (orientation == null) {
            return infile;
        }
        BufferedImage i = null;
        switch (orientation) {
            case TiffTagConstants.ORIENTATION_VALUE_HORIZONTAL_NORMAL:
                // Standard orientation.
                break;
            case TiffTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL:
                i = ImageIO.read(infile);
                i = Scalr.rotate(i, Rotation.FLIP_HORZ);
                break;
            case TiffTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_270_CW:
                i = ImageIO.read(infile);
                i = Scalr.rotate(i, Rotation.FLIP_HORZ);
                i = Scalr.rotate(i, Rotation.CW_270);
                break;
            case TiffTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_90_CW:
                i = ImageIO.read(infile);
                i = Scalr.rotate(i, Rotation.FLIP_HORZ);
                i = Scalr.rotate(i, Rotation.CW_90);
                break;
            case TiffTagConstants.ORIENTATION_VALUE_MIRROR_VERTICAL:
                i = ImageIO.read(infile);
                i = Scalr.rotate(i, Rotation.FLIP_HORZ);
                break;
            case TiffTagConstants.ORIENTATION_VALUE_ROTATE_90_CW:
                i = ImageIO.read(infile);
                i = Scalr.rotate(i, Rotation.CW_90);
                break;
            case TiffTagConstants.ORIENTATION_VALUE_ROTATE_180:
                i = ImageIO.read(infile);
                i = Scalr.rotate(i, Rotation.CW_180);
                break;
            case TiffTagConstants.ORIENTATION_VALUE_ROTATE_270_CW:
                i = ImageIO.read(infile);
                i = Scalr.rotate(i, Rotation.CW_270);
                break;
            default:
                LOGGER.warn("Unhandled orientation {}", orientation);
        }
        File outfile;
        if (i == null) {
            outfile = infile;
        } else {
            outfile = File.createTempFile("ams", "." + type.toExtension());
            OutputStream os = null;
            try {
                os = new FileOutputStream(outfile);
                String outtype = type == ImageType.JPG ? "JPEG" : type.name();
                ImageIO.write(i, outtype, os);
            } finally {
                IOUtils.closeQuietly(os);
            }
        }
        return outfile;
    }
    
    public static Short readImageOrientation(TiffImageMetadata exif)
        throws ImageReadException
    {
        if (exif == null) {
            return null;
        }
        Object o = exif.getFieldValue(TiffTagConstants.TIFF_TAG_ORIENTATION);
        Short orientation;
        if (o == null) {
            orientation = null;
        } else if (o instanceof Short) {
            orientation = (Short)o;
        } else {
            orientation = Short.valueOf(o.toString());
        }
        return orientation;
    }
}
