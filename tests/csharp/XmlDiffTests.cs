﻿namespace XmlUnit.Tests {
    using XmlUnit;
    using NUnit.Framework;
    using System.IO;
    
    [TestFixture]
    public class XmlDiffTests {
        private XmlDiff _xmlDiff;
          
        [Test] public void EqualResultForSameReader() { 
            TextReader reader = new StringReader("<empty/>");
            DiffResult result = PerformDiff(reader, reader);
            Assertion.AssertEquals(true, result.Equal);
        }
        
        [Test] public void SameResultForTwoInvocations() {
            TextReader reader = new StringReader("<empty/>");
            DiffResult result1 = PerformDiff(reader, reader);
            DiffResult result2 = _xmlDiff.Compare();
            Assertion.AssertSame(result1, result2);
            
        }
        
        private DiffResult PerformDiff(TextReader reader1, TextReader reader2) {            
            _xmlDiff = new XmlDiff(reader1, reader2);
            DiffResult result = _xmlDiff.Compare();
            return result;
        }
        
        [Test] public void EqualResultForSameEmptyElements() { 
            string[] input1 = {"<empty/>" , "<empty></empty>", "<empty/>" };
            string[] input2 = {"<empty/>" , "<empty></empty>", "<empty></empty>"};
            for (int i=0; i < input1.Length; ++i) {
                AssertExpectedResult(input1[i], input2[i], true);
                AssertExpectedResult(input2[i], input1[i], true);
                
                AssertExpectedResult(input1[i], input1[i], true);
                AssertExpectedResult(input2[i], input2[i], true);
            }
        }

        [Test] public void NotEqualResultForEmptyVsNotEmptyElements() { 
            string[] input1 = {"<empty/>" , "<empty></empty>", "<empty><empty/></empty>"};
            string[] input2 = {"<empty>text</empty>", "<empty>text</empty>", "<empty>text</empty>"};
            for (int i=0; i < input1.Length; ++i) {
                AssertExpectedResult(input1[i], input2[i], false);
                AssertExpectedResult(input2[i], input1[i], false);
                
                AssertExpectedResult(input1[i], input1[i], true);
                AssertExpectedResult(input2[i], input2[i], true);
            }
        }
        
        [Test] public void NotEqualResultForDifferentElements() { 
            string[] input1 = {"<a><b/></a>" , "<a><b/></a>", "<a><b/></a>"};
            string[] input2 = {"<b><a/></b>", "<a><c/></a>", "<a><b><c/></b></a>"};
            for (int i=0; i < input1.Length; ++i) {
                AssertExpectedResult(input1[i], input2[i], false);
                AssertExpectedResult(input2[i], input1[i], false);
                
                AssertExpectedResult(input1[i], input1[i], true);
                AssertExpectedResult(input2[i], input2[i], true);
            }
        }
        
        [Test] public void NotEqualResultForDifferentNumberOfAttributes() { 
            string[] input1 = {"<a><b x=\"1\"/></a>", "<a><b x=\"1\"/></a>"};
            string[] input2 = {"<a><b/></a>", "<a><b x=\"1\" y=\"2\"/></a>"};
            for (int i=0; i < input1.Length; ++i) {
                AssertExpectedResult(input1[i], input2[i], false);
                AssertExpectedResult(input2[i], input1[i], false);
                
                AssertExpectedResult(input1[i], input1[i], true);
                AssertExpectedResult(input2[i], input2[i], true);
            }
        }
        
        [Test] public void NotEqualResultForDifferentAttributeValues() { 
            string[] input1 = {"<a><b x=\"1\"/></a>", "<a><b x=\"1\" y=\"2\"/></a>"};
            string[] input2 = {"<a><b x=\"2\"/></a>", "<a><b x=\"1\" y=\"3\"/></a>"};
            for (int i=0; i < input1.Length; ++i) {
                AssertExpectedResult(input1[i], input2[i], false);
                AssertExpectedResult(input2[i], input1[i], false);
                
                AssertExpectedResult(input1[i], input1[i], true);
                AssertExpectedResult(input2[i], input2[i], true);
            }
        }
        
        [Test] public void NotEqualResultForDifferentAttributeNames() { 
            string[] input1 = {"<a><b x=\"1\"/></a>", "<a><b x=\"1\" y=\"2\"/></a>"};
            string[] input2 = {"<a><b y=\"2\"/></a>", "<a><b x=\"1\" z=\"3\"/></a>"};
            for (int i=0; i < input1.Length; ++i) {
                AssertExpectedResult(input1[i], input2[i], false);
                AssertExpectedResult(input2[i], input1[i], false);
                
                AssertExpectedResult(input1[i], input1[i], true);
                AssertExpectedResult(input2[i], input2[i], true);
            }
        }
        
        [Test] public void EqualResultForDifferentAttributeSequences() { 
            string[] input1 = {"<a x=\"1\" y=\"2\" z=\"3\"/>", 
                                "<a><b x=\"1\" y=\"2\"/></a>"};
            string[] input2 = {"<a y=\"2\" z=\"3\" x=\"1\"/>", 
                                "<a><b y=\"2\" x=\"1\"/></a>"};
            for (int i=0; i < input1.Length; ++i) {
                AssertExpectedResult(input1[i], input2[i], true);
                AssertExpectedResult(input2[i], input1[i], true);
                
                AssertExpectedResult(input1[i], input1[i], true);
                AssertExpectedResult(input2[i], input2[i], true);
            }
        }
        
        [Test] public void NotEqualResultForDifferentAttributeValuesAndSequences() { 
            string[] input1 = {"<a x=\"1\" y=\"2\" z=\"3\"/>", 
                                "<a><b x=\"1\" y=\"2\"/></a>"};
            string[] input2 = {"<a y=\"2\" z=\"3\" x=\"2\"/>", 
                                "<a><b y=\"1\" x=\"1\"/></a>"};
            for (int i=0; i < input1.Length; ++i) {
                AssertExpectedResult(input1[i], input2[i], false);
                AssertExpectedResult(input2[i], input1[i], false);
                
                AssertExpectedResult(input1[i], input1[i], true);
                AssertExpectedResult(input2[i], input2[i], true);
            }
        }
        
        private void AssertExpectedResult(string input1, string input2, bool expected) {
            TextReader reader1 = new StringReader(input1);
            TextReader reader2 = new StringReader(input2);
            DiffResult result = PerformDiff(reader1, reader2);
            string msg = "comparing " + input1 + " to " + input2 + ": " + result.Difference;
            Assertion.AssertEquals(msg, expected, result.Equal);
        }
        
    }
}