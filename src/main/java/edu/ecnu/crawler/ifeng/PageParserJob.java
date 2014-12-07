package edu.ecnu.crawler.ifeng;

import edu.ecnu.crawler.general.XMLRecorder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.logging.Logger;

/**
 * Created by leyi on 14/12/5.
 */
public class PageParserJob {
    private static Logger logger = Logger.getLogger(PageCrawlJob.class.getName());


    public void parseDocument(String page, XMLRecorder xmlRecorder) {
        Document doc = Jsoup.parse(page);
        //处理发布日期
        Element timeele = doc.select("p.p_time span[itemprop=datePublished]").first();
        if(timeele!=null){
            xmlRecorder.writeRecordUTF8("<date>"+reformat(timeele.text())+"</date>\r\n");
        }

        //处理新闻来源
        Element publisher = doc.select("p.p_time span[itemprop=publisher]").first();
        if(publisher!=null) {
            xmlRecorder.writeRecordUTF8("<source>"+reformat(publisher.text())+"</source>\r\n");
        }

        //处理新闻主体内容，包括翻页
        StringBuffer contentBuffer = new StringBuffer("");
        parseContent(contentBuffer,doc);
        String content = reformat(contentBuffer.toString());
        xmlRecorder.writeRecordUTF8("<content>"+content+"</content>\r\n");

    }

    private void parseContent(StringBuffer content, Document doc) {
        Elements paras = doc.select("div[id=main_content] p");
        for(Element para : paras) {
            content.append(para.text()).append("\n");
        }

        Elements nextpage = doc.select("div.next a[id=pagenext]");
        if(nextpage.size()>0){
             String nextUrl = nextpage.first().attr("href");
             if(nextUrl.startsWith("http://edu.ifeng.com/a/")){
                 logger.info("Get next page:"+nextUrl);
                 String nextContent = BasicCrawler.crawlPage(nextUrl);
                 Document nextdoc = Jsoup.parse(nextContent);
                 parseContent(content,nextdoc);
             }
        }
    }

    /**
     * Reformat String literal
     */
    public static String reformat(String message) {
        StringBuffer sb = new StringBuffer("");

        message = message.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;").replace("^", "").replace("\b", "");

        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if ((ch == 0x9) || (ch == 0xA) || (ch == 0xD)
                    || ((ch >= 0x20) && (ch <= 0xD7FF))
                    || ((ch >= 0xE000) && (ch <= 0xFFFD))
                    || ((ch >= 0x10000) && (ch <= 0x10FFFF)))
                sb.append(ch);
        }
        return sb.toString();
    }
}
