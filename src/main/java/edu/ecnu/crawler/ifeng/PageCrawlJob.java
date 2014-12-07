package edu.ecnu.crawler.ifeng;

import edu.ecnu.crawler.general.XMLRecorder;

import java.io.*;
import java.util.logging.Logger;

/**
 * 爬取列表页面
 * Created by leyi on 14/12/5.
 */
public class PageCrawlJob {
    private static Logger logger = Logger.getLogger(PageCrawlJob.class.getName());

    public void crawlURLs(String inputPath){
        File path = new File(inputPath);

        if(path.isDirectory()) {
            File[] fileList = path.listFiles();
            if(fileList == null || fileList.length<=0) {
                return;
            }

            for(File file : fileList) {
                crawlURLs(file.getPath());
            }

        }
        else if(path.toString().endsWith("pagelists.txt")) {
            logger.info("Start crawl path:"+path.toString());
            crawlPageList(path);
            logger.info("Successfully crawl path:"+path.toString());
        }
    }

    private void crawlPageList(File pageListFile){
        BufferedReader br;
        XMLRecorder xmlRecorder = new XMLRecorder("pages",pageListFile.getParent()+File.separator+"pages.xml");
        PageParserJob parser = new PageParserJob();

        try {
            br = new BufferedReader(new FileReader(pageListFile));
            String line="";
            while((line=br.readLine())!=null) {
                String[] urls = line.split("\t");

                if(urls.length==2) {
                    String title = urls[0];
                    String url = urls[1];

                    logger.info("Start crawl list page:"+url);

                    String content = "";
                    content = BasicCrawler.crawlPage(url);
                    logger.info("Get the page content:"+url);

                    //解析
                    if(content.length()>50) {
                        xmlRecorder.writeRecordUTF8("<page>\r\n");
                        xmlRecorder.writeRecordUTF8("<title>"+title+"</title>\r\n");
                        xmlRecorder.writeRecordUTF8("<url>"+url+"</url>\r\n");
                        parser.parseDocument(content,xmlRecorder);
                        xmlRecorder.writeRecordUTF8("</page>\r\n");
                    }
                }
            }

            xmlRecorder.writeEnd();


        } catch (FileNotFoundException e) {
            logger.info("File not found"+pageListFile);
        } catch (IOException e) {
            logger.info("IO exception" + pageListFile);
        }
    }
}
