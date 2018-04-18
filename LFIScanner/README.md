burp检测LFI插件，相比上一个插件来说，优化了很多，写上一版本的时候，文档的内容没仔细看没写好，做了无用的扫描，比如burp检测的时候，还把payload插入到cookies中去了。
除了优化扫描请求，还增加了PHP报错信息的检测。比如当有报错的时候，有报错Warning: include() [function.include]信息，也会被爆存在漏洞。
