package net.finetunes.ftcldstr.actionhandlers.webdav;

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
    
    public void handle(String pathTranslated) {
        
        // TODO: use full path name in the filesystem for pathTranslated 
        
        String fn = pathTranslated;
        Logger.debug("GET: " + fn);
        
        if (FileOperationsService.is_hidden(fn)) {
            OutputService.printHeaderAndContent("404 Not Found", "text/plain", "404 - NOT FOUND");
        }
        else if (FileOperationsService.is_directory(fn) && !ConfigService.FANCYINDEXING) {
            OutputService.printHeaderAndContent("404 Not Found", "text/plain", "404 - NOT FOUND");
        }
        else if (FileOperationsService.file_exits(fn) /* && $cgi->param('action') eq 'davmount' */) {
            // ...
        }
        
        
        
        /*
        if (is_hidden($fn)) {
            printHeaderAndContent('404 Not Found','text/plain','404 - NOT FOUND');
        } elsif (-d $fn && !$FANCYINDEXING) {
            printHeaderAndContent('404 Not Found','text/plain','404 - NOT FOUND');
        } elsif (-e $fn && $cgi->param('action') eq 'davmount') {
            my $su = $ENV{REDIRECT_SCRIPT_URI} || $ENV{SCRIPT_URI};
            my $bn = basename($fn);
            $su =~ s/\Q$bn\E\/?//;
            $bn.='/' if -d $fn && $bn!~/\/$/;
            printHeaderAndContent('200 OK','application/davmount+xml',
                   qq@<dm:mount xmlns:dm="http://purl.org/NET/webdav/mount"><dm:url>$su</dm:url><dm:open>$bn</dm:open></dm:mount>@);
        } elsif ($ENABLE_THUMBNAIL  && -f $fn && -r $fn && $cgi->param('action') eq 'thumb') {
            my $image = Graphics::Magick->new;
            my $width = $THUMBNAIL_WIDTH || $ICON_WIDTH || 22;
            if ($ENABLE_THUMBNAIL_CACHE) {
                my $uniqname = $fn;
                $uniqname=~s/\//_/g;
                my $cachefile = "$THUMBNAIL_CACHEDIR/$uniqname.thumb";
                mkdir($THUMBNAIL_CACHEDIR) if ! -e $THUMBNAIL_CACHEDIR;
                if (! -e $cachefile || (stat($fn))[9] > (stat($cachefile))[9]) {
                    $image->Read($fn);
                    $image->Resize(geometry=>$width,filter=>'Gaussian');
                    $image->Write($cachefile);
                }
                if (open(my $cf, "<$cachefile")) {
                    print $cgi->header(-status=>'200 OK',-type=>getMIMEType($fn), -ETag=>getETag($cachefile), -Content-length=>(stat($cachefile))[7]);
                    binmode $cf;
                    binmode STDOUT;
                    print while(<$cf>);
                    close($cf);
                }
            } else {
                print $cgi->header(-status=>'200 OK',-type=>getMIMEType($fn), -ETag=>getETag($fn));
                $image->Read($fn);
                $image->Resize(geometry=>$width,filter=>'Gaussian');
                binmode STDOUT;
                $image->Write('-');
            }
        } elsif (-e $fn && $cgi->param('action') eq 'props') {
            my $content = "";
            $content .= start_html("$REQUEST_URI properties");
            $content .= $LANGSWITCH if defined $LANGSWITCH;
            $content .= $HEADER if defined $HEADER;
            my $fullparent = dirname($REQUEST_URI) .'/';
            $fullparent = '/' if $fullparent eq '//' || $fullparent eq '';
            $content .=$cgi->h1( (-d $fn ? getQuickNavPath($REQUEST_URI,getQueryParams()) 
                             : getQuickNavPath($fullparent,getQueryParams())
                               .' '.$cgi->a({-href=>$REQUEST_URI}, basename($REQUEST_URI))
                          ). _tl('properties'));
            $content .= $cgi->a({href=>$REQUEST_URI,title=>_tl('clickforfullsize')},$cgi->img({-src=>$REQUEST_URI.($ENABLE_THUMBNAIL?'?action=thumb':''), -alt=>'image', -style=>'border:0; width:'.($ENABLE_THUMBNAIL?$THUMBNAIL_WIDTH:200)})) if getMIMEType($fn) =~ /^image\//;
            $content .= $cgi->start_table({-style=>'width:100%;table-layout:fixed;'});
            local(%NAMESPACEELEMENTS);
            my $dbprops = db_getProperties($fn);
            my @bgcolors = ( '#ffffff', '#eeeeee' );
            my (%visited);
            my $bgcolor;
            $content.=$cgi->Tr({-style=>"background-color:#dddddd;text-align:left"}, $cgi->th({-style=>'width:25%'},_tl('propertyname')), $cgi->th({-style=>'width:75%'},_tl('propertyvalue')));
            foreach my $prop (sort {nonamespace(lc($a)) cmp nonamespace(lc($b)) } keys %{$dbprops},-d $fn ? @KNOWN_COLL_PROPS : @KNOWN_FILE_PROPS ) {
                my (%r200);
                next if exists $visited{$prop} || exists $visited{'{'.getNameSpaceUri($prop).'}'.$prop};
                if (exists $$dbprops{$prop}) {
                    $r200{prop}{$prop}=$$dbprops{$prop};
                } else {
                    getProperty($fn, $REQUEST_URI, $prop, undef, \%r200, \my %r404);
                }
                $visited{$prop}=1;
                $NAMESPACEELEMENTS{nonamespace($prop)}=1;
                my $title = createXML($r200{prop},1);
                my $value = createXML($r200{prop}{$prop},1);
                my $namespace = getNameSpaceUri($prop);
                if ($prop =~ /^\{([^\}]*)\}/) {
                    $namespace = $1;
                }
                push @bgcolors, $bgcolor = shift @bgcolors;
                $content.= $cgi->Tr( {-style=>"background-color:$bgcolor; text-align:left" },
                     $cgi->th({-title=>$namespace, -style=>'vertical-align:top;'},nonamespace($prop))
                    .$cgi->td({-title=>$title, -style=>'vertical-align:bottom;' }, 
                            $cgi->pre({style=>'margin:0px; overflow:auto;'},$cgi->escapeHTML($value)))
                    );
                
            }
            $content.=$cgi->end_table();
            $content.=$cgi->hr().$SIGNATURE if defined $SIGNATURE;
            $content.=$cgi->end_html();
            printHeaderAndContent('200 OK', 'text/html', $content, 'Cache-Control: no-cache, no-store');
        } elsif (-d $fn) {
            my $ru = $REQUEST_URI;
            my $content = "";
            debug("_GET: directory listing of $fn");
            $content .= start_html($ru);
            $content .= $LANGSWITCH if defined $LANGSWITCH;
            $content .= $HEADER if defined $HEADER;
            if ($ALLOW_SEARCH && -r $fn) {
                my $search = $cgi->param('search');
                $content .= $cgi->start_form(-method=>'GET');
                $content .= $cgi->div({-style=>'text-align:right;font-size:0.8em;padding:2px 0 0 0;border:0;margin:0;'}, _tl('search'). ' '. $cgi->input({-title=>_tl('searchtooltip'),-onkeyup=>'javascript:if (this.size<this.value.length || (this.value.length<this.size && this.value.length>10)) this.size=this.value.length;', -style=>'font-size: 0.8em;', -name=>'search',-size=>$search?(length($search)>10?length($search):10):10, -value=>defined $search?$search:''}));
                $content .= $cgi->end_form();
            }
            if ( my $msg = $cgi->param('errmsg') || $cgi->param('msg')) {
                my @params = ();
                my $p=1;
                while (defined $cgi->param("p$p")) {
                    push @params, $cgi->escapeHTML($cgi->param("p$p"));
                    $p++;
                }
                $content .= $cgi->div({-style=>'background-color:'.($cgi->param('errmsg')?'#ffeeee':'#eeeeff')}, sprintf(_tl('msg_'.$msg),@params));
            }
            if ($cgi->param('search')) {
                $content.=getSearchResult($cgi->param('search'),$fn,$ru);
            } else {
                $content .= $cgi->div({-style=>'background-color:#ffeeee'}, _tl('foldernotwriteable')) if (!-w $fn) ;
                $content .= $cgi->div({-style=>'background-color:#ffeeee'}, _tl('foldernotreadable')) if (!-r $fn) ;

                my ($list, $count) = getFolderList($fn,$ru);
                $content.=$list;
                if ($ALLOW_FILE_MANAGEMENT && -w $fn) {
                    $content.=$cgi->hr();
                    $content.='&bull; '._tl('createfoldertext').$cgi->input({-name=>'colname', -size=>30}).$cgi->submit(-name=>'mkcol',-value=>_tl('createfolderbutton'));
                    if ($count>0) {
                        $content.=$cgi->br().'&bull; '._tl('movefilestext');
                        $content.=$cgi->input({-name=>'newname',-size=>30}).$cgi->submit(-name=>'rename',-value=>_tl('movefilesbutton'),-onclick=>'return window.confirm("'._tl('movefilesconfirm').'");');
                        $content.=$cgi->br().'&bull; '.$cgi->submit(-name=>'delete', -value=>_tl('deletefilesbutton'), -onclick=>'return window.confirm("'._tl('deletefilesconfirm').'");')
                            ._tl('deletefilestext'); 
                    }
                    $content .= $cgi->hr()
                            .'&bull; '
                            ._tl('changefilepermissions')
                            .(defined $PERM_USER 
                                ? _tl('user')
                                    .$cgi->checkbox_group(-name=>'fp_user', -values=>$PERM_USER,
                                        -labels=>{'r'=>_tl('readable'), 'w'=>_tl('writeable'), 'x'=>_tl('executable'), 's'=>_tl('setuid')})
                                : ''
                              )
                            .(defined $PERM_GROUP
                                ? _tl('group')
                                    .$cgi->checkbox_group(-name=>'fp_group', -values=>$PERM_GROUP,
                                        -labels=>{'r'=>_tl('readable'), 'w'=>_tl('writeable'), 'x'=>_tl('executable'), 's'=>_tl('setgid')})
                                : ''
                             )
                            .(defined $PERM_OTHERS
                                ? _tl('others')
                                    .$cgi->checkbox_group(-name=>'fp_others', -values=>$PERM_OTHERS,
                                        -labels=>{'r'=>_tl('readable'), 'w'=>_tl('writeable'), 'x'=>_tl('executable'), 't'=>_tl('sticky')})
                                : ''
                             )
                            . '; '. $cgi->popup_menu(-name=>'fp_type',-values=>['a','s','r'], -labels=>{ 'a'=>_tl('add'), 's'=>_tl('set'), 'r'=>_tl('remove')})
                            .($ALLOW_CHANGEPERMRECURSIVE ? '; ' .$cgi->checkbox_group(-name=>'fp_recursive', -value=>['recursive'], 
                                    -labels=>{'recursive'=>_tl('recursive')}) : '')
                            . '; '.$cgi->submit(-name=>'changeperm',-value=>_tl('changepermissions'),
                                    -onclick=>'return window.confirm("'._tl('changepermconfirm').'");')
                            . $cgi->br().'&nbsp;&nbsp;'._tl('changepermlegend')
                        if $ALLOW_CHANGEPERM;

                    if ($ALLOW_ZIP_UPLOAD || $ALLOW_ZIP_DOWNLOAD) {
                        $content.=$cgi->hr();
                        $content.='&bull; '.$cgi->submit(-name=>'zip',-value=>_tl('zipdownloadbutton'))._tl('zipdownloadtext').$cgi->br() if $ALLOW_ZIP_DOWNLOAD && $count>0;
                        $content.='&bull; '._tl('zipuploadtext').$cgi->filefield(-name=>'zipfile_upload', -multiple=>'multiple')
                                . $cgi->submit(-name=>'uncompress', -value=>_tl('zipuploadbutton'),-onclick=>'return window.confirm("'._tl('zipuploadconfirm').'");')
                            if $ALLOW_ZIP_UPLOAD;
                    }
                }
                $content.=$cgi->end_form() if $ALLOW_FILE_MANAGEMENT;
                $content .= $cgi->hr().$cgi->start_multipart_form(-onsubmit=>'return window.confirm("'._tl('confirm').'");')
                    .$cgi->hidden(-name=>'upload',-value=>1)
                    .$cgi->span({-id=>'file_upload'},'&bull; '._tl('fileuploadtext').$cgi->filefield(-name=>'file_upload', -multiple=>'multiple' ))
                    .$cgi->span({-id=>'moreuploads'},"").$cgi->submit(-name=>'filesubmit',-value=>_tl('fileuploadbutton'),-onclick=>'return window.confirm("'._tl('fileuploadconfirm').'");')
                    .' '
                    .$cgi->a({-onclick=>'javascript:document.getElementById("moreuploads").innerHTML=document.getElementById("moreuploads").innerHTML+"<br/>"+document.getElementById("file_upload").innerHTML',-href=>'#'},_tl('fileuploadmore'))
                    .' ('.($CGI::POST_MAX / 1048576).' MB max)'
                    .$cgi->end_form() if $ALLOW_POST_UPLOADS && -w $fn ;

            }

            $content .= $cgi->hr().$SIGNATURE if defined $SIGNATURE;
            $content .= $cgi->end_html();
            printHeaderAndContent('200 OK','text/html',$content,'Cache-Control: no-cache, no-store' );
        } elsif (-e $fn && !-r $fn) {
            printHeaderAndContent('403 Forbidden','text/plain', '403 Forbidden');
        } elsif (-e $fn) {
            debug("_GET: DOWNLOAD");
            printFileHeader($fn);
            if (open(F,"<$fn")) {
                binmode(STDOUT);
                while (read(F,my $buffer, $BUFSIZE)>0) {
                    print $buffer;
                }
                close(F);
            }
        } else {
            debug("GET: $fn NOT FOUND!");
            printHeaderAndContent('404 Not Found','text/plain','404 - FILE NOT FOUND');
        }
        */        
        
        
        
    }

}
