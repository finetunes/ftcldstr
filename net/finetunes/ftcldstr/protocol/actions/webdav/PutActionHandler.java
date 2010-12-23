package net.finetunes.ftcldstr.protocol.actions.webdav;

import net.finetunes.ftcldstr.protocol.actions.base.AbstractActionHandler;

/**
 * A PUT performed on an existing resource replaces the GET response
 * entity of the resource.  Properties defined on the resource may be
 * recomputed during PUT processing but are not otherwise affected.  For
 * example, if a server recognizes the content type of the request body,
 * it may be able to automatically extract information that could be
 * profitably exposed as properties.
 * 
 * A PUT request to an existing collection MAY be treated as an error 
 * (405 Method Not Allowed).
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class PutActionHandler extends AbstractActionHandler {

}
