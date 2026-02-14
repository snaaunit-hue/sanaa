package ye.gov.sanaa.healthoffice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController implements ErrorController {

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        String accept = request.getHeader("Accept");
        boolean wantsHtml = accept != null && accept.contains("text/html");

        if (status != null && Integer.parseInt(status.toString()) == 404 && wantsHtml) {
            return "forward:/index.html";
        }

        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        return ResponseEntity.status(statusCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"" + HttpStatus.valueOf(statusCode).getReasonPhrase() + "\"}");
    }
}
