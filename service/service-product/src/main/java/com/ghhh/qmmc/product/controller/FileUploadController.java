package com.ghhh.qmmc.product.controller;

import com.ghhh.qmmc.product.service.FileUploadService;
import com.ghhh.qmmc.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("admin/product")
@Api(tags = "文件上传接口")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @ApiOperation("文件上传")
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception {
        String url = fileUploadService.fileUpload(file);
        return Result.ok(url);
    }

}
