<?php
function cdn($url)
{
	//变量初始化
	//==========================================================================================
	$cdnHeader = array(
		'via',
		'x-via',
		'by-360wzb',
		'by-anquanbao',
		'cc_cache',
		'cdn cache server',
		'cf-ray',
		'chinacache',
		'verycdn',
		'webcache',
		'x-cacheable',
		'x-fastly',
		'yunjiasu'
	);

	$cdnCname = array(
		'tbcache.com',
		'tcdn.qq.com',
		'00cdn.com',
		'21cvcdn.com',
		'21okglb.cn',
		'21speedcdn.com',
		'21vianet.com.cn',
		'21vokglb.cn',
		'360wzb.com',
		'51cdn.com',
		'acadn.com',
		'aicdn.com',
		'akadns.net',
		'akamai-staging.net',
		'akamai.com',
		'akamai.net',
		'akamaitech.net',
		'akamaized.net',
		'alicloudlayer.com',
		'alikunlun.com',
		'aliyun-inc.com',
		'aliyuncs.com',
		'amazonaws.com',
		'anankecdn.com.br',
		'aodianyun.com',
		'aqb.so',
		'awsdns',
		'azioncdn.net',
		'azureedge.net',
		'bdydns.com',
		'bitgravity.com',
		'cachecn.com',
		'cachefly.net',
		'ccgslb.com',
		'ccgslb.net',
		'cdn-cdn.net',
		'cdn.cloudflare.net',
		'cdn.dnsv1.com',
		'cdn.ngenix.net',
		'cdn20.com',
		'cdn77.net',
		'cdn77.org',
		'cdnetworks.net',
		'cdnify.io',
		'cdnnetworks.com',
		'cdnsun.net',
		'cdntip.com',
		'cdnudns.com',
		'cdnvideo.ru',
		'cdnzz.net',
		'chinacache.net',
		'chinaidns.net',
		'chinanetcenter.com',
		'cloudcdn.net',
		'cloudfront.net',
		'customcdn.cn',
		'customcdn.com',
		'dnion.com',
		'dnspao.com',
		'edgecastcdn.net',
		'edgesuite.net',
		'ewcache.com',
		'fastcache.com',
		'fastcdn.cn',
		'fastly.net',
		'fastweb.com',
		'fastwebcdn.com',
		'footprint.net',
		'fpbns.net',
		'fwcdn.com',
		'fwdns.net',
		'globalcdn.cn',
		'hacdn.net',
		'hadns.net',
		'hichina.com',
		'hichina.net',
		'hwcdn.net',
		'incapdns.net',
		'internapcdn.net',
		'jiashule.com',
		'kunlun.com',
		'kunlunar.com',
		'kunlunca.com',
		'kxcdn.com',
		'lswcdn.net',
		'lxcdn.com',
		'lxdns.com',
		'mwcloudcdn.com',
		'netdna-cdn.com',
		'okcdn.com',
		'okglb.com',
		'ourwebcdn.net',
		'ourwebpic.com',
		'presscdn.com',
		'qingcdn.com',
		'qiniudns.com',
		'skyparkcdn.net',
		'speedcdns.com',
		'sprycdn.com',
		'tlgslb.com',
		'txcdn.cn',
		'txnetworks.cn',
		'ucloud.cn',
		'unicache.com',
		'verygslb.com',
		'vo.llnwd.net',
		'wscdns.com',
		'wscloudcdn.com',
		'xgslb.net',
		'ytcdn.net',
		'yunjiasu-cdn'
	);

	$result = array();

	//http://v.ifeng.com/
	//第一种是通过获取cname来判断是否存在cdn，第二种是通过header来判断是否存在cdn，如果第一个方法没有找到cdn，flag不改变，再使用第二种办法。
	$flag = 0;

	$httpRule  = "/.*?\/\/.*?\//i";
	preg_match($httpRule,$url,$http);
	$http = $http[0];


	$domainRule = "/(?<=\/\/).*?(?=\/)/";
	preg_match($domainRule,$url,$domain);
	$domain = $domain[0];

	//==========================================================================================

	//==========================================================================================
	//通过cname判断是否存在cdn
	exec('nslookup -qt=cname '.$domain.' 114.114.114.114 | findstr "canonical name"', $cname);
	$cname = strrchr($cname[0], " canonical name = ");


	foreach($cdnCname as $v)
	{
		if(strstr($cname, $v))
		{
			$result["Status"] = "exist:True,";
			$result["KEYWORD"] = "cname:".$v;
			
			$flag = 1;
			break;
		}
	}

	//==========================================================================================


	//==========================================================================================
	//通过获取header头判断是否存在cdn
	if($flag == 0)
	{
		$headerArr = get_headers($http, 1);
		$headerStr = "";
		foreach($headerArr as $k => $v)
		{
			$headerStr = $headerStr . $k;
		}
		
		foreach($cdnHeader as $v)
		{
			if(strstr(strtolower($headerStr), $v))
			{
				$result["Status"] = "exist:True,";
				$result["KEYWORD"] = "header:".$v;
				
				break;
			}
		}
	}

	if($result["Status"] == NULL)
	{
		return "NULL";
	}
	else
	{
		return $result;
	}
}


?>