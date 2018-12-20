package com.precisionhawk.ams.domain;

/**
 *
 * @author pchapman
 */
public class InspectionEventSource extends ExtendableEnum {
    
    public InspectionEventSource(String value) {
        super(value);
    }
    
    public static final InspectionEventSource AI = new InspectionEventSource("AI");
    public static final InspectionEventSource Analyst = new InspectionEventSource("Analyst");
}
