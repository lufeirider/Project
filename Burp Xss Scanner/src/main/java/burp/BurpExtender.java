package burp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import java.util.regex.*;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import static burp.IScannerInsertionPoint.INS_PARAM_BODY;
import static burp.IScannerInsertionPoint.INS_PARAM_URL;

public class BurpExtender implements IBurpExtender, IScannerCheck
{
    private static PrintWriter stdout;
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    public static boolean isFirst = true;
    public static Integer count = 1;


    byte[] payload = "111000'\"<ttt'\"ttt>".getBytes();



    //
    // implement IBurpExtender
    //
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {

        // obtain our output and error streams
        stdout = new PrintWriter(callbacks.getStdout(), true);

        // write a message to our output stream
        stdout.println("Author:lufei");


        // keep a reference to our callbacks object
        this.callbacks = callbacks;

        // obtain an extension helpers object
        helpers = callbacks.getHelpers();

        // set our extension name
        callbacks.setExtensionName("Xss Scanner");

        // register ourselves as a custom scanner check
        callbacks.registerScannerCheck(this);
    }


    @Override
    public List<IScanIssue> doPassiveScan(IHttpRequestResponse baseRequestResponse) {
        count = 0;
        return null;
    }


    @Override
    public List<IScanIssue> doActiveScan(IHttpRequestResponse baseRequestResponse, IScannerInsertionPoint insertionPoint) throws IOException {
        Pattern pt;
        Matcher mt;
        String body;
        boolean isMatch;

        isFirst = true;

        //由于使用htmlunit，而不适用burp的api发送，只能自己重写。getComment我把他重写为获取url。
        IHttpRequestResponse checkRequestResponse = new IHttpRequestResponse() {
            byte[] request;
            byte[] response;
            String url;
            IHttpService service;

            @Override
            public byte[] getRequest() {
                return request;
            }

            @Override
            public void setRequest(byte[] message) {
                request = message;
            }

            @Override
            public byte[] getResponse() {
                return response;
            }

            @Override
            public void setResponse(byte[] message) {
                response = message;
            }

            @Override
            public String getComment() {
                return url;
            }

            @Override
            public void setComment(String comment) {
                url = comment;
            }

            @Override
            public String getHighlight() {
                return null;
            }

            @Override
            public void setHighlight(String color) {

            }

            @Override
            public IHttpService getHttpService() {
                return service;
            }

            @Override
            public void setHttpService(IHttpService httpService) {
                service = httpService;
            }

        };

        //判断payload插入的类型
        byte type = insertionPoint.getInsertionPointType();

        //xss只检测url参数和body参数
        if(type != INS_PARAM_URL && type != INS_PARAM_BODY)
            return null;


        byte[] checkRequest = insertionPoint.buildRequest(payload);

        //checkRequestResponse设置请求报文
        checkRequestResponse.setRequest(checkRequest);


        //获取完整的URL，并且checkRequestResponse设置URL
        pt = Pattern.compile("/.*?(?=HTTP)");
        mt = pt.matcher(new String(checkRequest));
        mt.find();
        String testPath = mt.group(0);

        //下面会报错，不能这样获取url
        // java.lang.UnsupportedOperationException: This IRequestInfo object was created without any HTTP service details, so the full request URL is not available. To obtain the full URL, use one of the other overloaded methods in IExtensionHelpers to analyze the request.
        //stdout.println(helpers.analyzeRequest(checkRequestResponse.getRequest()).getUrl());

        String testUrl = baseRequestResponse.getHttpService().getProtocol()+"://"+baseRequestResponse.getHttpService().getHost()+":"+baseRequestResponse.getHttpService().getPort()+mt.group(0);


        if(count == 0)
        {
            testUrl = testUrl + "#" + new String(payload);
            count = 1;
        }
        checkRequestResponse.setComment(testUrl);


        //checkRequestResponse设置setHttpService
        checkRequestResponse.setHttpService(baseRequestResponse.getHttpService());


        //设置请求选项
        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        new WebConnectionListener(webClient);


        //使用htmlunit发送数据包
        URL url = new URL(testUrl);

        WebRequest request = new WebRequest(url);
        if( type == INS_PARAM_BODY)
        {
            request = new WebRequest(url,HttpMethod.POST);

            //stdout.println(new String(checkRequest));
            pt = Pattern.compile("^\\s+(.*)",Pattern.MULTILINE);
            mt = pt.matcher(new String(checkRequest));
            mt.find();
            body = mt.group(1);
            request.setRequestBody(body);


            //stdout.println(body);
        }


        for (String head:helpers.analyzeRequest(baseRequestResponse.getRequest()).getHeaders()
                ) {
            if (head.contains(":")&&!head.contains("Content-Length"))
            {
                //stdout.println(head.split(":")[0]+":" + head.split(":")[1]);
                request.setAdditionalHeader(head.split(":")[0],head.split(":")[1]);

            }
        }

        try {

            HtmlPage page = webClient.getPage(request);

            //设置为带payload的返回包。
            String response = "";
            for (String tmp:helpers.analyzeResponse(baseRequestResponse.getResponse()).getHeaders()
                    ) {
                response = response + tmp + "\n";
            }
            response = response + "\n\n" + page.asXml();
            checkRequestResponse.setResponse(response.getBytes());


            //stdout.println("正常：\n"+page.asXml());
            return checkXss(page,checkRequestResponse,insertionPoint);

        }catch (ScriptException e)
        {
            //stdout.println("异常：\n"+ e.getPage().asXml());
            return checkXss(e.getPage(),checkRequestResponse,insertionPoint);
        }


    }

    @Override
    public int consolidateDuplicateIssues(IScanIssue existingIssue, IScanIssue newIssue) {
        if (existingIssue.getIssueName().equals(newIssue.getIssueName()))
            return -1;
        else return 0;
    }

    // helper method to search a response for occurrences of a literal match string
    // and return a list of start/end offsets
    private List<int[]> getMatches(byte[] response, byte[] match)
    {
        List<int[]> matches = new ArrayList<int[]>();

        int start = 0;
        while (start < response.length)
        {
            start = helpers.indexOf(response, match, true, start, response.length);
            if (start == -1)
                break;
            matches.add(new int[] { start, start + match.length });
            start += match.length;
        }

        return matches;
    }

    //返回问题
    public List<IScanIssue> returnIssues(IHttpRequestResponse checkRequestResponse,List<int[]> requestHighlights,List<int[]> matches,String detail) throws MalformedURLException {
        List<IScanIssue> issues = new ArrayList<IScanIssue>(1);
        issues.add(new CustomScanIssue(
                checkRequestResponse.getHttpService(),
                new URL(checkRequestResponse.getComment()),
                new IHttpRequestResponse[] { callbacks.applyMarkers(checkRequestResponse, requestHighlights, matches) },
                "xss",
                detail,
                "Medium"));
        return issues;
    }

    //对返回包进行正则匹配，是否存在xss
    public List<IScanIssue> checkXss(HtmlPage page,IHttpRequestResponse checkRequestResponse,IScannerInsertionPoint insertionPoint) throws MalformedURLException {
        Pattern pt;
        Matcher mt;
        boolean isMatch;
        List<int[]> matches;
        List<int[]> requestHighlights = new ArrayList<int[]>(1);
        requestHighlights.add(insertionPoint.getPayloadOffsets(payload));


        //获取script里面的js
        pt = Pattern.compile("//<!\\[CDATA\\[(.*?)//\\]\\]>",Pattern.DOTALL);
        mt = pt.matcher(page.asXml());
        List<String> scriptList = new ArrayList<String>();
        while (mt.find()) {
            scriptList.add(mt.group());
        }

//        for (String tmp:scriptList
//                ) {
//            System.out.println(tmp);
//        }

        //stdout.println(page.asXml());
        pt = Pattern.compile("<ttt(.*?)'\"ttt(.*?)>");
        mt = pt.matcher(page.asXml());
        isMatch = mt.find();
        if(isMatch)
        {
            matches =  getMatches(checkRequestResponse.getResponse(), mt.group(0).getBytes());
            return returnIssues(checkRequestResponse,requestHighlights,matches,"xss: '  \"  <  > all is available");
        }

        //html 逃逸分号
        pt = Pattern.compile("<(.*?)ttt(.*?)'(.*?)ttt(.*?)=\"\"(.*?)>");
        mt = pt.matcher(page.asXml());
        isMatch = mt.find();
        if(isMatch)
        {
            matches =  getMatches(checkRequestResponse.getResponse(), mt.group(0).getBytes());
            return returnIssues(checkRequestResponse,requestHighlights,matches,"html-content xss: the separator is ',but can escape.");
        }

        //html 逃逸单引号
        pt = Pattern.compile("<(.*?)ttt(.*?)\"(.*?)ttt(.*?)=\"\"(.*?)>");
        mt = pt.matcher(page.asXml());
        isMatch = mt.find();
        if(isMatch)
        {
            matches =  getMatches(checkRequestResponse.getResponse(), mt.group(0).getBytes());
            return returnIssues(checkRequestResponse,requestHighlights,matches,"html-content xss: the separator is \",but can escape.");
        }

        //检测js中逃逸单引号的情况
        pt = Pattern.compile("\"(.*?)ttt(.*?)\"(.*?)ttt(.*?)\"");
        mt = pt.matcher(page.asXml());
        isMatch = mt.find();
        if(isMatch)
        {
            matches =  getMatches(checkRequestResponse.getResponse(), mt.group(0).getBytes());
            return returnIssues(checkRequestResponse,requestHighlights,matches,"js-content xss:the separator is \",but can escape.");
        }


        //检测js中逃逸分号的情况
        pt = Pattern.compile("'(.*?)ttt(.*?)'(.*?)ttt(.*?)'");
        mt = pt.matcher(page.asXml());
        isMatch = mt.find();
        if(isMatch)
        {
            matches =  getMatches(checkRequestResponse.getResponse(), mt.group(0).getBytes());
            return returnIssues(checkRequestResponse,requestHighlights,matches,"js-content xss:the separator is ',but can escape.");
        }

        //检测js中没有分隔符符号的情况，var a = test
        pt = Pattern.compile("(.*?)111000(.*?)ttt(.*?)ttt");
        mt = pt.matcher(page.asXml());
        isMatch = mt.find();
        if(isMatch)
        {
            if(scriptList.toString().contains(mt.group(0)) && !mt.group(0).substring(0,mt.group(0).indexOf("111000")).contains("'") && !mt.group(0).substring(0,mt.group(0).indexOf("111000")).contains("\""))
            {
                matches =  getMatches(checkRequestResponse.getResponse(), mt.group(0).getBytes());
                return returnIssues(checkRequestResponse,requestHighlights,matches,"js-content xss:no separator");
            }
        }

        pt = Pattern.compile("(a|frame)(.*?)(href|src)=\"111000");
        mt = pt.matcher(page.asXml());
        isMatch = mt.find();
        if(isMatch)
        {
            matches =  getMatches(checkRequestResponse.getResponse(), mt.group(0).getBytes());
            return returnIssues(checkRequestResponse,requestHighlights,matches,"javascript:alert()");
        }

        pt = Pattern.compile("eval(.*?)111000");
        mt = pt.matcher(page.asXml());
        isMatch = mt.find();
        if(isMatch)
        {
            matches =  getMatches(checkRequestResponse.getResponse(), mt.group(0).getBytes());
            return returnIssues(checkRequestResponse,requestHighlights,matches,"notice eval()");
        }

        pt = Pattern.compile("settimeout(.*?)111000");
        mt = pt.matcher(page.asXml());
        isMatch = mt.find();
        if(isMatch)
        {
            matches =  getMatches(checkRequestResponse.getResponse(), mt.group(0).getBytes());
            return returnIssues(checkRequestResponse,requestHighlights,matches,"notice settimeout()");
        }

        pt = Pattern.compile("location(.*?)111000");
        mt = pt.matcher(page.asXml());
        isMatch = mt.find();
        if(isMatch)
        {
            matches =  getMatches(checkRequestResponse.getResponse(), mt.group(0).getBytes());
            return returnIssues(checkRequestResponse,requestHighlights,matches,"notice location()");
        }

        return null;
    }


    public static void debug(String s)
    {
        stdout.println(s);
    }

}

//
// class implementing IScanIssue to hold our custom scan issue details
//
class CustomScanIssue implements IScanIssue
{
    private IHttpService httpService;
    private URL url;
    private IHttpRequestResponse[] httpMessages;
    private String name;
    private String detail;
    private String severity;

    public CustomScanIssue(
            IHttpService httpService,
            URL url,
            IHttpRequestResponse[] httpMessages,
            String name,
            String detail,
            String severity)
    {
        this.httpService = httpService;
        this.url = url;
        this.httpMessages = httpMessages;
        this.name = name;
        this.detail = detail;
        this.severity = severity;
    }

    @Override
    public URL getUrl()
    {
        return url;
    }

    @Override
    public String getIssueName()
    {
        return name;
    }

    @Override
    public int getIssueType()
    {
        return 0;
    }

    @Override
    public String getSeverity()
    {
        return severity;
    }

    @Override
    public String getConfidence()
    {
        return "Certain";
    }

    @Override
    public String getIssueBackground()
    {
        return null;
    }

    @Override
    public String getRemediationBackground()
    {
        return null;
    }

    @Override
    public String getIssueDetail()
    {
        return detail;
    }

    @Override
    public String getRemediationDetail()
    {
        return null;
    }

    @Override
    public IHttpRequestResponse[] getHttpMessages()
    {
        return httpMessages;
    }

    @Override
    public IHttpService getHttpService()
    {
        return httpService;
    }


}