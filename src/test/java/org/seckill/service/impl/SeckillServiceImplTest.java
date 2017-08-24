package org.seckill.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExcution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * SeckillServiceImplTest
 * Author： liping
 * Date: 2017/8/17
 * Time: 14:41
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"
        ,"classpath:spring/spring-service.xml"})
public class SeckillServiceImplTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillAll() throws Exception {
        List<Seckill> list = seckillService.getSeckillAll();
        logger.info("list={}",list);
    }

    @Test
    public void getById() throws Exception {
        long id = 1000L;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}",seckill);
    }

    @Test
    /**
     * 测试代码完整逻辑，可重复执行
     */
    public void testSeckillLogic() throws Exception {
        long id = 1000L;
        long phone = 11111111112L;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposerr={}",exposer);
        //如果开启了秒杀
        if(exposer.isExposed()){
            try{
                SeckillExcution seckillExcution = seckillService.executeSeckill(id,phone,exposer.getMd5());
                logger.info("result={}",seckillExcution);
            }catch (RepeatKillException e){
                logger.error(e.getMessage());
            }catch (SeckillCloseException e){
                logger.error(e.getMessage());
            }
        }else{
            //秒杀未开启
            logger.warn("exposer={}",exposer);
        }
    }

    @Test
    public void executeSeckillProcedure(){
        long seckillId = 1002L;
        long phone = 15764318169L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExcution excution = seckillService.executeSeckillProcedure(seckillId,phone,md5);
            logger.info(excution.getStateInfo());
        }
    }
}