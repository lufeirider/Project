<!DOCTYPE html>
<html>
<head>
<title>CMS在线识别</title>
<link rel="stylesheet" href="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/css/bootstrap.min.css">
<script src="http://cdn.static.runoob.com/libs/jquery/2.1.1/jquery.min.js"></script>
<script src="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<link href="main.css" rel="stylesheet">
</head>

<body>
    <div class="container">
	<form class="form-signin" action="" method="GET">
		<div align="center" style="margin:5%"><h2>CMS在线识别</h2></div>
		<div class="row">
			<div class="col-xs-10" style="padding:0;margin:0">
				<input name="url" class="form-control" placeholder="http://www.xxx.com" required autofocus>
			</div>
			
			<div class="col-xs-2" style="padding-right: 0%;">
				<button class="btn btn-md btn-primary btn-block" type="submit">识 别</button>
			</div>
		</div>
	</form>

<?php
header("Content-type: text/html; charset=gb2312"); 
//print_r($_GET);
if(!empty($_GET["url"]))
{
	$sOutPut = shell_exec("python3 CmsIdentify2.py ".escapeshellarg($_GET["url"]));
	//print_r($sOutPut);
	if($sOutPut!="NULL")
	{
		$sAllRule  = "/\{(.*?)\}/i";
		preg_match_all($sAllRule,$sOutPut,$info);
		//print_r($info);die;
		$nPage = 1;
		foreach($info[1] as $k => $v)
		{
			//[岁月工作室通用企业网站系统 3.3,3]['gbook/', 'favicon.ico', 'inc/aspcms_statistics.asp']

			$sitemsrule  = "/\[(.*?)\]/i";
			preg_match_all($sitemsrule,$v,$items);
			

			$sName = $items[1][0];
			$sUrl = $items[1][1];
			
			$aName = explode(",",$sName);
			
			echo '	
					<div class="panel panel-default">
					<div class="panel-heading">
						<h4 class="panel-title">
							<a data-toggle="collapse" data-parent="#accordion" 
							   href="#collapse'.$nPage.'">
							   '.$aName[0].'('. $aName[1] .')
							</a>
						</h4>
					</div>
					<div id="collapse'.$nPage.'" class="panel-collapse collapse ';if($nPage==1){echo 'in';}echo '">
			
			';
			$nPage = $nPage + 1;


			$aUrl = explode(",",$sUrl);
			foreach($aUrl as $url)
			{
				echo '
					<p style="text-indent:1em;padding:0;margin:0.5%">  

					'.str_replace("'","",$url).'

					</p>
					<hr style="padding:0;margin:0"/>
				';
			}
			
			echo '</div></div>';
		}
		echo '
			</div>
		</div>
	</div>
		';
	}

}

?>
    </div> <!-- /container -->
</body>

</html>