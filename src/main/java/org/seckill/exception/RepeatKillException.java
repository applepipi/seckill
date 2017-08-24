package org.seckill.exception;

/**
 * RepeatKillException
 * Author： liping
 * Date: 2017/8/17
 * Time: 10:02
 */

/**
 * 重复秒杀异常（运行期异常）
 */
public class RepeatKillException extends SeckillException{
    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
