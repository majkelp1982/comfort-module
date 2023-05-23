package pl.smarthouse.comfortmodule.exceptions;

import org.springframework.http.HttpStatus;

public class VentilationModuleServiceResponseException extends RuntimeException {
  public VentilationModuleServiceResponseException(
      final HttpStatus httpStatus, final String response) {
    super(String.format("Http status code: %s, response: %s", httpStatus, response));
  }
}
