package burp;

import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

import static burp.IScannerInsertionPoint.INS_PARAM_BODY;
import static burp.IScannerInsertionPoint.INS_PARAM_URL;

public class BurpExtender implements IBurpExtender, IScannerCheck
{
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private PrintWriter stdout;
    
    int time = 4;
    List<byte[]> payloads = new ArrayList<byte[]>(){{
        add(("11^sleep("+time+")#'^sleep("+time+")#\"^sleep("+time+")#'").getBytes());
    }};



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
        callbacks.setExtensionName("SQLi Scanner");

        // register ourselves as a custom scanner check
        callbacks.registerScannerCheck(this);
    }


    @Override
    public List<IScanIssue> doPassiveScan(IHttpRequestResponse baseRequestResponse) {

        return null;
    }

    @Override
    public List<IScanIssue> doActiveScan(IHttpRequestResponse baseRequestResponse, IScannerInsertionPoint insertionPoint) {
        boolean flag =  false;
        byte type = insertionPoint.getInsertionPointType();

        if(type != INS_PARAM_URL && type != INS_PARAM_BODY)
            return null;
        for (byte[] payload:payloads
             ) {
            flag =  false;
            long startTime = System.currentTimeMillis();
            byte[] checkRequest = insertionPoint.buildRequest(payload);
            IHttpRequestResponse checkRequestResponse = callbacks.makeHttpRequest(
                    baseRequestResponse.getHttpService(), checkRequest);


            if(System.currentTimeMillis() - startTime > time*1000)
            {
                flag = true;
            }

            if (flag)
            {
                // get the offsets of the payload within the request, for in-UI highlighting
                List<int[]> requestHighlights = new ArrayList<>(1);
                requestHighlights.add(insertionPoint.getPayloadOffsets(payload));


                //stdout.println(matches.toString());
                // report the issue
                List<IScanIssue> issues = new ArrayList<>(1);
                issues.add(new CustomScanIssue(
                        baseRequestResponse.getHttpService(),
                        helpers.analyzeRequest(baseRequestResponse).getUrl(),
                        new IHttpRequestResponse[] { callbacks.applyMarkers(checkRequestResponse, requestHighlights, null) },
                        "time-based blind sql inject",
                        "payload: " + helpers.bytesToString(payload),
                        "High"));
                return issues;
            }
        }
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