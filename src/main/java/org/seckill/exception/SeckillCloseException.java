package org.seckill.exception;

/**
 * SeckillCloseException
 * Author： liping
 * Date: 2017/8/17
 * Time: 10:04
 */

/**
 * 秒杀关闭
 */
public class SeckillCloseException extends SeckillException{
    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
