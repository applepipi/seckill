package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.cache.RedisDao;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExcution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SeckillServiceImpl
 * Author： liping
 * Date: 2017/8/17
 * Time: 10:08
 */
@Service
public class SeckillServiceImpl implements SeckillService{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    //用于混淆md5
    private String salt = "svdgrqr43t65tyh452432343859uhOU9O8";

    /**
     * 秒杀列表
     * @return
     */
    public List<Seckill> getSeckillAll() {
        return seckillDao.queryAll(0,4);
    }

    /**
     * 秒杀详情
     * @param seckillId
     * @return
     */
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    /**
     * 暴露秒杀接口地址
     * @param seckillId
     * @return
     */
    public Exposer exportSeckillUrl(long seckillId) {
        //緩存优化（超时的基础上维护一致性，因为秒杀的对象一般不会改变）
        //1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            //2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            if(seckill == null){
                //秒杀单不存在
                return new Exposer(false,seckillId);
            }else{
                //放入redis
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date now = new Date();
        if(now.getTime()<startTime.getTime()
                ||now.getTime()>endTime.getTime()){
            return new Exposer(false,seckillId,now.getTime(),startTime.getTime(),endTime.getTime());
        }
        String md5 = getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    /**
     * 秒杀
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作（缓存，http请求）或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务
     */
    public SeckillExcution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException {
        //md5不对，抛出异常
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存+记录购买
        Date now = new Date();
        try {
            //记录购买
            int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
            if(insertCount <= 0){
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            }else {
                //减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId,now);
                if(updateCount <= 0){
                    //秒杀结束（库存没有了，时间问题在接口暴露时已经处理了）rollback
                    throw new SeckillCloseException("seckill is closed");
                }else{
                    //秒杀成功 commit
                    SuccessKilled successKilled =  successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExcution(seckillId, SeckillStateEnum.SUCCESSS,successKilled);
                }
            }
        }catch (SeckillCloseException e1){
            throw e1;
        }catch (RepeatKillException e2){
            throw e2;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            //所有编译期异常，转化为运行期异常（spring会回滚）
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }

    public SeckillExcution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if(md5 == null ||!md5.equals(getMD5(seckillId))){
            return new SeckillExcution(seckillId, SeckillStateEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        try {
            seckillDao.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map,"result",-2);
            if(result == 1){
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExcution(seckillId,SeckillStateEnum.SUCCESSS,sk);
            }else{
                return new SeckillExcution(seckillId,SeckillStateEnum.stateOf(result));
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExcution(seckillId,SeckillStateEnum.INNER_ERROR);
        }

    }

    private String getMD5(long seckillId){
        String base = seckillId + "/" +salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
