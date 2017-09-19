#coding=utf-8 
import re

text = r"""
GdiPlus.dll	Microsoft GDI+	Microsoft Corporation	C:\Windows\winsxs\x86_microsoft.windows.gdiplus_6595b64144ccf1df_1.1.7601.17514_none_72d18a4386696c80\GdiPlus.dll
comctl32.dll	用户体验控件库	Microsoft Corporation	C:\Windows\winsxs\x86_microsoft.windows.common-controls_6595b64144ccf1df_6.0.7601.17514_none_41e6975e2bd6f2b2\comctl32.dll
msgsm32.acm.mui	Microsoft GSM 6.10 Audio CODEC for MSACM	Microsoft Corporation	C:\Windows\SysWOW64\zh-CN\msgsm32.acm.mui
msg711.acm.mui	Microsoft CCITT G.711 (A-Law and u-Law) CODEC for MSACM	Microsoft Corporation	C:\Windows\SysWOW64\zh-CN\msg711.acm.mui
l3codeca.acm.mui	MPEG Layer-3 Audio Codec for MSACM	Fraunhofer Institut Integrierte Schaltungen IIS	C:\Windows\SysWOW64\zh-CN\l3codeca.acm.mui
"""
lists = []
m = re.findall(r"\w+\.dll", text)
for i in m:
    lists.append(i)

lists = list(set(lists))

for i in lists:
	print i
