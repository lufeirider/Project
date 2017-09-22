<?php
function waf($url)
{
	/*
	安全狗
	http://172.16.10.210/
	http://172.16.10.210/?id=1%20union%20select%201,2,3,4

	云锁
	http://www.npxpf.com/
	http://www.aemedia.org/
	http://www.aemedia.org/?id=1%20union%20select%201,2,3,4

	腾讯云
	http://www.zhuojianchina.com/
	http://www.zhuojianchina.com/?id=1%20union%20select%201,2,3,4

	百度云
	https://su.baidu.com/
	https://su.baidu.com/?id=1%20union%20select%201,2,3,4

	阿里云
	https://yq.aliyun.com/
	https://www.itjuzi.com/
	https://yq.aliyun.com/?id=1%20union%20select%201,2,3,4

	加速乐
	https://www.yunaq.com/
	https://www.yunaq.com/?id=1%20union%20select%201,2,3,4

	360主机卫士
	http://zhuji.360.cn/
	http://zhuji.360.cn/?id=1%20union%20select%201,2,3,4
	*/

	//腾讯云
	//405 Not Allowed 您的访问可能会对网站造成危险，已被腾讯云安全拦截。

	//安全狗
	//网站防火墙 您提交的内容包含危险的攻击请求

	//云锁
	//网站防火墙 提交的请求含有不合法的参数,已被网站管理员设置拦截

	//百度云
	//友情提示 | 百度云加速

	//==========================================================================================

	//变量初始化
	$wafContent = array(
		safedog => "http://404.safedog.cn/images/safedogsite/broswer_logo.jpg", //安全狗
		yunsuo => ".yunsuologo{margin:0 auto; display:block; margin-top:20px;}",	//云锁
		tencent => "http://waf.tencent-cloud.com:8080/css/main.css", //腾讯云
		aliyun => "https://errors.aliyun.com/images/TB1TpamHpXXXXaJXXXXeB7nYVXX-104-162.png",	//阿里云
		baidu => "/cdn-cgi/styles/baidu.errors.css",	//百度云
		zhuji => "http://zhuji.360.cn/guard/firewall/stopattack.html",		//360
		yunaq => "https://www.yunaq.com/misinformation_upload/?from="		//加速乐
	);

	//	$payload = "?id=1%20union%20select%201,2,3,4";
	//	$payload = "/test.asp;.jpg";
	//	$pyaload = "/test.asp;.jpg?id=1%20and%201=1";
	//	$payload = "/test.asp;?id=1union%20select.jpg";
	//	$payload = "/test.asp;?id=1 and user() /*.jpg";
	//	$payload = "/test.asp%3b%3fid%3d1+and+user()+%2f*.jpg";


	$payload = "/?id=1%20union%20select%201,2,3,4";
	$result = "NULL";

	//==========================================================================================
	$url = $_GET['url'];


	$httpRule  = "/.*?\/\/.*?\//i";
	preg_match($httpRule,$url,$http);
	$http = $http[0];

	$url= $http.$payload; 


	$context = stream_context_create();
	stream_context_set_option($context, 'http', 'ignore_errors', true);
	$html = file_get_contents($url, false, $context); 


	foreach($wafContent as $k => $v)
	{
		if(strstr($html,$v))
		{
			$result = $k;
		}
	}

	return $result;
}
?>
