package org.custommonkey.xmlunit.diff;

import java.io.Reader;

import net.sf.xmlunit.diff.DifferenceEngineFactory;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.w3c.dom.Document;


public class Diffs {

    protected static Diff prepareDiff(XmlUnitProperties properties, Document control, Document test)
            throws Exception {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    protected static Diff prepareDiff(XmlUnitProperties properties, String control, String test)
            throws Exception {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    protected static Diff prepareDiff(XmlUnitProperties properties, Reader control, Reader test)
            throws Exception {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    protected static Diff prepareDiff(
            XmlUnitProperties properties,
            String control, String test,
            DifferenceEngineFactory engineFactory)
            throws Exception {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .usingDifferenceEngineFactory(engineFactory)
                .build();
    }
}
