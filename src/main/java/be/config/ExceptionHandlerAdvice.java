package be.config;

import be.util.api.*;
import be.util.exception.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.JsonMappingException.*;
import com.fasterxml.jackson.databind.exc.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.springframework.http.converter.*;
import org.springframework.validation.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.*;
import org.springframework.web.multipart.*;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(CustomException.class)
    public ApiResponse<Object> customException(CustomException e) {

        int code = e.getCode();
        Object data = e.getData();
        String clientShowMsg;

        if (e instanceof ExpectableServerException serverEx) {
            log.warn(
                    "Server exception has been raised : {}",
                    serverEx.getClass().getSimpleName(),
                    serverEx
            );

            clientShowMsg = serverEx.getClientShowMessage();
        } else {
            clientShowMsg = e.getMessage();
        }

        return ApiResponse.fail(code, data, clientShowMsg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<?> missingRequestParamException(MissingServletRequestParameterException e) {
        String paramType = e.getParameterType();
        String paramName = e.getParameterName();
        String detailedMessage = e.getMessage();

        String message = String.format(
                "요청에 필요한 파라미터 '(%s) %s' 를 찾을 수 없습니다.",
                paramType, paramName
        );

        return ApiResponse.fail(400, detailedMessage, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<?> typeMismatchException(MethodArgumentTypeMismatchException e) {

        Class<?> requiredType = e.getRequiredType();
        String simpleTypeName = requiredType == null ?
                "UNKNOWN" : requiredType.getSimpleName();
        String argName = e.getName();
        Object data = null;

        String message = String.format(
                "파라미터 '%s' 를 타입 %s 에 매칭시킬 수 없습니다.", argName, simpleTypeName
        );

        if (requiredType != null && requiredType.isEnum()) {
            data = String.format(
                    "%s 는 %s 중 하나여야 합니다.",
                    simpleTypeName,
                    Arrays.toString(requiredType.getEnumConstants())
            );
        }

        return ApiResponse.fail(400, data, message);
    }

    @SuppressWarnings("unchecked")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        String message;
        Object data;

        switch (cause) {
            case JsonParseException jsonParseEx -> {
                message = "올바른 json 형식이 아닙니다.";
                data = jsonParseEx.getOriginalMessage();
            }
            case InvalidFormatException invalidFormatEx -> {
                message = String.format(
                        "주어진 값 %s 은 올바르지 않은 형식입니다.",
                        invalidFormatEx.getValue()
                );

                List<String> description = (List<String>) (data = new ArrayList<>());

                for (Reference path : invalidFormatEx.getPath()) {
                    String fieldName = path.getFieldName();
                    if (fieldName != null) {
                        description.add(String.format(
                                "파라미터 '%s' 의 형식이 올바르지 않습니다.", fieldName
                        ));
                    }
                }
            }
            case JsonMappingException mappingEx -> {
                message = "필요한 값이 제공되지 않았습니다.";

                List<String> description = (List<String>) (data = new ArrayList<>());

                for (Reference path : mappingEx.getPath()) {
                    String fieldName = path.getFieldName();
                    description.add(String.format(
                            "%s 가 제공되지 않았습니다.", fieldName
                    ));
                }
            }
            case null, default -> {
                message = "요청 변환중 오류가 발생했습니다.";
                data = e.getMessage();
            }
        }

        return ApiResponse.fail(400, data, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> reqArgNotValidException(MethodArgumentNotValidException e) {

        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(Util::fieldErrorMessage)
                .toList();

        return ApiResponse.fail(400, errorMessages, "Bad Request");
    }

    @ExceptionHandler(MultipartException.class)
    public ApiResponse<?> multipartException(MultipartException e)  {
        String message = e.getMessage();
        return ApiResponse.fail(400, null, message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<?> multipartMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {

        String maxima = "[Unknown]";
        if (e.getMaxUploadSize() == -1) {

            String caseMsg = e.getCause().getMessage();
            if (caseMsg != null) {
                int i = caseMsg.indexOf("size of ");
                if (i != -1) {
                    maxima = caseMsg.substring(Math.min(
                            i + 8, caseMsg.length()
                    ));
                }
            }

        } else {
            maxima = String.format("%d bytes.", e.getMaxUploadSize());
        }

        String message = String.format(
                "제공된 파일 크기가 허용치를 초과했습니다: %s",
                maxima
        );
        int statusCode = e.getStatusCode().value();

        return ApiResponse.fail(statusCode, null, message);
    }

    private record Util() {

        static String fieldErrorMessage(FieldError fieldError) {
            return String.format("Field '%s' (given:'%s'). Message: %s",
                    fieldError.getField(), fieldError.getRejectedValue(),
                    fieldError.getDefaultMessage()
            );
        }
    }
}
