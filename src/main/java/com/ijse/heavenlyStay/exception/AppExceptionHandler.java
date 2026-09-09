package com.ijse.heavenlyStay.exception;


import com.ijse.heavenlyStay.dto.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    public CommonResponse handleServerException(Exception ex, WebRequest webRequest){
        ex.printStackTrace();
        return new CommonResponse(500,"UNEXPECTED_ERROR");
    }

    @ExceptionHandler(value = {CustomerException.class})
    public ResponseEntity<CommonResponse> handleCustomException(CustomerException ex , WebRequest webRequest){
        ex.printStackTrace();

        return ResponseEntity.status(ex.getStatus()).body(new CommonResponse(ex.getStatus(), ex.getMessage()));
    }
}
