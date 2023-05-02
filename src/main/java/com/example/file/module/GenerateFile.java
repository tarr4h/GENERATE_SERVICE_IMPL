package com.example.file.module;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOExceptionList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

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
@Slf4j
public class GenerateFile {

    public final String myDir = "C:\\WORKSPACE\\FILE_WORK\\FILE\\src\\main\\java\\";

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
                                List<String> srvcNames = new ArrayList<>();

                                File[] serviceFiles = new File(srvcDir).listFiles();
                                if(serviceFiles != null){
                                    Path implPath = Paths.get(prntsDir + "\\serviceImpl");
                                    Files.createDirectories(implPath);

                                    for(File serviceFile : serviceFiles){
                                        result.add(serviceFile.getName());

                                        String serviceName = FilenameUtils.getBaseName(serviceFile.getName());
                                        srvcNames.add(serviceName);

                                        // file 내부 Impl로 변환
                                        Path srvcFilePath = Paths.get(srvcDir + "\\" + serviceFile.getName());
                                        byte[] bytes = Files.readAllBytes(srvcFilePath);
                                        String content = new String(bytes);
                                        String srvcFilePathStr = srvcFilePath.toString();
                                        String importStr = srvcFilePathStr.replace(myDir, "")
                                                .replace("\\", ".").replace(".java", "");
                                        String rplc = content.replace(serviceName, serviceName + "Impl")
                                                .replace("class " + serviceName + "Impl",  "class "+ serviceName + "Impl implements " + serviceName)
                                                .replace(".service;", ".serviceImpl;\n\nimport " + importStr + ";");

                                        try{
                                            BufferedWriter br = Files.newBufferedWriter(srvcFilePath,
                                                    StandardOpenOption.TRUNCATE_EXISTING);
                                            Files.write(srvcFilePath, rplc.getBytes());
                                        } catch (IOException e){
                                            e.printStackTrace();
                                        }
                                        ///////

                                        // 파일명 변경 및 위치 이동
                                        File toImplFile = new File(implPath + "\\" + serviceName + "Impl.java");
                                        FileUtils.moveFile(serviceFile, toImplFile);
                                    }
                                    // 디렉토리를 serviceImpl로 변경
                                    File toImplDir = new File(prntsDir + "\\serviceImpl");
                                    file.renameTo(toImplDir);

                                    Path path = Paths.get(prntsDir + "\\service");
                                    Files.createDirectories(path);
                                    for(String srvcName : srvcNames){
                                        File serviceImplJava = new File(path + "\\" + srvcName + ".java");
                                        serviceImplJava.createNewFile();
                                        Path created = Paths.get(path + "\\" + srvcName + ".java");

                                        String pathStr = path.toString();
                                        String packageStr = pathStr.replace(myDir, "")
                                                .replace("\\", ".") + ";";

                                        String str = "package " + packageStr + "\n\npublic interface " + srvcName + "{\n\n\n\n}";
                                        Files.write(created, str.getBytes());
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
