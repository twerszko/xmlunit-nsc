namespace XmlUnit {
    using System;
    using System.IO;
    using System.Xml;
    using System.Xml.Schema;
    
    public class XmlDiff {
        private readonly XmlReader _controlReader; 
        private readonly XmlReader _testReader;
        private readonly DiffConfiguration _diffConfiguration;
        private DiffResult _diffResult;
                
        public XmlDiff(TextReader control, TextReader test, 
                       DiffConfiguration diffConfiguration) {
            _diffConfiguration =  diffConfiguration;
            _controlReader = CreateXmlReader(control);
            if (control.Equals(test)) {
                _testReader = _controlReader;
            } else {
                _testReader = CreateXmlReader(test);
            }
        }
        public XmlDiff(TextReader control, TextReader test)
            : this(control, test, new DiffConfiguration()) {
        }
                
        public XmlDiff(string control, string test) 
            : this(new StringReader(control), new StringReader(test)) {
        }
        
        public XmlDiff(string control, string test, DiffConfiguration diffConfiguration) 
            : this(new StringReader(control), new StringReader(test), diffConfiguration) {
        }
        
        public XmlDiff(string control, TextReader test) 
            : this(new StringReader(control), test) {
        }
        
        public XmlDiff(TextReader control, string test) 
            : this(control, new StringReader(test)) {
        }
        
        private XmlReader CreateXmlReader(TextReader forTextReader) {
            XmlTextReader xmlReader = new XmlTextReader(_diffConfiguration.BaseURI, forTextReader);
            xmlReader.WhitespaceHandling = _diffConfiguration.WhitespaceHandling;           
            if (!_diffConfiguration.UseValidatingParser) {
                return xmlReader;
            }
            XmlValidatingReader validatingReader = new XmlValidatingReader(xmlReader);
            return validatingReader;
        }
        
        public DiffResult Compare() {
            if (_diffResult == null) {
                _diffResult = new DiffResult();
                if (!_controlReader.Equals(_testReader)) {
                    Compare(_diffResult);
                }
            }
            return _diffResult;
        }
        
        private void Compare(DiffResult result) {
            bool controlRead, testRead;
            try {
                do {
                    controlRead = _controlReader.Read();
                    testRead = _testReader.Read();
                    if (controlRead) {
                        if(testRead) {
                            CompareNodes(result);
                            CheckEmptyOrAtEndElement(result, ref controlRead, ref testRead);
                        } else {
                            DifferenceFound(DifferenceType.CHILD_NODELIST_LENGTH_ID, result);
                        } 
                    }
                } while (controlRead && testRead) ;
            } catch (FlowControlException e) {       
                Console.Out.WriteLine(e.Message);
            }
        }        
                
        private void CompareNodes(DiffResult result) {
            XmlNodeType controlNodeType = _controlReader.NodeType;
            XmlNodeType testNodeType = _testReader.NodeType;
            if (!controlNodeType.Equals(testNodeType)) {
                DifferenceFound(DifferenceType.NODE_TYPE_ID, controlNodeType, 
                                testNodeType, result);
            } else if (controlNodeType == XmlNodeType.Element) {
                CompareElements(result);
            } else if (controlNodeType == XmlNodeType.Text) {
                CompareText(result);
            }
        }
        
        private void CompareElements(DiffResult result) {
            string controlTagName = _controlReader.Name;
            string testTagName = _testReader.Name;
            if (!String.Equals(controlTagName, testTagName)) {
                DifferenceFound(DifferenceType.ELEMENT_TAG_NAME_ID, result);
            } else {
                int controlAttributeCount = _controlReader.AttributeCount;
                int testAttributeCount = _testReader.AttributeCount;
                if (controlAttributeCount != testAttributeCount) {
                    DifferenceFound(DifferenceType.ELEMENT_NUM_ATTRIBUTES_ID, result);
                } else {
                    CompareAttributes(result, controlAttributeCount);
                }
            }
        }
        
        private void CompareAttributes(DiffResult result, int controlAttributeCount) {
            string controlAttrValue, controlAttrName;
            string testAttrValue, testAttrName;
            
            _controlReader.MoveToFirstAttribute();
            _testReader.MoveToFirstAttribute();
            for (int i=0; i < controlAttributeCount; ++i) {
                
                controlAttrName = _controlReader.Name;
                testAttrName = _testReader.Name;
                
                controlAttrValue = _controlReader.Value;
                testAttrValue = _testReader.Value;
                
                if (!String.Equals(controlAttrName, testAttrName)) {
                    DifferenceFound(DifferenceType.ATTR_SEQUENCE_ID, result);
                
                    if (!_testReader.MoveToAttribute(controlAttrName)) {
                        DifferenceFound(DifferenceType.ATTR_NAME_NOT_FOUND_ID, result);
                    }
                    testAttrValue = _testReader.Value;
                }
                
                if (!String.Equals(controlAttrValue, testAttrValue)) {
                    DifferenceFound(DifferenceType.ATTR_VALUE_ID, result);
                }
                
                _controlReader.MoveToNextAttribute();
                _testReader.MoveToNextAttribute();
            }
        }
        
        private void CompareText(DiffResult result) {
            string controlText = _controlReader.Value;
            string testText = _testReader.Value;
            if (!String.Equals(controlText, testText)) {
                DifferenceFound(DifferenceType.TEXT_VALUE_ID, result);
            }
        }
        
        private void DifferenceFound(DifferenceType differenceType, DiffResult result) {
            DifferenceFound(new Difference(differenceType), result);
        }
        
        private void DifferenceFound(Difference difference, DiffResult result) {
            result.DifferenceFound(difference);
            if (!ContinueComparison(difference)) {
                throw new FlowControlException(difference);
            }
        }
        
        private void DifferenceFound(DifferenceType differenceType, 
                                     XmlNodeType controlNodeType,
                                     XmlNodeType testNodeType, 
                                     DiffResult result) {
            DifferenceFound(new Difference(differenceType, controlNodeType, testNodeType),
                            result);
        }
        
        public bool ContinueComparison(Difference afterDifference) {
            return !afterDifference.MajorDifference;
        }
        
        private void CheckEmptyOrAtEndElement(DiffResult result, 
                                              ref bool controlRead, ref bool testRead) {
            if (_controlReader.IsEmptyElement) {
                if (!_testReader.IsEmptyElement) {
                    CheckEndElement(_testReader, ref testRead, result);
                }
            } else {
                if (_testReader.IsEmptyElement) {
                    CheckEndElement(_controlReader, ref controlRead, result);
                }
            }
        }
        
        private void CheckEndElement(XmlReader reader, ref bool readResult, DiffResult result) {            
            readResult = reader.Read();
            if (!readResult || reader.NodeType != XmlNodeType.EndElement) {
                DifferenceFound(DifferenceType.CHILD_NODELIST_LENGTH_ID, result);
            }        
        }
        
        private class FlowControlException : ApplicationException {
            public FlowControlException(Difference cause) : base(cause.ToString()) {
            }
        }
        
        public string OptionalDescription {
            get {
                return _diffConfiguration.Description;
            }
        }
    }
}
