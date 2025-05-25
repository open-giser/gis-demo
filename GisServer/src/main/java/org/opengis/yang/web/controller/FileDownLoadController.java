package org.opengis.yang.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;

/**
 * 对比传统文件流下载与异部文件流下载的
 */
@RestController
@RequestMapping("/file")
public class FileDownLoadController {

    @GetMapping("/downloadsync/{id}")
    public void downLoadSync(HttpServletResponse response, @PathVariable("id") String id) throws IOException {
        // 传统同步下载示例（非异步）
        System.out.println("当前线程名称：" + Thread.currentThread().getName());
        File file = new File("H:\\初心CSO新客户端.zip");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=初心CSO新客户端.zip");
        response.setContentLengthLong(file.length()); // 设置文件大小
        try (InputStream is = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            System.out.println("当前线程名称：" + Thread.currentThread().getName());
             byte[] buffer = new byte[1024];
             int bytesRead;
             while ((bytesRead = is.read(buffer)) != -1) {
                 os.write(buffer, 0, bytesRead); // 同步写入，阻塞当前线程
             }
        }
    }


    // 异步流式下载示例
    @GetMapping("/downloadasync")
    public ResponseEntity<StreamingResponseBody> downloadAsync() {
        System.out.println("当前线程名称：" + Thread.currentThread().getName());
        File file = new File("H:\\初心CSO新客户端.zip");
        StreamingResponseBody body = outputStream -> {
            try (InputStream is = new FileInputStream(file)) {
                System.out.println("当前线程名称：" + Thread.currentThread().getName());
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead); // 异步写入，由框架管理线程
                }
            }
        };

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=file.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(body);
    }


    @GetMapping("/download2")
    public StreamingResponseBody download2(HttpServletResponse response) {
        // 1. 直接操作 HttpServletResponse 设置头
        response.setHeader("Content-Disposition", "attachment; filename=file.bin");
        response.setContentType("application/octet-stream");

        // 2. 返回 StreamingResponseBody
        return outputStream -> {
            try (InputStream is = new FileInputStream("H:\\初心CSO新客户端.zip")) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        };
    }
}
