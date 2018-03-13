package ch.so.agi.cccservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/myservice")
public class SpringService {

    @RequestMapping(value="/process", method = RequestMethod.POST)
    public void process(@RequestBody String payload) throws Exception {
        System.out.println(payload);
    }

    @RequestMapping(value="/process", method = RequestMethod.GET)
    public void getprocess(@RequestBody String payload) throws Exception {
        System.out.println(payload);
    }
}
