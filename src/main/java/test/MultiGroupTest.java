package test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.cache.client.core.CacheService;

public class MultiGroupTest {
    private static final AtomicInteger runflag = new AtomicInteger(1);
    private static AtomicLong[] normalCntVec;
    private static AtomicLong[] errorCntVec;
    private static CountDownLatch shutdownLatch;
    private static final Logger logger = LoggerFactory.getLogger(MultiGroupTest.class);

    private static class Sender implements Runnable {
        private CacheService service;
        private String threadName;
        private AtomicLong normalCnt;
        private AtomicLong errorCnt;
        private byte[] sendBuf;
        private CountDownLatch latch;
        
        public Sender(String threadName, AtomicLong normalCnt, AtomicLong errorCnt,
            CountDownLatch latch, int packLen) {
            
            this.threadName = threadName;
            this.normalCnt = normalCnt;
            this.errorCnt = errorCnt;
            this.latch = latch;
            this.service = CacheService.getInstance();
            
            sendBuf = new byte[packLen];
            for (int i = 0; i < packLen; i++) {
                if (i % 3 == 0) {
                    sendBuf[i] = 'p';
                } else if (i % 3 == 1) {
                    sendBuf[i] = 'o';
                } else {
                    sendBuf[i] = 'i';
                }
            }
        }

        @Override
        public void run() {
            try {
                long start = System.currentTimeMillis();
                String key = null;
                
                do {
                    try {
                        key = String.format("%s:%d", threadName, System.nanoTime());

                        String resultSet = service.set(key, sendBuf);
                        if (resultSet.equals("0")) {
                            normalCnt.incrementAndGet();
                        } else {
                            errorCnt.incrementAndGet();
                        }

                        String resultGet = service.get(key);
                        if (resultGet != null) {
                            normalCnt.incrementAndGet();
                        } else {
                            errorCnt.incrementAndGet();
                        }
                        
                        long resultDel = service.del(key);
                        if (resultDel == 1L) {
                            normalCnt.incrementAndGet();
                        } else {
                            errorCnt.incrementAndGet();
                        }
    					
//                        Thread.sleep(660*1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                        errorCnt.incrementAndGet();
                    }
                } while (runflag.get() > 0);

                long totalSend = (long) (normalCnt.get());
                long end = System.currentTimeMillis();
                long timeSpend = end - start;

                logger.info(threadName + " runs " + timeSpend / 1000 + " seconds, total send message count:"
                        + totalSend + ", error count:" + errorCnt.get());
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }
    }

    public void testMultiThread(int totalTime, int totalThreadCnt, int packLen) throws Exception {
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        logger.info("--------------------------------------------------------------------------");
        logger.info("PerformanceTest start at: "+format.format(date));
    	
        normalCntVec = new AtomicLong[totalThreadCnt];
        errorCntVec = new AtomicLong[totalThreadCnt];
        for (int i = 0; i < totalThreadCnt; i++) {
            normalCntVec[i] = new AtomicLong(0);
            errorCntVec[i] = new AtomicLong(0);
        }

        shutdownLatch = new CountDownLatch(totalThreadCnt);
        NStatistic stat = new NStatistic(normalCntVec, errorCntVec);

        Timer timer = new Timer("timer", true);
        timer.schedule(stat, 0, 5 * 1000);

        long start = System.currentTimeMillis();
        Vector<Sender> theadVec = new Vector<Sender>(totalThreadCnt);
        int threadIdx = 0;
        
        
        for (; threadIdx < totalThreadCnt; threadIdx++) {
            String threadName = "SenderThread" + (threadIdx < 10 ? "0" : "") + threadIdx;      
            Sender sender = new Sender(threadName, normalCntVec[threadIdx], errorCntVec[threadIdx], shutdownLatch, packLen);
            Thread t = new Thread(sender);
            t.start();
            theadVec.add(sender);

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {}

            if (threadIdx % 200 == 0) {
            	logger.info("************Run Thread count:" + threadIdx);
            }
        }

        try {
            shutdownLatch.await(totalTime, TimeUnit.SECONDS);
            runflag.decrementAndGet();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            timer.cancel();
        }

        long end = System.currentTimeMillis();
        long timeSpend = end - start;
        long totalSetCount = 0;
        for (AtomicLong i : normalCntVec)
            totalSetCount += i.get();

        Thread.sleep(2000);
        logger.info("Thread count:" + threadIdx);
        logger.info("--------------------------------------------------------------------------");
        logger.info("MultiSender runs for:" + timeSpend / 1000 + "seconds, total request:" + totalSetCount
                + ", average TPS:" + (totalSetCount * 1000) / timeSpend + ", Max TPS:" + stat.getMaxTps());
        date = new Date(System.currentTimeMillis());
        logger.info("FunctionTest end at: "+format.format(date));
        logger.info("--------------------------------------------------------------------------");

        Thread.sleep(2000);
        CacheService.getInstance().close();
    }

	public void run() {
		try {
			PropertyConfigurator.configure("conf/log4j.properties");
			InputStream in = new BufferedInputStream(new FileInputStream("conf/test.properties"));

			Properties p = new Properties();
            p.load(in);

            int totalTime = Integer.valueOf(p.getProperty("totalTime"));
            int totalThreadCnt = Integer.valueOf(p.getProperty("totalThreadCnt"));
            int packLen = Integer.valueOf(p.getProperty("packLen"));
            testMultiThread(totalTime, totalThreadCnt, packLen);
		} catch (Exception e) {
			logger.error("Test failed!", e);
		}
	}
	
    public static void main(String[] args) throws Exception {
    	new MultiGroupTest().run();
    }
}