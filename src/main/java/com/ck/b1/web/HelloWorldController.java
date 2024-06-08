package com.ck.b1.web;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ck.b1.model.Customer;
import com.ck.b1.model.CustomerRepository;
import com.ck.b1.model.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HelloWorldController {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    UserService userService;

    @Autowired
    HttpSession httpSession;

    @GetMapping("/hello-world")
    @ResponseBody
    public List<Customer> sayHello(
            @RequestParam(name = "name", required = false, defaultValue = "Stranger") String name) {
        List<Customer> customers = customerRepository.findByLastName(name);

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        log.info("hello-world auth: authentication.isAuthenticated() = " + authentication.isAuthenticated());
        log.info("hello-world auth: authentication.getPrincipal() = " + authentication.getPrincipal());

        pushToHistory("hello");
        return customers;
    }

    private void pushToHistory(String item) {
        Object o = httpSession.getAttribute("history");
        if (o == null) {
            o = item;
        } else {
            o = o.toString() + "|" + item;
        }
        httpSession.setAttribute("history", o);
    }

    @PostMapping("/create-user")
    @ResponseBody
    public Map<String, Boolean> createUser(@RequestParam() String name, @RequestParam() String password) {

        UserDetails user = userService.createUser(name, password);
        return Collections.singletonMap("creationSuccess", user != null);
    }

    @GetMapping("/greeting")
    public String greeting(Authentication authentication, Model model) {

        log.info("greeting authentication = " + authentication);
        log.info("greeting authentication.getName() = " + authentication.getName());
        log.info("greeting authentication.getPrincipal() = " + authentication.getPrincipal());
        User user = (User) authentication.getPrincipal();
        for (var g : user.getAuthorities()) {
            log.info("\tauthority: " + g.getAuthority());
        }

        pushToHistory("greet");
        model.addAttribute("name", "Csaba");
        model.addAttribute("history", httpSession.getAttribute("history"));

        return "greeting";
    }

    @GetMapping("/user")
    @ResponseBody
    public Principal user(Principal user) {
        return user;
    }
}
