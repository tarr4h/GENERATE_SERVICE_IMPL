package com.example.file;

import com.example.file.module.GenerateFile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class FileApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(FileApplication.class, args);
        GenerateFile ge = new GenerateFile();
        ge.run();
    }

}
