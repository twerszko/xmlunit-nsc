package org.xmlunit.examples;

import static org.fest.assertions.api.Assertions.assertThat;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DifferenceEngineFactory;
import net.sf.xmlunit.diff.Evaluators.DefaultEvaluator;

import org.custommonkey.xmlunit.XmlUnit;
import org.custommonkey.xmlunit.XmlUnitBuilder;
import org.custommonkey.xmlunit.diff.Diff;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

public class OverridingEvaluator {

    private XmlUnit xmlUnit;

    @Before
    public void setup() {
        xmlUnit = XmlUnitBuilder.xmlUnit().build();
    }

    @Test
    public void should_be_similar_with_default_evaluator() throws Exception {
        // given
        String expected = "<message>Hello World</message>";
        String actual = "<message><![CDATA[Hello World]]></message>";

        // when
        Diff diff = xmlUnit.newDiff()
                .betweenControlDocument(expected)
                .andTestDocument(actual)
                .build();

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_be_identical_with_custom_evaluator() throws Exception {
        // given
        String expected = "<message>Hello World</message>";
        String actual = "<message><![CDATA[Hello World]]></message>";

        // when
        DifferenceEngineFactory factory = xmlUnit.newDifferenceEngineFactory();
        factory.useEvaluator(new CustomEvaluator());

        Diff diff = xmlUnit.newDiff()
                .betweenControlDocument(expected)
                .andTestDocument(actual)
                .usingDifferenceEngineFactory(factory)
                .build();

        // then
        assertThat(diff.identical()).isTrue();
        assertThat(diff.similar()).isTrue();
    }

    private class CustomEvaluator extends DefaultEvaluator {
        @Override
        public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
            ComparisonType type = comparison.getType();
            if (type == ComparisonType.NODE_TYPE) {
                Node control = comparison.getControlDetails().getTarget();
                Node test = comparison.getTestDetails().getTarget();
                if (isText(control) && isText(test)) {
                    return ComparisonResult.EQUAL;
                }
            }
            return super.evaluate(comparison, outcome);
        }

        private boolean isText(Node node) {
            if (node == null)
                return false;
            short type = node.getNodeType();
            if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE)
                return true;
            return false;
        }
    }
}
