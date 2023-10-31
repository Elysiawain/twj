package com.tangwuji.reggie.controller;

import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.utils.AliOSSUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

//主要实现文件的上传和下载
@RestController
@Slf4j
@RequestMapping("/common")
public class CommonsController {
    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {//上传成功就返回对应图片的访问地址
        log.info("文件上传");
        String imgURL = AliOSSUtil.upload(file);
        log.info("文件访问路径：{}",imgURL);
        String[] split = imgURL.split("\\?");
        log.info("文件访问路径：{}",split[0]);
        String imageUrl=split[0];
        return R.success(imageUrl,"success");
    }

}
