<?php
	$conn = mysql_connect("localhost","root","root");
	mysql_select_db("test");
	mysql_query("set names utf8");
	
	
	$url = "http://www.yangmingcapital.com/"; 
	$contents = file_get_contents($url);
	
	//图片路径
	$picRule  = "/src=\W(\S*?(?:\.jpg|png|gif))/i";
	if(preg_match($picRule,$contents))
	{
		preg_match_all($picRule,$contents,$aPic);
		$aPic = $aPic[1];
		$aPic = array_unique($aPic);
	}
	
	//css样式文件路径
	$cssRule  = "/href=\W(\S*?\.css)/i";
	if(preg_match($cssRule,$contents))
	{
		preg_match_all($cssRule,$contents,$aCss);
		$aCss = $aCss[1];
		$aCss = array_unique($aCss);
	}
	
	
	//文件路径处理
	$aspRule  = "/a.*?href=\W(\S*?)\"|'/i";
	if(preg_match($aspRule,$contents))
	{
		preg_match_all($aspRule,$contents,$aAsp);
		$aAsp = $aAsp[1];
		$aAsp = array_unique($aAsp);
	}
	
	foreach($aAsp as $k => $v)
	{
		$aAsp[$k] = substr($v, 0, strrpos($v,"/"))."/";
		if(strstr($aAsp[$k],"http:") || empty($aAsp[$k]) || $aAsp[$k]=="/")
		{
			unset($aAsp[$k]);
		}
	}
	$aAsp = array_unique($aAsp);
	/////////////////////////////////////////


	$aPath = array_merge($aPic,$aCss,$aAsp);
	
	
	//print_r($aPath);die;
	$aResult = array();

	$re = mysql_query("select * from Cms");
	
	$t1 = microtime(true);
	while($row = mysql_fetch_array($re))
	{
		$path = $row["path"];
		$name = $row["name"];
		
		foreach($aPath as $k => $v)
		{
			if(strstr($path,$v))
			{
				if(empty($aResult[$name]))
				{
					$aResult[$name] = array();
					array_push($aResult[$name],$v);
				}else
				{
					if(!in_array($v,$aResult[$name]))
					{
						array_push($aResult[$name],$v);
					}
					
				}
				
			}
		}
	}
	$t2 = microtime(true);
	echo '耗时'.round($t2-$t1,3).'秒';
	die;
	$max = 0;
	foreach($aResult as $k => $v)
	{
		if(count($v) > $max)
		{
			$max = count($v);
		}
	}
	
	function test($var)
	{
		return count($var)==3;
	}
	
	$aResult = array_filter($aResult,"test");
	
	print_r($aResult);
	// foreach($aName as $k => $v )
	// {
		// echo $k . ":" . $v . "<br/>";
	// }
	
?>