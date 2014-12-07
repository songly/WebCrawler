package edu.ecnu.crawler.ifeng;

import java.util.logging.Logger;

/**
 * Created by leyi on 14/12/5.
 */
public class EduPageCrawler {

    private static Logger logger = Logger.getLogger(EduPageCrawler.class.getName());

    public static void main(String[] args) {
        logger.info("Start Crawl Pages on edu.ifeng.com");
        if(args.length<1){
            logger.info("Required 1 arguments: directory or pagelists.txt file");
            return;
        }

        String file =args[0];
        PageCrawlJob crawler = new PageCrawlJob();
        crawler.crawlURLs(file);

        logger.info("Crawl Job Finished");
    }

}
