看到一个很老的cookie修改器的插件，余弦写的一款插件 [CookieHacker](http://evilcos.me/?p=366 "CookieHacker") 能够在浏览器上修改cookie，挺方便的。以前一般都是拿其他的软件进行修改的，因为在浏览器上没有很方便进行修改的插件。

修正：在使用的过程中，会出现cookie无效的情况（cookie还是有效的）。通过抓包分析，原来是使用插件进行修改后，会出现两个cookie key 重名存在的情况。

新添小功能：能够对特定的网站的cookie进行删除，主要是有时候收到多个账户的cookie，能够方便切换cookie进行测试，添加的js代码如下。
