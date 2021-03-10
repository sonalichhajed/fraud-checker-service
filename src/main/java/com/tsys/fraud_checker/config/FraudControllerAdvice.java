package com.tsys.fraud_checker.config;

import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * org.springframework.web.util.NestedServletException:
 * Request processing failed; nested exception is java.lang.IllegalStateException:
 * JSR-303 validated property 'creditCard.number' does not have a corresponding
 * accessor for Spring data binding - check your DataBinder's configuration
 * (bean property versus direct field access)
 * <p>
 * at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)
 * at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:909)
 * at javax.servlet.http.HttpServlet.service(HttpServlet.java:652)
 * at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883)
 * ...
 * ...
 * <p>
 * Solution 1: One starts adding Getters/Setters for a domain object. They
 * generally break encapsulation in many cases, esp. domain objects.
 * Instead make your objects immutable and fields public. If at all,
 * due to compulsions, you need to add Getters/Setters, mark them
 * Deprecated.
 * <p>
 * Solution 2: Introduce a Controller Advice like this and add a method that
 * activates Direct Field Access on the DataBinder when the
 * ControllerAdvice kicks-in to do its work with InitBinder
 */

@ControllerAdvice
public class FraudControllerAdvice {

    @InitBinder
    private void activateDirectFieldAccess(DataBinder dataBinder) {
        dataBinder.initDirectFieldAccess();
    }
}
