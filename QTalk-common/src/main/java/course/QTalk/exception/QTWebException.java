package course.QTalk.exception;

import course.QTalk.enums.BaseEnum;
import course.QTalk.enums.BaseExceptionEnum;
import lombok.Data;

/**
 * QTalk Web自定义异常
 */
@Data
public class QTWebException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String msg; //异常中的信息
    private int code = 1; //业务状态码，规则：异常弹窗状态码
    private int status = 200; //http状态码，按照http协议规范，如：200,201,400等

    public QTWebException(BaseEnum baseEnum) {
        super(baseEnum.getValue());
        this.msg = baseEnum.getValue();
        this.code = baseEnum.getCode();
    }

    public QTWebException(BaseEnum baseEnum, Throwable e) {
        super(baseEnum.getValue(), e);
        this.msg = baseEnum.getValue();
        this.code = baseEnum.getCode();
    }

    public QTWebException(BaseExceptionEnum errorEnum) {
        super(errorEnum.getValue());
        this.status = errorEnum.getStatus();
        this.msg = errorEnum.getValue();
        this.code = errorEnum.getCode();
    }

    public QTWebException(BaseExceptionEnum errorEnum, Throwable e) {
        super(errorEnum.getValue(), e);
        this.status = errorEnum.getStatus();
        this.msg = errorEnum.getValue();
        this.code = errorEnum.getCode();
    }

    public QTWebException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public QTWebException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public QTWebException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public QTWebException(String msg, int code, int status) {
        super(msg);
        this.msg = msg;
        this.code = code;
        this.status = status;
    }

    public QTWebException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

    public QTWebException(String msg, int code, int status, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
        this.status = status;
    }
}