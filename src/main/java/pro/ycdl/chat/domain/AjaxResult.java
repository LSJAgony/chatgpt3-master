package pro.ycdl.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.ycdl.chat.common.Constants;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjaxResult {

    /**
     * 响应码
     * 1 成功
     * 0 失败
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String message;
    /**
     * 响应数据
     */
    private Object data;
    /**
     * 响应时间
     */
    private Long timestamp;

    public static AjaxResult success(Object data) {
        return new AjaxResult(Constants.SUCCESS_CODE, "请求成功", data, System.currentTimeMillis());
    }

    public static AjaxResult success() {
        return success(null);
    }

    public static AjaxResult failure(String message) {
        return new AjaxResult(Constants.FAILURE_CODE, message, null, System.currentTimeMillis());
    }
}