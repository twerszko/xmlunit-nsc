package org.custommonkey.xmlunit.diff;

import static org.custommonkey.xmlunit.diff.Diff.newDiff;

import java.io.Reader;

import net.sf.xmlunit.diff.DifferenceEngineFactory;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.w3c.dom.Document;


public class Diffs {

    public static Diff prepareDiff(XmlUnitProperties properties, Document control, Document test)
            throws Exception {
        return newDiff(properties).betweenControlDocument(control).andTestDocument(test).build();
    }

    public static Diff prepareDiff(XmlUnitProperties properties, String control, String test)
            throws Exception {
        return newDiff(properties).betweenControlDocument(control).andTestDocument(test).build();
    }

    public static Diff prepareDiff(XmlUnitProperties properties, Reader control, Reader test)
            throws Exception {
        return newDiff(properties).betweenControlDocument(control).andTestDocument(test).build();
    }

    public static Diff prepareDiff(XmlUnitProperties properties, String control, String test,
            DifferenceEngineFactory engineFactory)
            throws Exception {
        return newDiff(properties).betweenControlDocument(control).andTestDocument(test)
                .usingDifferenceEngineFactory(engineFactory).build();
    }

    public static DetailedDiff prepareDetailedDiff(XmlUnitProperties properties, String control, String test)
            throws Exception {
        return new DetailedDiff(prepareDiff(properties, control, test));
    }

    public static DetailedDiff prepareDeatiledDiff(XmlUnitProperties properties, Reader control, Reader test)
            throws Exception {
        return new DetailedDiff(prepareDiff(properties, control, test));
    }
}
