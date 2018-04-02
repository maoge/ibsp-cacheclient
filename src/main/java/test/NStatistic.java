package test;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NStatistic extends TimerTask {
    private AtomicLong maxTPS;
    private AtomicLong[] normalCntVec;
    private AtomicLong[] errorCntVec;
    private long lastTotalCnt;
    private long prevRunSysTime;
    private long currRunSysTime;
    private long startRunSysTime;
    private static final Logger logger = LoggerFactory.getLogger(NStatistic.class);
    
    public NStatistic(AtomicLong[] normalCntVec, AtomicLong[] errorCntVec) {
        this.normalCntVec = normalCntVec;
        this.errorCntVec = errorCntVec;
        this.maxTPS = new AtomicLong();
        lastTotalCnt = 0;
        startRunSysTime = System.currentTimeMillis();
        prevRunSysTime = System.currentTimeMillis();
        currRunSysTime = System.currentTimeMillis();
    }
        
    public long getMaxTps() {
        return  maxTPS.get();
    } 

    @Override
    public void run() {
        long currTotalCnt = 0;
        long errorTotalCnt = 0;
        int lastSecondTPS = 0;
        currRunSysTime = System.currentTimeMillis();
        long diff = currRunSysTime - prevRunSysTime;
        long timeSpend = currRunSysTime - startRunSysTime;
        
        for (AtomicLong ai : normalCntVec) {
            currTotalCnt += ai.get();
        }
        
        for (AtomicLong ai : errorCntVec) {
            errorTotalCnt += ai.get();
        }
        
        if(Math.round(diff/1000) > 0) {
            lastSecondTPS = Math.round((currTotalCnt - lastTotalCnt)/Math.round(diff/1000));
            long nMaxTps = maxTPS.get();
                            
            if (lastSecondTPS > nMaxTps) {
                maxTPS.getAndSet(lastSecondTPS);
            }
        }
        
        if (timeSpend>0) {
            logger.info("Statistic runs for:" + timeSpend/1000 + "seconds, total processed:" + currTotalCnt + ", total errors:" + errorTotalCnt + ", average TPS:" + (currTotalCnt*1000)/timeSpend + ", last Tps: " + lastSecondTPS + ", Max TPS:" + maxTPS.get());
        }
            
        lastTotalCnt = currTotalCnt;
        prevRunSysTime = currRunSysTime;
    }
    
}
