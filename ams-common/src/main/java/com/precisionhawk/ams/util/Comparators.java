package com.precisionhawk.ams.util;

import com.precisionhawk.ams.domain.ResourceMetadata;
import java.time.ZonedDateTime;
import java.util.Comparator;

/**
 *
 * @author pchapman
 */
public interface Comparators {
    public static Comparator<ResourceMetadata> RESOURCE_BY_TIMESTAMP = new Comparator<ResourceMetadata>() {
        @Override
        public int compare(ResourceMetadata o1, ResourceMetadata o2) {
            ZonedDateTime t1 = o1 == null ? null : o1.getTimestamp();
            ZonedDateTime t2 = o2 == null ? null : o2.getTimestamp();
            if (t1 == null) {
                if (t2 == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (t2 == null) {
                return -1;
            } else {
                return t1.compareTo(t2);
            }
        }
    };
}
