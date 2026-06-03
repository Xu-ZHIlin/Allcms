package cn.edu.guet.cms2.exception;

import cn.edu.guet.cms2.util.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PermissionException.class)
    public Result<Object> handlePermissionException(PermissionException e) {
        // 返回 403 状态码（Forbidden）或业务约定的错误码
        return Result.fail(403, e.getMessage());
    }
    @ExceptionHandler(IllegalUserException.class)
    public Result<Object> handlePermissionException(IllegalUserException e) {
        // 返回 401 状态码（Unauthorized）或业务约定的错误码
        return Result.fail(401, e.getMessage());
    }
}
