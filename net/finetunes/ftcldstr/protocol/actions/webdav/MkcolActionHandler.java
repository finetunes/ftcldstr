package net.finetunes.ftcldstr.protocol.actions.webdav;

import net.finetunes.ftcldstr.protocol.actions.base.AbstractActionHandler;

/**
 * MKCOL creates a new collection resource at the location specified by
 * the Request-URI.  If the Request-URI is already mapped to a resource,
 * then the MKCOL MUST fail.  During MKCOL processing, a server MUST
 * make the Request-URI an internal member of its parent collection,
 * unless the Request-URI is "/".  If no such ancestor exists, the
 * method MUST fail.  When the MKCOL operation creates a new collection
 * resource, all ancestors MUST already exist, or the method MUST fail
 * with a 409 (Conflict) status code.  For example, if a request to
 * create collection /a/b/c/d/ is made, and /a/b/c/ does not exist, the
 * request must fail.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class MkcolActionHandler extends AbstractActionHandler {

}
