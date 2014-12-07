package edu.ecnu.crawler.ifeng;

import java.util.logging.Logger;

/**
 * Main Entry of Crawler for IFENG EDU
 * Created by leyi on 14/12/5.
 */
public class EduListCrawler {

    private static Logger logger = Logger.getLogger(EduListCrawler.class.getName());

    public static void main(String[] args) {
        logger.info("Start Crawl Pages on edu.ifeng.com");
        if(args.length<1){
            logger.info("Required 1 arguments: directory path");
            return;
        }

        String directory =args[0];
        ListCrawlJob crawler = new ListCrawlJob();
        crawler.crawlList(directory);

        logger.info("Crawl Job Finished");
    }
}
