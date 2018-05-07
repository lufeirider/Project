package burp;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.FalsifyingWebConnection;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static burp.BurpExtender.debug;
import static burp.BurpExtender.isFirst;


public class WebConnectionListener extends FalsifyingWebConnection {

    public WebConnectionListener(WebClient webClient) throws IllegalArgumentException {
        super(webClient);
    }

    @Override
    public WebResponse getResponse(WebRequest request) throws IOException {
        Pattern pt;
        Matcher mt;

        WebResponse response = super.getResponse(request);

        String url = response.getWebRequest().getUrl().toString();

        //debug(url);

        pt = Pattern.compile("location(\\W*?)=");
        mt = pt.matcher(response.getContentAsString());
        String reResponse = mt.replaceAll("location.herf=");

        pt = Pattern.compile("location.replace");
        mt = pt.matcher(response.getContentAsString());
        reResponse = mt.replaceAll("_replace");

        if(isFirst)
        {
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
                    "\tappend(\"settimeout\",code);\n" +
                    "\t_setTimeout(code,millisec);\n" +
                    "};\n" +
                    "var _replace = function(url)\n" +
                    "{\n" +
                    "\tappend(\"location.replace\",url);\n" +
                    "};\n" +
                    "location.__defineSetter__('href', function(url) {\n" +
                    "\tappend(\"location\",url);\n" +
                    "});\n" +
                    "</script>" + reResponse, response.getContentType(), response.getStatusCode(), "Ok");

        }

        return response;
    }

}