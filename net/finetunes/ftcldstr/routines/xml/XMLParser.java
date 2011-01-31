package net.finetunes.ftcldstr.routines.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {
	
	// TODO: XML object should be returned here
    // requestParams.getRequest().getCharacterEncoding()
    
    // error control: returned null means some parsing error occured or input text was null
    // empty hashmap if input text was empty
    // hashmap with data if everything was ok
	public HashMap<String, Object> simpleXMLParser(String text, String encoding, boolean keepRoot) {
		
	    if (text != null) {
	        
	        if (encoding == null) {
	            encoding = ConfigService.CHARSET;
	        }
	        
            HashMap<String, Object> outXml = new HashMap<String, Object>();
	        if (!text.isEmpty()) {
	            try {
	                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	                DocumentBuilder db = dbf.newDocumentBuilder();
	                InputStream is = new ByteArrayInputStream(text.getBytes(encoding));
	                Document doc = db.parse(is);
	                doc.getDocumentElement().normalize();
	                
	                NodeList nodes = doc.getChildNodes();
	                XMLIn in = new XMLIn();
	                in.addXml(outXml, nodes.item(0));
	            }
	            catch (ParserConfigurationException e) {
	                Logger.log("Exception on xml parsing: " + e.getMessage());
	                return null;
	            }
	            catch (UnsupportedEncodingException e) {
	                Logger.log("Exception on xml parsing: " + e.getMessage());
	                return null;
	            }
	            catch (SAXException e) {
	                Logger.log("Exception on xml parsing: " + e.getMessage());
	                return null;
	            }
	            catch (IOException e) {
	                Logger.log("Exception on xml parsing: " + e.getMessage());
	                return null;
	            }	            
	        }
            return outXml;
	    }
	    return null;
	}
	
	public HashMap<String, Object> simpleXMLParser(String text, String encoding) {
	    return simpleXMLParser(text, encoding, false);
	}

    public class XMLIn{
    
        public void addXml(Object parent, Node node) {
            String type = getNodeType(node);
            Object value = new Object();
            if (type.equals("Hash")) {
                value = new HashMap<String, Object>();
            } else if (type.equals("String") && node.getNodeType() != Node.TEXT_NODE) {
                value = node.getChildNodes().item(0).getNodeValue();
                //value = node.getNodeValue();
            } 
            if (parent instanceof HashMap<?, ?>) {
                if (((HashMap<String, Object>)parent).containsKey(node.getNodeName())) {
                    if (((HashMap<String, Object>)parent).get(node.getNodeName()).getClass().isArray()) {
                        Object parentObj = ((HashMap<String, Object>)parent).get(node.getNodeName());
                        Object[] parentArray = (Object[])parentObj;
                        Object[] parentArrayEx = new Object[parentArray.length + 1];
                        System.arraycopy(parentArray, 0, parentArrayEx, 0, parentArray.length);
                        parentArrayEx[parentArray.length] = value;
                        ((HashMap<String, Object>)parent).put(node.getNodeName(), parentArrayEx);
                    } else {
                        Object[] parentArray = {((HashMap<String, Object>)parent).get(node.getNodeName()), value};
                        ((HashMap<String, Object>)parent).put(node.getNodeName(), parentArray);
                    }
                } else {
                    ((HashMap<String, Object>)parent).put(node.getNodeName(), value);
                }
            } else if (parent.getClass().isArray()) {
                Arrays.asList(parent).add(value);
            } else if (parent instanceof String) {
                return;
            }
            
            NodeList nodes = node.getChildNodes();
            
            for (int i = 0; i < nodes.getLength(); i++) {
                addXml(value, nodes.item(i));
            }
        }
        
        public String getNodeType(Node node) {
            String retValue = "String";
            
            if (node.getChildNodes().getLength() > 1) {
                retValue = "Hash";
            } else if (node.getChildNodes().getLength() == 0) {
                retValue = "String";
            } else if (node.getChildNodes().getLength() == 1) {
                if (node.getChildNodes().item(0).getChildNodes().getLength() == 0 && node.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
                    retValue = "String";
                } else {
                    retValue = "Hash";
                }
            }
            
            return retValue;
        }
        
         public String getElementValue(Node elem) {
             
             Node n;
             if(elem != null) {
                 if (elem.hasChildNodes()) {
                     for (n = elem.getFirstChild(); n != null; n = n.getNextSibling()) {
                         if (n.getNodeType() == Node.TEXT_NODE) {
                             return n.getNodeValue();
                         }
                     }
                 }
             }
             
             return "";
        }
    }

}
