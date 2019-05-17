package com.precisionhawk.ams.util;

import com.precisionhawk.ams.domain.Organization;
import com.precisionhawk.ams.domain.ResourceMetadata;
import com.precisionhawk.ams.domain.Site;
import com.precisionhawk.ams.domain.WorkOrder;
import java.time.ZonedDateTime;
import java.util.Comparator;

/**
 *
 * @author pchapman
 */
public interface Comparators {
    
    public static Comparator<Organization>  ORGS_COMPARATOR = new Comparator<Organization>() {
        @Override
        public int compare(Organization o1, Organization o2) {
            String n1 = o1 == null ? null : o1.getName();
            String n2 = o2 == null ? null : o2.getName();
            if (n1 == null) {
                if (n2 == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (n2 == null) {
                return -1;
            } else {
                return n1.compareTo(n2);
            }
        }
    };

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
    
    public static Comparator<WorkOrder>  WORK_ORDERS_COMPARATOR = new Comparator<WorkOrder>() {
        @Override
        public int compare(WorkOrder wo1, WorkOrder wo2) {
            String n1 = wo1 == null ? null : wo1.getOrderNumber();
            String n2 = wo2 == null ? null : wo2.getOrderNumber();
            if (n1 == null) {
                if (n2 == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (n2 == null) {
                return -1;
            } else {
                return n1.compareTo(n2);
            }
        }
    };
}
