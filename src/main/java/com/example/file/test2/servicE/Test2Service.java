package com.example.file.test2.servicE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class Test2Service {

    public Object job(Map<String, Object> param){
        int val = 0;
        for(int i = 0; i < 10; i++){
            val += i;
        }
        log.debug("val = {}", val);
        return val;
    }

    public void vJob(){
        log.debug("vJOb ==== ");
    }
}
