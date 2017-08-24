package org.seckill.exception;

/**
 * SeckillException
 * Author： liping
 * Date: 2017/8/17
 * Time: 10:05
 */
public class SeckillException extends RuntimeException{
    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
