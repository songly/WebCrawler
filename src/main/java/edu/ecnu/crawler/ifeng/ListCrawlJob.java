package edu.ecnu.crawler.ifeng;

import com.google.common.collect.Lists;
import edu.ecnu.crawler.general.Recorder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Get News List from the given URL
 * Created by leyi on 14/12/5.
 */
public class ListCrawlJob {
    private static Logger logger = Logger.getLogger(ListCrawlJob.class.getName());

    /**
     * 爬取包含一个文件link.txt文件夹下的候选页面列表
     * @param outputFolder
     */
    public void crawlList(String outputFolder){
        //判断子目录或者link.txt文件
        String dirPath = outputFolder;
        File dir = new File(dirPath);
        String linkPath="";
        String mPath="";
        if(dir.isDirectory()) {
            File[] fileList = dir.listFiles();
            if(fileList == null || fileList.length<=0) {
                return;
            }
            for(File file : fileList) {
                if (file.toString().endsWith("link.txt")) {
                    linkPath = file.toString();
                    mPath = dirPath;
                    String linkUrl = getLinkURL(linkPath);
                    if (linkUrl == null || "".equals(linkUrl)) {
                        return;
                    }
                    getUrlList(linkUrl, mPath);
                    break;
                } else {
                    crawlList(file.toString());
                }
            }
        }
        logger.info("Finished crawling dir:"+dirPath);
    }

    /**
     * Get Document URLs
     * @param linkUrl
     * @param mPath
     */
    private void getUrlList(String linkUrl, String mPath) {
        //爬取列表页面
        logger.info("Start crawl list page:"+linkUrl);

        String content = "";
        content = BasicCrawler.crawlPage(linkUrl);

        //TODO  解析文章列表页面，获取需要爬取页面URL
        Document doc = Jsoup.parse(content);

        //页面格式1
        if(linkUrl.startsWith("http://edu.ifeng.com/listpage/4111") ||
                linkUrl.startsWith("http://edu.ifeng.com/kaoyan")) {
            processListType(doc,linkUrl,mPath);
        }

        //页面格式2
        else if(linkUrl.startsWith("http://edu.ifeng.com/listpage") ||
                linkUrl.startsWith("http://edu.ifeng.com/gaokao/news/") ||
                linkUrl.startsWith("http://edu.ifeng.com/zxx/") ||
                linkUrl.startsWith("http://edu.ifeng.com/peixun/")) {

                processLiType(doc,linkUrl,mPath);
        }
    }

    /**
     * Type 1 Page
     */
    private void processLiType(Document doc, String linkUrl, String mPath){
        List<String> urlList = Lists.newArrayList();
        Elements eles = doc.select("div.newslist li");
        //if 2014
        int lastMon=12;
        int lastlastMon=12;
        int count=0;

        for(org.jsoup.nodes.Element ele : eles) {
            String time = ele.select("h4").first().text();
            Element href = ele.select("a[href]").first();
            String title = href.text();
            String url = href.attr("href");

            if(checktime(time,"MM/DD hh:mm")) {
                int month = Integer.valueOf(time.substring(0,2));
                if(month>lastMon && month>lastlastMon){  //发现有意外错误事件标注
                    count++;
                    if(count>1){
                        logger.info("Break from Invaild Time");
                        writeToLinkFile(urlList, mPath);
                        return;
                    }
                }
                else{
                    lastlastMon = lastMon;  //发现较小的意外错误
                    lastMon = month;
                    count=0;   //发现是意外错误，重新计数
                }

                urlList.add(title+"\t"+url);
            }
            else{
                writeToLinkFile(urlList,mPath);
                return;
            }
        }

        //写入List文件
        writeToLinkFile(urlList,mPath);
        urlList.clear();

        //TODO  翻页，根据不同页面规则不同
        Elements nextPage = doc.select("div.m_page").select("span");
        if(nextPage.size()<2) {
            logger.info("Could not find next page");
        }
        for(Element span : nextPage) {
            if(span.text().equals("下一页")){
                Element next = span.select("a[href]").first();
                String nextHref = next.attr("href");
                if(!linkUrl.equals(nextHref)){
                    getUrlList(nextHref,mPath);
                }
            }
        }

    }

    /**
     * Type 2 Page
     */
    private void processListType(Document doc, String linkUrl, String mPath){
        List<String> urlList = Lists.newArrayList();
        Elements eles = doc.select("div.listPublic");

        for(Element ele : eles) {
            Element titlehref = ele.select("h3 > a").first();
            String title = titlehref.text();
            String url = titlehref.attr("href");
            urlList.add(title+"\t"+url);
        }

        //写入List文件
        writeToLinkFile(urlList,mPath);
        urlList.clear();

        //翻页
        Elements nextPage = doc.select("div.m_page").select("a[href]");
        if(nextPage.size()<2) {
            logger.info("Could not find next page");
        }
        for(Element href : nextPage) {
            if(href.text().startsWith("下一页")){
                String nextHref = href.attr("href");
                if(!linkUrl.equals(nextHref)){
                    getUrlList(nextHref,mPath);
                }
            }
        }

    }

    /**
     * Write URL list to link files
     * @param urlList
     */
    private void writeToLinkFile(List<String> urlList,String mPath) {
        Recorder linkRecoder = new Recorder(mPath+File.separator+"pagelists.txt",false);
        for(String url : urlList){
            linkRecoder.writeRecordUTF8(url+"\r\n");
        }
    }

    /**
     * 判断文章时间是否在需要范围内
     * @param time
     * @return
     */
    private boolean checktime(String time,String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            Date date = simpleDateFormat.parse(time);
            //TODO check date
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            Calendar base = Calendar.getInstance();
            base.set(2014,1,1);
            int year = cal.get(Calendar.YEAR);
            if(year == 1970) {
                return true;
            }
            else if(cal.before(base)){
                return false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String getLinkURL(String linkPath) {
        BufferedReader br;
        try {
             br = new BufferedReader(new FileReader(linkPath));
             String line="";
             if((line=br.readLine())!=null) {
                 return line.trim();
             }
        } catch (FileNotFoundException e) {
            logger.info("File not found"+linkPath);
            return "";
        } catch (IOException e) {
            logger.info("IO exception" + linkPath);
            return "";
        }
        return "";
    }
}
