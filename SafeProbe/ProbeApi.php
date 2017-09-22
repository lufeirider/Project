<?php
include "cdn.php";
include "waf.php";

$url = $_GET['url'];
$cdnResult = cdn($url);
$wafResult = waf($url);


if( ($cdnResult=="NULL") && ($wafResult == "NULL") )
{
	echo "NULL";
}else
{
	if($waf == "NULL")
	{
		echo "WAF:NULL";
	}else{
		echo "WAF:".$wafResult;
	}
	
	echo "\r\n";
	
	if($cdnResult == "NULL")
	{
		echo "CDN:NULL";
	}else{
		echo "CDN:";
		foreach($cdnResult as $k => $v )
		{
			echo $v;
		}
	}
}
?>