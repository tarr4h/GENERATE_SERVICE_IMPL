package com.example.file.module;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * <pre>
 * com.example.file.module.GenerateFile
 *  - GenerateFile.java
 * </pre>
 *
 * @author : tarr4h
 * @ClassName : GenerateFile
 * @description :
 * @date : 2023-05-02
 */
@SuppressWarnings("unchecked")
@Slf4j
public class GenerateFile {

    public final String myDir = "C:\\DEV\\WORKSPACE\\GENERATE_SERVICE_IMPL\\src\\main\\java\\";

    public void run() throws IOException {
        String baseDir = "src/main/java/com/example/file";
        List<String> serviceList = trvDir(baseDir);
        log.debug("baseDir = {}", baseDir);
        log.debug("------------------------ RUN -------------------------");
        log.debug("Total count = {}", serviceList.size());
        for(String service : serviceList){
            log.debug("------------------------------------------------------");
            log.debug("** genereated Service = [ {} ]", service);
            log.debug("------------------------------------------------------");
        }
        log.debug("--------------------------END-------------------------");
    }



    public List<String> trvDir(String dir) throws IOException {
        List<String> result = new ArrayList<>();

        File[] files = new File(dir).listFiles();
        if(files != null){
            for(File file : files){
                if(file.isDirectory()){
                    if(file.getName().contains("service")){
                        String prntsDir = file.getParentFile().getAbsolutePath();
                        File[] prntsFiles = new File(prntsDir).listFiles();
                        boolean prntsBool = true;
                        if(prntsFiles != null){
                            for(File prntsFile : prntsFiles){
                                if(prntsFile.getName().contains("serviceImpl")){
                                    prntsBool = false;
                                    break;
                                };
                            }

                            if(prntsBool){
                                String srvcDir = file.getAbsolutePath();
                                List<Map<String, Object>> srvcList = new ArrayList<>();

                                File[] serviceFiles = new File(srvcDir).listFiles();
                                if(serviceFiles != null){
                                    Path implPath = Paths.get(prntsDir + "\\serviceImpl");
                                    Files.createDirectories(implPath);

                                    for(File serviceFile : serviceFiles){
                                        result.add(serviceFile.getName());

                                        String serviceName = FilenameUtils.getBaseName(serviceFile.getName());
                                        Map<String, Object> srvcMap = new HashMap<>();
                                        srvcMap.put("name", serviceName);
                                        srvcMap.put("lines", Files.readAllLines(Paths.get(serviceFile.getAbsolutePath())));
                                        srvcList.add(srvcMap);

                                        Path srvcFilePath = Paths.get(srvcDir + "\\" + serviceFile.getName());
                                        byte[] bytes = Files.readAllBytes(srvcFilePath);
                                        String content = new String(bytes);
                                        String srvcFilePathStr = srvcFilePath.toString();
                                        String importStr = srvcFilePathStr.replace(myDir, "")
                                                .replace("\\", ".").replace(".java", "");
                                        String rplc = content.replace(serviceName, serviceName + "Impl")
                                                .replace("class " + serviceName + "Impl",  "class "+ serviceName + "Impl implements " + serviceName)
                                                .replace(".service;", ".serviceImpl;\n\nimport " + importStr + ";")
                                                .replace("public", "@Override\n\tpublic")
                                                .replace("@Override\n\tpublic class", "public class");

                                        try{
                                            BufferedWriter br = Files.newBufferedWriter(srvcFilePath,
                                                    StandardOpenOption.TRUNCATE_EXISTING);
                                            Files.write(srvcFilePath, rplc.getBytes());
                                        } catch (IOException e){
                                            e.printStackTrace();
                                        }

                                        File toImplFile = new File(implPath + "\\" + serviceName + "Impl.java");
                                        FileUtils.moveFile(serviceFile, toImplFile);
                                    }

                                    File toImplDir = new File(prntsDir + "\\serviceImpl");
                                    file.renameTo(toImplDir);

                                    Path path = Paths.get(prntsDir + "\\service");
                                    Files.createDirectories(path);
                                    for(Map<String, Object> srvcMap : srvcList){
                                        String srvcName = (String) srvcMap.get("name");
                                        List<String> lines = (List<String>) srvcMap.get("lines");

                                        File serviceImplJava = new File(path + "\\" + srvcName + ".java");
                                        serviceImplJava.createNewFile();
                                        Path created = Paths.get(path + "\\" + srvcName + ".java");

                                        String pathStr = path.toString();
                                        String packageStr = pathStr.replace(myDir, "")
                                                .replace("\\", ".") + ";";

                                        StringBuilder str = new StringBuilder();
                                        str.append("package ").append(packageStr).append("\n");

                                        for(String line : lines){
                                            boolean importBool = line.contains("import");
                                            boolean annotationBool = line.contains("stereotype.Service");
                                            boolean logBool = line.contains("Slf4j");
                                            if(importBool && !annotationBool && !logBool){
                                                str.append("\n").append(line).append(";");
                                            }
                                        }

                                        str.append("\n\npublic interface ").append(srvcName).append("{\n\n");

                                        for(String line : lines){
                                            boolean pubBool = line.contains("public");
                                            boolean prvBool = line.contains("private");
                                            boolean clsBool = line.contains("class");
                                            if((pubBool || prvBool) && !clsBool){
                                                String rplc = line.replace("public", "")
                                                        .replace("private", "")
                                                        .replace("{", ";");
                                                str.append(rplc).append("\n\n");
                                            }
                                        }

                                        str.append("}");

                                        Files.write(created, str.toString().getBytes());
                                    }
                                }
                            }
                        }
                    } else {
                        result.addAll(trvDir(file.getAbsolutePath()));
                    }
                }
            }
        }

        return result;
    }
}
