package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

/**
 * The MOVE operation on a non-collection resource is the logical
 * equivalent of a copy (COPY), followed by consistency maintenance
 * processing, followed by a delete of the source, where all three
 * actions are performed in a single operation.  The consistency
 * maintenance step allows the server to perform updates caused by the
 * move, such as updating all URLs, other than the Request-URI that
 * identifies the source resource, to point to the new destination
 * resource.
 * 
 * Description from RF4918 (c) The IETF Trust (2007).
 * http://www.ietf.org/rfc/rfc4918.txt
 * 
 */

public class MoveActionHandler extends AbstractActionHandler {

}
