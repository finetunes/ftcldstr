package net.finetunes.ftcldstr.actionhandlers.webdav;

import net.finetunes.ftcldstr.actionhandlers.base.AbstractActionHandler;

/**
 * The POST method is used to request that the origin server accept the
 * entity enclosed in the request as a new subordinate of the resource
 * identified by the Request-URI in the Request-Line.
 * 
 * Since by definition the actual function performed by POST is
 * determined by the server and often depends on the particular
 * resource, the behavior of POST when applied to collections cannot be
 * meaningfully modified because it is largely undefined.  Thus, the
 * semantics of POST are unmodified when applied to a collection.
 * 
 * Descriptions from RF4918 (c) The IETF Trust (2007)
 * and RFC 2616 (C) The Internet Society (1999).
 * 
 * http://www.ietf.org/rfc/rfc4918.txt
 * http://www.ietf.org/rfc/rfc2616.txt
 * 
 */

public class PostActionHandler extends AbstractActionHandler {

    
/*    
    debug("_POST: $PATH_TRANSLATED");

    if (!$cgi->param('file_upload') && $cgi->cgi_error) {
        printHeaderAndContent($cgi->cgi_error,undef,$cgi->cgi_error);   
        exit 0;
    }

    my($msg,$msgparam,$errmsg);
    my $redirtarget = $REQUEST_URI;
    $redirtarget =~s/\?.*$//; # remove query
    
    if ($ALLOW_FILE_MANAGEMENT && ($cgi->param('delete')||$cgi->param('rename')||$cgi->param('mkcol')||$cgi->param('changeperm'))) {
        debug("_POST: file management ".join(",",$cgi->param('file')));
        if ($cgi->param('delete')) {
            if ($cgi->param('file')) {
                my $count = 0;
                foreach my $file ($cgi->param('file')) {
                    debug("_POST: delete $PATH_TRANSLATED.$file");
                    if ($ENABLE_TRASH) {
                        moveToTrash($PATH_TRANSLATED.$file);
                    } else {
                        $count += deltree($PATH_TRANSLATED.$file, \my @err);
                    }
                    logger("DELETE($PATH_TRANSLATED) via POST");
                }
                if ($count>0) {
                    $msg= ($count>1)?'deletedmulti':'deletedsingle';
                    $msgparam="p1=$count";
                } else {
                    $errmsg='deleteerr'; 
                }
            } else {
                $errmsg='deletenothingerr';
            }
        } elsif ($cgi->param('rename')) {
            if ($cgi->param('file')) {
                if ($cgi->param('newname')) {
                    my @files = $cgi->param('file');
                    if (($#files > 0)&&(! -d $PATH_TRANSLATED.$cgi->param('newname'))) {
                        printHeaderAndContent('403 Forbidden','text/plain','403 Forbidden');
                        exit;
                    } else {
                        $msg='rename';
                        $msgparam = 'p1='.$cgi->escape(join(', ',@files))
                                  . ';p2='.$cgi->escape($cgi->param('newname'));
                        foreach my $file (@files) {
                            if (rmove($PATH_TRANSLATED.$file, $PATH_TRANSLATED.$cgi->param('newname'))) {
                                logger("MOVE($PATH_TRANSLATED,$PATH_TRANSLATED".$cgi->param('newname').") via POST");
                            } else {
                                $errmsg='renameerr';
                            }
                        }
                    }
                } else {
                    $errmsg='renamenotargeterr';
                }
            } else {
                $errmsg='renamenothingerr';
            }
        } elsif ($cgi->param('mkcol'))  {
            if ($cgi->param('colname')) {
                $msgparam="p1=".$cgi->escape($cgi->param('colname'));
                if (mkdir($PATH_TRANSLATED.$cgi->param('colname'))) {
                    logger("MKCOL($PATH_TRANSLATED".$cgi->param('colname').") via POST");
                    $msg='foldercreated';
                } else {
                    $errmsg='foldererr'; 
                    $msgparam.=';p2='.$cgi->escape(_tl($!));
                }
            } else {
                $errmsg='foldernothingerr';
            }
        } elsif ($cgi->param('changeperm')) {
            if ($cgi->param('file')) {
                my $mode = 0000;
                foreach my $userperm ($cgi->param('fp_user')) {
                    $mode = $mode | 0400 if $userperm eq 'r' && grep(/^r$/,@{$PERM_USER}) == 1;
                    $mode = $mode | 0200 if $userperm eq 'w' && grep(/^w$/,@{$PERM_USER}) == 1;
                    $mode = $mode | 0100 if $userperm eq 'x' && grep(/^x$/,@{$PERM_USER}) == 1;
                    $mode = $mode | 04000 if $userperm eq 's' && grep(/^s$/,@{$PERM_USER}) == 1;
                }
                foreach my $grpperm ($cgi->param('fp_group')) {
                    $mode = $mode | 0040 if $grpperm eq 'r' && grep(/^r$/,@{$PERM_GROUP}) == 1;
                    $mode = $mode | 0020 if $grpperm eq 'w' && grep(/^w$/,@{$PERM_GROUP}) == 1;
                    $mode = $mode | 0010 if $grpperm eq 'x' && grep(/^x$/,@{$PERM_GROUP}) == 1;
                    $mode = $mode | 02000 if $grpperm eq 's' && grep(/^s$/,@{$PERM_GROUP}) == 1;
                }
                foreach my $operm ($cgi->param('fp_others')) {
                    $mode = $mode | 0004 if $operm eq 'r' && grep(/^r$/,@{$PERM_OTHERS}) == 1;
                    $mode = $mode | 0002 if $operm eq 'w' && grep(/^w$/,@{$PERM_OTHERS}) == 1;
                    $mode = $mode | 0001 if $operm eq 'x' && grep(/^x$/,@{$PERM_OTHERS}) == 1;
                    $mode = $mode | 01000 if $operm eq 't' && grep(/^t$/,@{$PERM_OTHERS}) == 1;
                }

                $msg='changeperm';
                $msgparam=sprintf("p1=%04o",$mode);
                foreach my $file ($cgi->param('file')) {
                    changeFilePermissions($PATH_TRANSLATED.$file, $mode, $cgi->param('fp_type'), $ALLOW_CHANGEPERMRECURSIVE && $cgi->param('fp_recursive'));
                }
            } else {
                $errmsg='chpermnothingerr';
            }
        }
        print $cgi->redirect($redirtarget.createMsgQuery($msg,$msgparam, $errmsg, $msgparam));
    } elsif ($ALLOW_POST_UPLOADS && -d $PATH_TRANSLATED && defined $cgi->param('file_upload')) {
        my @filelist;
        foreach my $filename ($cgi->param('file_upload')) {
            next if $filename eq "";
            my $rfn= $filename;
            $rfn=~s/\\/\//g; # fix M$ Windows backslashes
            my $destination = $PATH_TRANSLATED.basename($rfn);
            debug("_POST: save $filename to $destination.");
            push(@filelist, basename($rfn));
            if (open(O,">$destination")) {
                while (read($filename,my $buffer,$BUFSIZE)>0) {
                    print O $buffer;
                }
                close(O);
            } else {
                printHeaderAndContent('403 Forbidden','text/plain','403 Forbidden');
                last;
            }
        }
        if ($#filelist>-1) {
            $msg=($#filelist>0)?'uploadmulti':'uploadsingle';
            $msgparam='p1='.($#filelist+1).';p2='.$cgi->escape(substr(join(', ',@filelist), 0, 150));
        } else {
            $errmsg='uploadnothingerr';
        }
        print $cgi->redirect($redirtarget.createMsgQuery($msg,$msgparam,$errmsg,$msgparam));
    } elsif ($ALLOW_ZIP_DOWNLOAD && defined $cgi->param('zip')) {
        my $zip =  Archive::Zip->new();     
        foreach my $file ($cgi->param('file')) {
            if (-d $PATH_TRANSLATED.$file) {
                $zip->addTree($PATH_TRANSLATED.$file, $file);
            } else {
                $zip->addFile($PATH_TRANSLATED.$file, $file);
            }
        }
        my $zfn = basename($PATH_TRANSLATED).'.zip';
        $zfn=~s/ /_/;
        print $cgi->header(-status=>'200 OK', -type=>'application/zip',-Content_disposition=>'attachment; filename='.$zfn);
        $zip->writeToFileHandle(\*STDOUT,0);
    } elsif ($ALLOW_ZIP_UPLOAD && defined $cgi->param('uncompress')) {
        my @zipfiles;
        foreach my $fh ($cgi->param('zipfile_upload')) {
            my $rfn= $fh;
            $rfn=~s/\\/\//g; # fix M$ Windows backslashes
            $rfn=basename($rfn);
            if (open(F,">$PATH_TRANSLATED$rfn")) {
                push @zipfiles, $rfn;
                print F $_ while (<$fh>);
                close(F);
                my $zip = Archive::Zip->new();
                my $status = $zip->read($PATH_TRANSLATED.$rfn);
                if ($status eq $zip->AZ_OK) {
                    $zip->extractTree(undef, $PATH_TRANSLATED);
                    unlink($PATH_TRANSLATED.$rfn);
                }
            }
        }
        if ($#zipfiles>-1) {
            $msg=($#zipfiles>0)?'zipuploadmulti':'zipuploadsingle';
            $msgparam='p1='.($#zipfiles+1).';p2='.$cgi->escape(substr(join(', ',@zipfiles), 0, 150));
        } else {
            $errmsg='zipuploadnothingerr';
        }
        print $cgi->redirect($redirtarget.createMsgQuery($msg,$msgparam,$errmsg,$msgparam));
        
    } elsif ($ENABLE_CALDAV_SCHEDULE && -d $PATH_TRANSLATED) {
        ## NOT IMPLEMENTED YET
    } else {
        debug("_POST: forbidden POST to $PATH_TRANSLATED");
        printHeaderAndContent('403 Forbidden','text/plain','403 Forbidden');
    }
*/    
    
}
