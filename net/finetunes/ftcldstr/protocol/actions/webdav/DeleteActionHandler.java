package net.finetunes.ftcldstr.protocol.actions.webdav;

import net.finetunes.ftcldstr.protocol.actions.base.AbstractActionHandler;

/**
 * The DELETE method requests that the origin server delete the resource
 * identified by the Request-URI. This method MAY be overridden by human
 * intervention (or other means) on the origin server. The client cannot
 * be guaranteed that the operation has been carried out, even if the
 * status code returned from the origin server indicates that the action
 * has been completed successfully. However, the server SHOULD NOT
 * indicate success unless, at the time the response is given, it
 * intends to delete the resource or move it to an inaccessible
 * location.
 *
 * The DELETE method on a collection MUST act as if a "Depth: infinity"
 * header was used on it.  A client MUST NOT submit a Depth header with
 * a DELETE on a collection with any value but infinity.
 * 
 * DELETE instructs that the collection specified in the Request-URI and
 * all resources identified by its internal member URLs are to be
 * deleted.
 * 
 * Descriptions from RF4918 (c) The IETF Trust (2007)
 * and RFC 2616 (C) The Internet Society (1999).
 * 
 * http://www.ietf.org/rfc/rfc4918.txt
 * http://www.ietf.org/rfc/rfc2616.txt
 * 
 */

public class DeleteActionHandler extends AbstractActionHandler {

}
