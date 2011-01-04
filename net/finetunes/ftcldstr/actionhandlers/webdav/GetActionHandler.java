package net.finetunes.ftcldstr.actionhandlers.webdav;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;
import net.finetunes.ftcldstr.helper.ConfigService;
import net.finetunes.ftcldstr.helper.Logger;
import net.finetunes.ftcldstr.rendering.OutputService;
import net.finetunes.ftcldstr.routines.fileoperations.FileOperationsService;

/**
 * The GET method means retrieve whatever information (in the form of an
 * entity) is identified by the Request-URI. If the Request-URI refers
 * to a data-producing process, it is the produced data which shall be
 * returned as the entity in the response and not the source text of the
 * process, unless that text happens to be the output of the process.
 * 
 * The semantics of GET are unchanged when applied to a collection,
 * since GET is defined as, "retrieve whatever information (in the form
 * of an entity) is identified by the Request-URI" [RFC2616].  GET, when
 * applied to a collection, may return the contents of an "index.html"
 * resource, a human-readable view of the contents of the collection, or
 * something else altogether.  Hence, it is possible that the result of
 * a GET on a collection will bear no correlation to the membership of
 * the collection.
 * 
 * Descriptions from RF4918 (c) The IETF Trust (2007)
 * and RFC 2616 (C) The Internet Society (1999).
 * 
 * http://www.ietf.org/rfc/rfc4918.txt
 * http://www.ietf.org/rfc/rfc2616.txt
 * 
 */

public class GetActionHandler extends AbstractActionHandler {
    
    public void handle(final HttpServletRequest request, final HttpServletResponse response,
            String pathTranslated) {
        
        // TODO: use full path name in the filesystem for pathTranslated 
        
        String fn = pathTranslated;
        Logger.debug("GET: " + fn);
        
        if (FileOperationsService.is_hidden(fn)) {
            OutputService.printHeaderAndContent("404 Not Found", "text/plain", "404 - NOT FOUND");
        }
        else if (FileOperationsService.is_directory(fn) && !ConfigService.FANCYINDEXING) {
            OutputService.printHeaderAndContent("404 Not Found", "text/plain", "404 - NOT FOUND");
        }
        else if (FileOperationsService.file_exits(fn) && 
                request.getParameter("action") != null && request.getParameter("action").equals("davmount")) {
            doDavmountRequest(fn);
        }
        else if (ConfigService.ENABLE_THUMBNAIL && FileOperationsService.is_plain_file(fn) &&
                FileOperationsService.is_file_readable(fn) &&
                request.getParameter("action") != null && request.getParameter("action").equals("thumb")) {
            doThumbnailRequest(fn);
        }
        else if (FileOperationsService.file_exits(fn) &&
                request.getParameter("action") != null && request.getParameter("action").equals("props")) {
            doFileIsDirectory(fn);
        }
        else if (FileOperationsService.is_directory(fn)) {
        }
        else if (FileOperationsService.file_exits(fn) &&
                !FileOperationsService.is_file_readable(fn)) {
            OutputService.printHeaderAndContent("403 Forbidden", "text/plain", "403 Forbidden");
        }
        else if (FileOperationsService.file_exits(fn)) {
            doFileExists(fn);
        }
        else {
            Logger.debug("GET: " + fn + " NOT FOUND!");
            OutputService.printHeaderAndContent("404 Not Found", "text/plain", "404 - FILE NOT FOUND");
        }
    }
    
    // TODO
    private void doDavmountRequest(String fn) {
//      my $su = $ENV{REDIRECT_SCRIPT_URI} || $ENV{SCRIPT_URI};
//      my $bn = basename($fn);
//      $su =~ s/\Q$bn\E\/?//;
//      $bn.='/' if -d $fn && $bn!~/\/$/;
//      printHeaderAndContent('200 OK','application/davmount+xml',
//             qq@<dm:mount xmlns:dm="http://purl.org/NET/webdav/mount"><dm:url>$su</dm:url><dm:open>$bn</dm:open></dm:mount>@);            
        
    }
    
    // TODO
    private void doThumbnailRequest(String fn) {
//      my $image = Graphics::Magick->new;
//      my $width = $THUMBNAIL_WIDTH || $ICON_WIDTH || 22;
//      if ($ENABLE_THUMBNAIL_CACHE) {
//          my $uniqname = $fn;
//          $uniqname=~s/\//_/g;
//          my $cachefile = "$THUMBNAIL_CACHEDIR/$uniqname.thumb";
//          mkdir($THUMBNAIL_CACHEDIR) if ! -e $THUMBNAIL_CACHEDIR;
//          if (! -e $cachefile || (stat($fn))[9] > (stat($cachefile))[9]) {
//              $image->Read($fn);
//              $image->Resize(geometry=>$width,filter=>'Gaussian');
//              $image->Write($cachefile);
//          }
//          if (open(my $cf, "<$cachefile")) {
//              print $cgi->header(-status=>'200 OK',-type=>getMIMEType($fn), -ETag=>getETag($cachefile), -Content-length=>(stat($cachefile))[7]);
//              binmode $cf;
//              binmode STDOUT;
//              print while(<$cf>);
//              close($cf);
//          }
//      } else {
//          print $cgi->header(-status=>'200 OK',-type=>getMIMEType($fn), -ETag=>getETag($fn));
//          $image->Read($fn);
//          $image->Resize(geometry=>$width,filter=>'Gaussian');
//          binmode STDOUT;
//          $image->Write('-');
//      }            
        
    }
    
    // TODO
    private void doFileIsDirectory(String fn) {
//      my $ru = $REQUEST_URI;
//      my $content = "";
//      debug("_GET: directory listing of $fn");
//      $content .= start_html($ru);
//      $content .= $LANGSWITCH if defined $LANGSWITCH;
//      $content .= $HEADER if defined $HEADER;
//      if ($ALLOW_SEARCH && -r $fn) {
//          my $search = $cgi->param('search');
//          $content .= $cgi->start_form(-method=>'GET');
//          $content .= $cgi->div({-style=>'text-align:right;font-size:0.8em;padding:2px 0 0 0;border:0;margin:0;'}, _tl('search'). ' '. $cgi->input({-title=>_tl('searchtooltip'),-onkeyup=>'javascript:if (this.size<this.value.length || (this.value.length<this.size && this.value.length>10)) this.size=this.value.length;', -style=>'font-size: 0.8em;', -name=>'search',-size=>$search?(length($search)>10?length($search):10):10, -value=>defined $search?$search:''}));
//          $content .= $cgi->end_form();
//      }
//      if ( my $msg = $cgi->param('errmsg') || $cgi->param('msg')) {
//          my @params = ();
//          my $p=1;
//          while (defined $cgi->param("p$p")) {
//              push @params, $cgi->escapeHTML($cgi->param("p$p"));
//              $p++;
//          }
//          $content .= $cgi->div({-style=>'background-color:'.($cgi->param('errmsg')?'#ffeeee':'#eeeeff')}, sprintf(_tl('msg_'.$msg),@params));
//      }
//      if ($cgi->param('search')) {
//          $content.=getSearchResult($cgi->param('search'),$fn,$ru);
//      } else {
//          $content .= $cgi->div({-style=>'background-color:#ffeeee'}, _tl('foldernotwriteable')) if (!-w $fn) ;
//          $content .= $cgi->div({-style=>'background-color:#ffeeee'}, _tl('foldernotreadable')) if (!-r $fn) ;
//
//          my ($list, $count) = getFolderList($fn,$ru);
//          $content.=$list;
//          if ($ALLOW_FILE_MANAGEMENT && -w $fn) {
//              $content.=$cgi->hr();
//              $content.='&bull; '._tl('createfoldertext').$cgi->input({-name=>'colname', -size=>30}).$cgi->submit(-name=>'mkcol',-value=>_tl('createfolderbutton'));
//              if ($count>0) {
//                  $content.=$cgi->br().'&bull; '._tl('movefilestext');
//                  $content.=$cgi->input({-name=>'newname',-size=>30}).$cgi->submit(-name=>'rename',-value=>_tl('movefilesbutton'),-onclick=>'return window.confirm("'._tl('movefilesconfirm').'");');
//                  $content.=$cgi->br().'&bull; '.$cgi->submit(-name=>'delete', -value=>_tl('deletefilesbutton'), -onclick=>'return window.confirm("'._tl('deletefilesconfirm').'");')
//                      ._tl('deletefilestext'); 
//              }
//              $content .= $cgi->hr()
//                      .'&bull; '
//                      ._tl('changefilepermissions')
//                      .(defined $PERM_USER 
//                          ? _tl('user')
//                              .$cgi->checkbox_group(-name=>'fp_user', -values=>$PERM_USER,
//                                  -labels=>{'r'=>_tl('readable'), 'w'=>_tl('writeable'), 'x'=>_tl('executable'), 's'=>_tl('setuid')})
//                          : ''
//                        )
//                      .(defined $PERM_GROUP
//                          ? _tl('group')
//                              .$cgi->checkbox_group(-name=>'fp_group', -values=>$PERM_GROUP,
//                                  -labels=>{'r'=>_tl('readable'), 'w'=>_tl('writeable'), 'x'=>_tl('executable'), 's'=>_tl('setgid')})
//                          : ''
//                       )
//                      .(defined $PERM_OTHERS
//                          ? _tl('others')
//                              .$cgi->checkbox_group(-name=>'fp_others', -values=>$PERM_OTHERS,
//                                  -labels=>{'r'=>_tl('readable'), 'w'=>_tl('writeable'), 'x'=>_tl('executable'), 't'=>_tl('sticky')})
//                          : ''
//                       )
//                      . '; '. $cgi->popup_menu(-name=>'fp_type',-values=>['a','s','r'], -labels=>{ 'a'=>_tl('add'), 's'=>_tl('set'), 'r'=>_tl('remove')})
//                      .($ALLOW_CHANGEPERMRECURSIVE ? '; ' .$cgi->checkbox_group(-name=>'fp_recursive', -value=>['recursive'], 
//                              -labels=>{'recursive'=>_tl('recursive')}) : '')
//                      . '; '.$cgi->submit(-name=>'changeperm',-value=>_tl('changepermissions'),
//                              -onclick=>'return window.confirm("'._tl('changepermconfirm').'");')
//                      . $cgi->br().'&nbsp;&nbsp;'._tl('changepermlegend')
//                  if $ALLOW_CHANGEPERM;
//
//              if ($ALLOW_ZIP_UPLOAD || $ALLOW_ZIP_DOWNLOAD) {
//                  $content.=$cgi->hr();
//                  $content.='&bull; '.$cgi->submit(-name=>'zip',-value=>_tl('zipdownloadbutton'))._tl('zipdownloadtext').$cgi->br() if $ALLOW_ZIP_DOWNLOAD && $count>0;
//                  $content.='&bull; '._tl('zipuploadtext').$cgi->filefield(-name=>'zipfile_upload', -multiple=>'multiple')
//                          . $cgi->submit(-name=>'uncompress', -value=>_tl('zipuploadbutton'),-onclick=>'return window.confirm("'._tl('zipuploadconfirm').'");')
//                      if $ALLOW_ZIP_UPLOAD;
//              }
//          }
//          $content.=$cgi->end_form() if $ALLOW_FILE_MANAGEMENT;
//          $content .= $cgi->hr().$cgi->start_multipart_form(-onsubmit=>'return window.confirm("'._tl('confirm').'");')
//              .$cgi->hidden(-name=>'upload',-value=>1)
//              .$cgi->span({-id=>'file_upload'},'&bull; '._tl('fileuploadtext').$cgi->filefield(-name=>'file_upload', -multiple=>'multiple' ))
//              .$cgi->span({-id=>'moreuploads'},"").$cgi->submit(-name=>'filesubmit',-value=>_tl('fileuploadbutton'),-onclick=>'return window.confirm("'._tl('fileuploadconfirm').'");')
//              .' '
//              .$cgi->a({-onclick=>'javascript:document.getElementById("moreuploads").innerHTML=document.getElementById("moreuploads").innerHTML+"<br/>"+document.getElementById("file_upload").innerHTML',-href=>'#'},_tl('fileuploadmore'))
//              .' ('.($CGI::POST_MAX / 1048576).' MB max)'
//              .$cgi->end_form() if $ALLOW_POST_UPLOADS && -w $fn ;
//
//      }
//
//      $content .= $cgi->hr().$SIGNATURE if defined $SIGNATURE;
//      $content .= $cgi->end_html();
//      printHeaderAndContent('200 OK','text/html',$content,'Cache-Control: no-cache, no-store' );            
        
    }

    // TODO
    private void doFileExists(String fn) {
        Logger.debug("GET: DOWNLOAD");
        OutputService.printFileHeader(fn);
//      if (open(F,"<$fn")) {
//          binmode(STDOUT);
//          while (read(F,my $buffer, $BUFSIZE)>0) {
//              print $buffer;
//          }
//          close(F);
//      }             
    }
    
}
