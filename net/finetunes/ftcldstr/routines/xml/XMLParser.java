package net.finetunes.ftcldstr.routines.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

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
	
	// returns map of an xml object
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
	                dbf.setNamespaceAware(true);
	                dbf.setValidating(true);
	                DocumentBuilder db = dbf.newDocumentBuilder();
	                InputStream is = new ByteArrayInputStream(text.getBytes(encoding));
	                Document doc = db.parse(is);
	                doc.getDocumentElement().normalize();
	                
                    NodeList nodes = doc.getChildNodes();
	                XMLIn in = new XMLIn();
	                
                    in.addXml(outXml, nodes.item(0));
	                if (!keepRoot) {
	                    if (outXml != null) {
	                        Set<String> keys = outXml.keySet();
	                        if (keys.size() > 0) {
	                            Object c = outXml.get(keys.toArray(new String[0])[0]);
	                            if (c instanceof HashMap<?, ?>) {
	                                outXml = (HashMap<String, Object>)c;
	                            }
	                            else if (c instanceof ArrayList<?>) {
	                                ArrayList<HashMap<String, Object>> c2 = (ArrayList<HashMap<String, Object>>)c;
	                                if (c2 != null && c2.size() > 0) {
	                                    Object c3 = c2.get(0);
	                                    if (c3 instanceof HashMap<?, ?>) {
	                                        outXml = (HashMap<String, Object>)c3;
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
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
            String ns = "";
            String nodename = "";
            
            if (node != null) {
                ns = node.getNamespaceURI();
                nodename = node.getNodeName();
                
                if (ns == null) {
                    ns = "";
                }
                if (!ns.isEmpty()) {
                    ns = "{" + ns + "}";
                }

                if (ns != null && !ns.isEmpty() && nodename != null) {
                    if (nodename.indexOf(":") > -1) {
                        nodename = nodename.substring(nodename.indexOf(":") + 1);
                    }
                    nodename = ns + nodename;
                }
            }
                
            String type = getNodeType(node);
            Object value = new Object();
            if (type.equals("Hash")) {
                value = new HashMap<String, Object>();
            } else if (type.equals("String") && node.getNodeType() != Node.TEXT_NODE) {
                if (node.getChildNodes().item(0) != null) {
                    value = node.getChildNodes().item(0).getNodeValue();
                }
                else {
                    value = null;
                }
                //value = node.getNodeValue();
            }
            
            if (node.getNodeType() != Node.TEXT_NODE) {
                if (parent instanceof HashMap<?, ?>) {
                    if (((HashMap<String, Object>)parent).containsKey(nodename)) {
                        if (((HashMap<String, Object>)parent).get(nodename) instanceof ArrayList<?>) {
                            ArrayList<Object> parentObj = (ArrayList<Object>)((HashMap<String, Object>)parent).get(nodename);
                            parentObj.add(value);
                            ((HashMap<String, Object>)parent).put(nodename, parentObj);
                        } else {
                            ArrayList<Object> parentArray = new ArrayList<Object>();
                            parentArray.add(((HashMap<String, Object>)parent).get(nodename));
                            parentArray.add(value);
                            
                            ((HashMap<String, Object>)parent).put(nodename, parentArray);
                        }
                    } else {
                        ((HashMap<String, Object>)parent).put(nodename, value);
                    }
                } else if (parent instanceof ArrayList<?>) {
                    ((ArrayList) parent).add(value);
                } else if (parent instanceof String) {
                    return;
                }
            }
            
            NodeList nodes = node.getChildNodes();
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    addXml(value, nodes.item(i));
                }
            }
        }
        
        public String getNodeType(Node node) {
            String retValue = "String";
            
            if (node != null && node.getChildNodes() != null && node.getChildNodes().getLength() > 1) {
                retValue = "Hash";
            } else if (node == null || node.getChildNodes() == null || node.getChildNodes().getLength() == 0) {
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
