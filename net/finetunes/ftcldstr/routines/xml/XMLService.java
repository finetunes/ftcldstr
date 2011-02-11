package net.finetunes.ftcldstr.routines.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.routines.NamespaceService;

public class XMLService {
	
    public static void createXMLData(Map<String, Integer> namespaceElements, XMLData w, Object dd, HashMap<String, String> xmlns) {
        
        Matcher m;
        
        if (namespaceElements == null) {
            namespaceElements = new HashMap<String, Integer>();
        }
        
        if (dd != null) {
            if (dd instanceof HashMap<?, ?>) {
                
                HashMap<String, Object> d = (HashMap<String, Object>)dd;
                
                ArrayList<String> keys = new ArrayList<String>(d.keySet());
                Collections.sort(keys, new KeyComparator());
                
                for (int i = 0; i < keys.size(); i++) {
                    String e = keys.get(i);
                    String el = new String(e);
                    String euns = "";
                    String uns = "";
                    String ns = NamespaceService.getNameSpace(e);
                    String attr = "";
                    
                    if (ConfigService.DATATYPES.containsKey(e) && ConfigService.DATATYPES.get(e) != null){
                        
                        attr += " " + ConfigService.DATATYPES.get(e);
                        
                        m = checkCondition("(\\w+):dt", ConfigService.DATATYPES.get(e));
                        
                        if (m != null) {
                            if (ConfigService.NAMESPACEABBR.containsKey(m.group(1)) && 
                                    ConfigService.NAMESPACEABBR.get(m.group(1)) != null) {
                            xmlns.put(m.group(1), "1");
                            }
                        }
                    }
                    m = checkCondition("\\{([^\\}]*)\\}", e);
                    
                    if (m != null) {
                        ns = m.group(1);
                        if (ConfigService.NAMESPACES.containsKey(ns) && 
                                ConfigService.NAMESPACES.get(ns) != null) {
                            m = checkCondition("\\{[^\\}]*\\}", el);
                            if (m != null) {
                                el = m.replaceAll("");
                            }
                            ns = ConfigService.NAMESPACES.get(ns);
                        } else {
                            uns = new String(ns);
                            euns = e;
                            m = checkCondition("\\{[^\\}]*\\}", euns);
                            if (m != null) {
                                euns = m.replaceAll("");
                            }
                        }
                    }
                    String el_end = new String(el);
                    m = checkCondition(" .*$", el_end);
                    if (m != null) {
                        el_end = m.replaceAll("");
                    } 
                    String euns_end = new String(euns);
                    m = checkCondition(" .*$", euns_end);
                    if (m != null) {
                        euns_end = m.replaceAll("");
                    } 
                    
                    if (uns == null || uns.isEmpty()) {
                        if (xmlns == null){
                            xmlns = new HashMap<String, String>();
                        }
                        xmlns.put(ns, "1");
                    }
                    String nsd = "";
                    if (e.equals("xmlns")) {
                        // # ignore namespace defs
                    } else if (e.equals("content")){
                        w.setData(w.getData() + d.get(e));
                    } else if (!(d.containsKey(e) && d.get(e) != null)) {
                        if (uns != null && !uns.equals("")){
                            w.setData(w.getData() + "<" + euns + " xmlns=\"" + uns + "\"/>");
                        } else {
                            w.setData(w.getData() + "<" + ns + ":" + el + nsd + attr + "/>");
                        }
                    } 
                    else if (d.get(e) instanceof ArrayList<?>) {
                        for (int l = 0; l < ((ArrayList<Object>)d.get(e)).size(); l++) {
                            Object e1 = ((ArrayList<Object>)d.get(e)).get(l); 
                            XMLData tmpw = new XMLData();
                            tmpw.setData("");
                            createXMLData(namespaceElements, tmpw, e1, xmlns);
                            if (namespaceElements.containsKey(el) &&
                                    namespaceElements.get(el) != null) {
                                
                                
                                ArrayList<String> keys2 = new ArrayList<String>(xmlns.keySet()); 
                                for (int j = 0; j < keys2.size(); j++) {
                                    String abbr = keys2.get(j);
                                    nsd += " xmlns:" + abbr + "=\"" + ConfigService.NAMESPACEABBR.get(abbr) + "\"";
                                    xmlns.remove(keys2.get(j));
                                }
                            }
                            w.setData(w.getData() + "<" + ns + ":" + el + nsd +attr + ">");
                            w.setData(w.getData() + tmpw.getData());
                            w.setData(w.getData() + "</" + ns + ":" + el_end + ">");
                        }
                    } else {
                        if (uns != null && !uns.equals("")) {
                            w.setData(w.getData() + "<" + euns + " xmlns=\"" + uns + "\">");
                            createXMLData(namespaceElements, w, d.get(e), xmlns);
                            w.setData(w.getData() + "</" + euns_end + ">");
                        } else {
                            XMLData tmpw2 = new XMLData(); 
                            tmpw2.setData("");
                            createXMLData(namespaceElements, tmpw2, d.get(e), xmlns);
                            if (namespaceElements.containsKey(el) &&
                                    namespaceElements.get(el) != null) {
                                
                                ArrayList<String> keys3 = new ArrayList<String>(xmlns.keySet()); 
                                for (int k = 0; k < keys3.size(); k++) {
                                    String abbr = keys3.get(k);
                                    nsd += " xmlns:" + abbr + "=\"" + ConfigService.NAMESPACEABBR.get(abbr) + "\"";
                                    xmlns.remove(keys3.get(k));
                                }
                            }
                            w.setData(w.getData() + "<" + ns + ":" + el + nsd + attr + ">");
                            w.setData(w.getData() + tmpw2.getData());
                            w.setData(w.getData() + "</" + ns + ":" + el_end + ">");
                        }
                    }
                }
            } else if (dd instanceof ArrayList<?>) {
                for (int i = 0; i < ((ArrayList<Object>)dd).size(); i++) {
                    createXMLData(namespaceElements, w, ((ArrayList<Object>)dd).get(i), xmlns);
                }
            } else if (dd.getClass().isPrimitive() || dd instanceof String) {
                w.setData(w.getData() + (String)dd);
            } else {
                Logger.log("XMLService: unknown data type:" + dd.getClass().toString());
                w.setData(w.getData() + (String)dd);
            }
        }
        
    }

    public static void createXMLData(Map<String, Integer> namespaceElements, XMLData w, HashMap<String, Object> d) {
        createXMLData(namespaceElements, w, d, null);
    }
    
    public static String createXML(Map<String, Integer> namespaceElements, HashMap<String, Object> dataRef, boolean withoutp) {
        XMLData data = new XMLData();
        data.setData("<?xml version=\"1.0\" encoding=\"" + ConfigService.CHARSET + "\"?>");
        createXMLData(namespaceElements, data, dataRef);
        return data.getData();
    }
	
    public static String createXML(Map<String, Integer> namespaceElements, HashMap<String, Object> dataRef) {
        
        return createXML(namespaceElements, dataRef, false);
    }
    
    private static Matcher checkCondition (String regExp, String input) {
        Pattern pattern = Pattern.compile(regExp);
        Matcher m = pattern.matcher(input);
        return m.find()? m : null;
    }
    
    public static String convXML2Str(HashMap<String, Object> xml) {
        
        if (xml != null) {
            String x = XMLService.createXML(ConfigService.NAMESPACEELEMENTS, xml, true);
            if (x != null) {
                return x.toLowerCase();
            }
            
            return x;
        }
        
        return null;
    }    
    
// usage    
//    public static void main (String[] args) {
//        
//        HashMap<String, Object> dataRef = new HashMap<String, Object>();
//        HashMap<String, Object> dataRef1 = new HashMap<String, Object>();
//        HashMap<String, Object> dataRef2 = new HashMap<String, Object>();
//        HashMap<String, Object> dataRef3 = new HashMap<String, Object>();
//        dataRef3.put("st8", "xxxx");
//        dataRef3.put("st9", "yyyy");
//        dataRef2.put("st1", dataRef3);
//        dataRef1.put("st11", dataRef2);
//        dataRef.put("stef1", dataRef1);
//        dataRef.put("aaaa", "iiiii");
//        dataRef.put("schedule-inbox", "333");
//        dataRef.put("resourcetype", "00000");
//        dataRef.put("owner", "44444");
//        dataRef.put("properties", "11111");
//        dataRef.put("read-free-busy", "ccccc");
//        String[] ss = {"z1", "z2", "z3"};
//        dataRef.put("zzzzzz", ss);
//        String s = createXML(dataRef, false);
//        System.out.println("*****" + s);
//        
//    }    

}

class KeyComparator implements Comparator<String> {

    public int compare(String key1, String key2){

        Integer key1Value = ConfigService.ELEMENTORDER.containsKey(key1)?ConfigService.ELEMENTORDER.get(key1):ConfigService.ELEMENTORDER.get("default");  
        Integer key2Value = ConfigService.ELEMENTORDER.containsKey(key2)?ConfigService.ELEMENTORDER.get(key2):ConfigService.ELEMENTORDER.get("default");
        
        if (key1Value != 1000 || key2Value != 1000){
            return key1Value.compareTo(key2Value);
        }
        return key1Value.compareTo(key2Value);

    }

}

// String encapsulated in a separate class
// to be able to pass as a parameter by reference
class XMLData {
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
