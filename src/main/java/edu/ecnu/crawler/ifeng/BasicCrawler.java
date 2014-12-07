package edu.ecnu.crawler.ifeng;

import edu.ecnu.crawler.general.Crawler;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by leyi on 14/12/7.
 */
public class BasicCrawler {

    private static Logger logger = Logger.getLogger(BasicCrawler.class.getName());


    private static BasicCrawler instance = null;

    private static Crawler crawler =null;

    private BasicCrawler() {
        crawler = new Crawler();
    }

    public static String crawlPage(String url) {
        if(instance==null) {
            instance = new BasicCrawler();
        }
        crawler.setUrlnoCheck(url.trim());

        String content = "";
        int trycount=5;
        try {
            content = crawler.getContent();
            while(content.length()<50 && trycount>0){
                sleep();
                trycount--;
                content = crawler.getContent();
            }
            sleep();
        }catch (Exception e){
            logger.info("Crawl the page failed!"+url);
        }

        return content;
    }

    public static void sleep() {
        int chance = new Random().nextInt(10);
        if(chance==0){
            long sleepTime = System.currentTimeMillis() % 8500 + 1000;
            System.out.println("Sleep " + sleepTime + "ms");
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.info("Thread sleep interrupted!");
            }
        }
    }
}
