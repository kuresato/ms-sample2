package sample2;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
@EnableAutoConfiguration
class Hello {

    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World (ms-sample2) !";
    }

    @RequestMapping("/foo")
    @ResponseBody
    String foo() {
        return "foo";
    }
    @RequestMapping("/bar")
    @ResponseBody
    String bar() {
        return "bar";
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Hello World.");
        SpringApplication.run(Hello.class, args);
    }
}
