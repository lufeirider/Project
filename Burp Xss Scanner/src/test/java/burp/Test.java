package burp;


import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.util.FalsifyingWebConnection;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static boolean isFirst = true;

    @org.junit.Test
    public void CheckHtmlXss() throws IOException {
        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);

        //获取页面
        try
        {
            String url ="http://127.0.0.1/test2.php?url=111000'\"<ttt'\"ttt>";
            HtmlPage page = webClient.getPage(url);

            System.out.println(page.asXml());
        } catch (ScriptException e) {
            System.out.println(e.getFailingLine());
            System.out.println(e.getPage().asXml());
        }
        webClient.close();
    }


    @org.junit.Test
    public void CheckJsXss() throws IOException {
        Pattern pt;
        Matcher mt;
        Boolean isMatch;

        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);

        //获取页面
        try
        {
            String url ="http://127.0.0.1/test3.php?url=000111\"'<ttt'\"ttt>";

            HtmlPage page = webClient.getPage(url);


            pt = Pattern.compile("//<!\\[CDATA\\[(.*?)//\\]\\]>",Pattern.DOTALL);
            mt = pt.matcher(page.asXml());
            List<String> scriptList = new ArrayList<String>();
            while (mt.find()) {
                scriptList.add(mt.group());
            }

            for (String tmp:scriptList
                 ) {
                System.out.println(tmp);
            }

            pt = Pattern.compile("(.*?)000111(.*?)ttt(.*?)ttt");
            mt = pt.matcher(page.asXml());
            isMatch = mt.find();
            if(isMatch)
            {
                if(scriptList.toString().contains(mt.group(0)) && !mt.group(0).substring(0,mt.group(0).indexOf("000111")).contains("'") && !mt.group(0).substring(0,mt.group(0).indexOf("000111")).contains("\""))
                {
                    System.out.println("xxxxxxxxxxxxxx");
                }
            }


            System.out.println("111111111111111");
            System.out.println(page.asXml());
            System.out.println("11111111111111");
        } catch (ScriptException e) {


            System.out.println("0000000000000");
            System.out.println(e.getFailingLine());
            System.out.println(e.getPage().asXml());
            System.out.println("0000000000000");
        }
        webClient.close();
    }


    public class WebConnectionListener extends FalsifyingWebConnection {

        int count = 0;
        public WebConnectionListener(WebClient webClient) throws IllegalArgumentException {
            super(webClient);
        }

        @Override
        public WebResponse getResponse(WebRequest request) throws IOException {


            WebResponse response = super.getResponse(request);

            String url = response.getWebRequest().getUrl().toString();

            Pattern pt;
            Matcher mt;
            String reResponse;

            pt = Pattern.compile("location(\\W*?)=");
            mt = pt.matcher(response.getContentAsString());
            reResponse = mt.replaceAll("location.href=");


            pt = Pattern.compile("location.replace");
            mt = pt.matcher(response.getContentAsString());
            reResponse = mt.replaceAll("_replace");

            if(isFirst)
            {
                System.out.println(url);
                isFirst = false;

                return createWebResponse(response.getWebRequest(), "<script>\n" +
                        "function append(type,payload)\n" +
                        "{\n" +
                        "\tif(payload.indexOf(\"111000\")>-1)\n" +
                        "\t{\n" +
                        "\t\tvar para=document.createElement(\"p\");\n" +
                        "\t\tvar node=document.createTextNode(type + payload);\n" +
                        "\t\tpara.appendChild(node);\n" +
                        "\t\tvar element=document.getElementsByTagName(\"html\")[0];\n" +
                        "\t\telement.appendChild(para);\n" +
                        "\t}\n" +
                        "}\n" +
                        "var _eval = eval;\n" +
                        "window.eval = function(string) {\n" +
                        "\tappend(\"eval\",string);\n" +
                        "\t_eval(string);\n" +
                        "};\n" +
                        "var _setTimeout = setTimeout;\n" +
                        "window.setTimeout = function(code,millisec) {\n" +
                        "\tappend(\"settimeout\"+code);\n" +
                        "\t_setTimeout(code,millisec);\n" +
                        "};\n" +
                        "var _replace = function(url)\n" +
                        "{\n" +
                        "\tappend(\"location\",url);\n" +
                        "};\n" +
                        "location.__defineSetter__('href', function(url) {\n" +
                        "\tappend(\"location\",url);\n" +
                        "});\n" +
                        "</script>" + reResponse, response.getContentType(), response.getStatusCode(), "Ok");

            }

            return response;
        }

    }


    @org.junit.Test
    public void CheckDomXss() throws IOException {
        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        new WebConnectionListener(webClient);

        // 这里尝试去取我们设置的JavaScript错误处理器
        final JavaScriptEngine myEngine = new JavaScriptEngine(webClient) {
            @Override
            protected void handleJavaScriptException(final ScriptException scriptException,
                                                     final boolean triggerOnError) {
                System.out.println("############");
                System.out.println(scriptException.getMessage());
                System.out.println("############");
                super.handleJavaScriptException(scriptException, triggerOnError);

            }
        };
        webClient.setJavaScriptEngine(myEngine);


        //获取页面
        try
        {
            String url ="http://127.0.0.1:80/test.php?id=111000'%22%3cttt'%22ttt%3e #111000'\"<ttt'\"ttt>";

            HtmlPage page = webClient.getPage(url);
            String html = page.asXml().toLowerCase();
            System.out.println("111111111111111");
            System.out.println(html);
            System.out.println("11111111111111");
        } catch (ScriptException e) {
            System.out.println("0000000000000");
            System.out.println(e.getFailingLine());
            System.out.println(e.getPage().asXml().toLowerCase());
            System.out.println("0000000000000");
        }
        webClient.close();
    }


    @org.junit.Test
    public void Post() throws IOException {
        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);

        WebRequest request = new WebRequest(new URL("http://xss-quiz.int21h.jp/?sid=709be6716ec194d5a628855dc984b8bde45dc2fa"),HttpMethod.POST);

        request.setAdditionalHeader("Cookie","PHPSESSID=1vh5h6pj61dqd2jp4nkfnq9un0; __utmt=1; __utma=251560719.1365751418.1525663290.1525663290.1525663290.1; __utmb=251560719.14.10.1525663290; __utmc=251560719; __utmz=251560719.1525663290.1.1.utmcsr=baid");

        request.setRequestBody("p1=111111111");

        //获取页面
        try
        {
            HtmlPage page = webClient.getPage(request);

            System.out.println("111111111111111");
            System.out.println(page.asXml());
            System.out.println("11111111111111");
        } catch (ScriptException e) {
            System.out.println("0000000000000");
            System.out.println(e.getFailingLine());
            System.out.println(e.getPage().asXml());
            System.out.println("0000000000000");
        }
        webClient.close();
    }
}
