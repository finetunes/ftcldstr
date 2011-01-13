package net.finetunes.ftcldstr.routines.fileoperations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.finetunes.ftcldstr.rendering.RenderingService;
import net.finetunes.ftcldstr.routines.webdav.QueryService;
import net.finetunes.ftcldstr.wrappers.ReadDirectoryContentWrapper;
import net.finetunes.ftcldstr.wrappers.ReadDirectoryResult;

public class DirectoryOperationsService {
	
	// TODO: hrefs array of strings (filenames) recursively passed by reference
	public static void readDirBySuffix(String filename, 
			String baseName, 
			// $hrefs, 
			String suffix, 
			int depth, 
			HashMap visited) {
		
		// TODO: implement
	}
	
	public static Object[] getFolderList(String fn, String ru, String filter) {
		
	    
	    // PZ: sample code
	    
        System.out.println("READDIR: " + fn);
        System.out.println("READDIR: " + ru);
	    
        ReadDirectoryContentWrapper rdw = new ReadDirectoryContentWrapper();
	    ReadDirectoryResult d = rdw.readDirectory(fn);
	    List<String> dc = d.getContent();
	    Iterator<String> it = dc.iterator();
	    while (it.hasNext()) {
	        System.out.println(it.next());
	    }
	    
	    // -------------------
	    
	    String content = "";
	    int list = 0;
	    int count = 0;
	    int filecount = 0;
	    int filesizes = 0;
	    
        content += "<h2 style=\"border:0; padding:0; margin:0\">";
        content += "<a href=\"" + ru + "?action=props\">";
        // TODO: // > content += $cgi->img({-src=>$ICONS{'< folder >'} || $ICONS{default},-style=>'border:0',-title=>_tl('showproperties'), -alt=>'folder'})
        content += "</a>";
        // TODO: // > content += '&nbsp;'.$cgi->a({-href=>'?action=davmount',-style=>'font-size:0.8em;color:black',-title=>_tl('mounttooltip')},_tl('mount'))
        content += " ";
        content += RenderingService.getQuickNavPath(ru, QueryService.getQueryParams());
        content += "</h2>";
	    
/*

    if ($SHOW_QUOTA) {
        my ($ql, $qu) = getQuota($fn);
        if (defined $ql && defined $qu) {
            $ql=$ql/1048576; $qu=$qu/1048576;
            $content .= $cgi->div({style=>'padding-left:30px;font-size:0.8em;'},
                            _tl('quotalimit')."${ql} MB,"
                            ._tl('quotaused')."${qu} MB,"
                            ._tl('quotaavailable').($ql-$qu)." MB");
        }
    }
    $list = "\n";
    $list.=$cgi->checkbox(-onclick=>'javascript:this.checked=false; var ea = document.getElementsByName("file"); for (var i=0; i<ea.length; i++) ea[i].checked=!ea[i].checked;', -name=>'selectall',-value=>"",-label=>"", -title=>_tl('togglealltooltip')) if $ALLOW_FILE_MANAGEMENT;
    my $order = $cgi->param('order') || 'name';
    my $dir = $order=~/_desc$/ ? '' : '_desc';
    my $query = "showall=".$cgi->param('showall') if $cgi->param('showall');
    $list.=$cgi->span({-style=>'font-weight:bold;padding-left:20px'},
            ' '.$cgi->a({-href=>"$ru?order=name$dir;$query",-style=>'color:black'},_tl('names'))
                .(' ' x ($MAXFILENAMESIZE-length(_tl('names'))+1)  )
            .$cgi->a({-href=>"$ru?order=lastmodified$dir;$query",-style=>'color:black'},_tl('lastmodified')) 
                .(' ' x ($MAXLASTMODIFIEDSIZE-length(_tl('lastmodified'))+1))
            . (' ' x ($MAXSIZESIZE-length(_tl('size')))).$cgi->a({-href=>"$ru?order=size$dir;$query",-style=>'color:black'},_tl('size'))  
            .($SHOW_PERM?' ' . $cgi->a({-href=>"$ru?order=mode$dir;$query",-style=>'color:black'},sprintf("%-11s",_tl('permissions'))):'')
            . ' '
            . $cgi->a({-href=>"$ru?order=mime$dir;$query",-style=>'color:black'},_tl('mimetype'))
            ."\n"
        );
    $list.=$cgi->checkbox(-name=>'hidden',-value=>"",-label=>"", -disabled=>'disabled', -style=>'visibility:hidden') 
          .getfancyfilename(dirname($ru).'/','..','< .. >',dirname($fn))."\n" unless $fn eq $DOCUMENT_ROOT || $ru eq '/' || $filter;

    my @files;
    if (opendir(DIR,$fn)) {
        @files = sort cmp_files grep { !/^(\.|\.\.)$/ } readdir(DIR);
        closedir(DIR);
    }
    my $page = $cgi->param('page') ? $cgi->param('page') - 1 : 0;
    my $fullcount = $#files + 1;
    if (!defined $filter && defined $PAGE_LIMIT && !defined $cgi->param('showall')) {
        splice(@files, $PAGE_LIMIT * ($page+1) );
        splice(@files, 0, $PAGE_LIMIT * $page) if $page>0;
    }

    eval qq@/$filter/;@;
    $filter="\Q$filter\E" if ($@);

    foreach my $filename (@files) {
        my $full = $fn.$filename;
        next if is_hidden($full);
        my $mimetype = -d $full ? '< folder >' : getMIMEType($filename);
        my $nru = $ru.uri_escape($filename);
        $filename.="/" if -d $full;
        $nru.="/" if -d $full;

        next if $filter && $filename !~/$filter/i;

        my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size, $atime,$mtime,$ctime,$blksize,$blocks) = stat($full);
        
        $list.= $cgi->checkbox(-name=>'file', -value=>$filename, -label=>'') if $ALLOW_FILE_MANAGEMENT;

        my $lmf = strftime(_tl('lastmodifiedformat'), localtime($mtime));
        $list.= getfancyfilename($nru,$filename,$mimetype, $full)
            .sprintf(" %-${MAXLASTMODIFIEDSIZE}s %${MAXSIZESIZE}d", $lmf, $size)
            . ($SHOW_PERM? ' '. $cgi->span({-style=>getmodecolors($full,$mode),-title=>sprintf("mode: %04o, uid: %s (%s), gid: %s (%s)",$mode & 07777,"".getpwuid($uid), $uid, "".getgrgid($gid), $gid)},sprintf("%-11s",mode2str($full,$mode))): '')   
            . ' '. $cgi->escapeHTML($mimetype)
            ."\n";

        $count++;
        $foldercount++ if -d $full;
        $filecount++ if -f $full;
        $filesizes+=$size if -f $full;
    }
    my $pagenav = $filter ? '' : getPageNavBar($ru, $fullcount);
    $content.=$cgi->start_multipart_form(-onsubmit=>'return window.confirm("'._tl('confirm').'");') if $ALLOW_FILE_MANAGEMENT;
    $content .= $pagenav;
    $content .= $cgi->pre({-style=>'overflow:auto;'}, $list); 
    $content .= $cgi->div({-style=>'font-size:0.8em'},sprintf("%s %d, %s %d, %s %d, %s %d Bytes (= %.2f KB = %.2f MB = %.2f GB)", _tl('statfiles'), $filecount, _tl('statfolders'), $foldercount, _tl('statsum'), $count, _tl('statsize'), $filesizes, $filesizes/1024, $filesizes/1048576, $filesizes/1073741824)) if ($SHOW_STAT); 

    $content .= $pagenav;
    return ($content, $count);
*/
        // TODO: implement
        return new Object[]{content, count};
		
	}
	
    public static Object[] getFolderList(String fn, String ru) {
     
        return getFolderList(fn, ru, null);
    }	
	
	public static int getDirInfo(String filename, String propertyName) {
		
		// TODO: implement
		return -1;
		
	}
	
	// TODO: params:
	// $fn, $ru, $respsRef, $props, $all, $noval, $depth, $noroot, $visited
	public static void readDirRecursive() {
		
		// TODO: implement
		
	}

}
