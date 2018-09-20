package com.scj.pic_server.controller;

import com.scj.pic_server.util.Md5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FileUtils;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;

public class PicController {
    private static Logger log = LoggerFactory.getLogger(PicController.class);

    /**
     * 上传图片生成二维码
     * @param request
     * @param mult
     */
    public void uploadPic(HttpServletRequest request, MultipartFile mult){
        try {
            log.info("开始上传文件准备:文件名:" + mult.getName());
            String[] str = mult.getOriginalFilename().split("\\.", 2);
            String fileDir = System.getProperty("user.dir") + "/";
            String newFileName = new Date().getTime()+ Md5Util.getSalt()+"."+str[1];
            File file = new File(fileDir + newFileName);
            FileUtils.copyInputStreamToFile(mult.getInputStream(), file);
            //上传文件到云端,返回图片地址

            String yunUrl="";

        }catch(Exception e){

        }

    }

}
