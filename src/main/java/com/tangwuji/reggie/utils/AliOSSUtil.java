package com.tangwuji.reggie.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Component//交给IOC方便后续操作
public class AliOSSUtil {
    //这里利用配置文件进行注入
    private static final String ENDPOINT ="https://oss-cn-beijing.aliyuncs.com";

    private  static final String ACCESS_KEY_ID ="your access key id";

    private static final String ACCESS_KEY_SECRET ="your access key secret";

    private static final String BUCKET_NAME ="your bucket name";

    /**
     * 将文件上传到阿里OSS
     *
     * @param sourceFilePathName 本地文件
     * @param aimFilePathName    在阿里OSS中保存的可以包含路径的文件名
     * @return 返回上传后文件的访问路径
     * @throws FileNotFoundException
     */
    public String upload(String sourceFilePathName, String aimFilePathName) throws FileNotFoundException {
        FileInputStream is = new FileInputStream(sourceFilePathName);

        if (aimFilePathName.startsWith("/")) {
            aimFilePathName = aimFilePathName.substring(1);
        }

        // 如果需要上传时设置存储类型与访问权限，请参考以下示例代码。
        ObjectMetadata metadata = new ObjectMetadata();
        int indexOfLastDot = aimFilePathName.lastIndexOf(".");
        String suffix = aimFilePathName.substring(indexOfLastDot);
        metadata.setContentType(getContentType(suffix));

        //避免文件覆盖
        aimFilePathName = aimFilePathName.substring(0, indexOfLastDot) + System.currentTimeMillis() + suffix;

        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, aimFilePathName, is);
        //避免访问时将图片下载下来
        putObjectRequest.setMetadata(metadata);

        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        ossClient.putObject(putObjectRequest);

        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 100);
        URL url = ossClient.generatePresignedUrl(BUCKET_NAME, aimFilePathName, expiration);

        // 关闭ossClient
        ossClient.shutdown();

        return url.toString();
    }

    /**
     * 网络实现上传头像到OSS
     *
     * @param multipartFile
     * @return
     */
    public static String upload(MultipartFile multipartFile) throws IOException {
        // 获取上传的文件的输入流
        InputStream inputStream = multipartFile.getInputStream();
        // 获取文件名称
        String fileName = multipartFile.getOriginalFilename();

        // 避免文件覆盖
        int i = fileName.lastIndexOf(".");
        String suffix = fileName.substring(i);
        fileName = fileName.substring(0, i) + System.currentTimeMillis() + suffix;

        // 把文件按照日期进行分类
        // 获取当前日期
        //String datePath = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
        // 拼接fileName
        //fileName = datePath + "/" + fileName;

        // 如果需要上传时设置存储类型与访问权限
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(getContentType(fileName.substring(fileName.lastIndexOf("."))));

        // 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, fileName, inputStream);
        putObjectRequest.setMetadata(metadata);

        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        ossClient.putObject(putObjectRequest);

        //文件访问路径
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 100);
        URL url = ossClient.generatePresignedUrl(BUCKET_NAME, fileName, expiration);

        // 关闭ossClient
        ossClient.shutdown();
        // 把上传到oss的路径返回
        return url.toString();
    }

    /**
     * 返回contentType
     *
     * @param FileNameExtension
     * @return
     */
    private static String getContentType(String FileNameExtension) {
        if (FileNameExtension.equalsIgnoreCase(".bmp")) {
            return "image/bmp";
        }
        if (FileNameExtension.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (FileNameExtension.equalsIgnoreCase(".jpeg") ||
                FileNameExtension.equalsIgnoreCase(".jpg") ||
                FileNameExtension.equalsIgnoreCase(".png")
        ) {
            return "image/jpg";
        }
        return "image/jpg";
    }


    /**
     * 列举 指定路径下所有的文件的文件名
     * 如果要列出根路径下的所有文件，path= ""
     *
     * @param path
     * @return
     */
    public List<String> listFileName(String path) {
        List<String> res = new ArrayList<>();
        // 构造ListObjectsRequest请求。
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(BUCKET_NAME);

        // 设置prefix参数来获取fun目录下的所有文件。
        listObjectsRequest.setPrefix(path);

        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        // 列出文件。
        ObjectListing listing = ossClient.listObjects(listObjectsRequest);
        // 遍历所有文件
        for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
            System.out.println(objectSummary.getKey());
        }
        // 关闭OSSClient。
        ossClient.shutdown();
        return res;
    }

    /**
     * 列举文件下所有的文件url信息
     */
    public List<String> listFileUrl(String path) {
        List<String> res = new ArrayList<>();

        // 构造ListObjectsRequest请求
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(BUCKET_NAME);

        // 设置prefix参数来获取fun目录下的所有文件。
        listObjectsRequest.setPrefix(path);

        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        // 列出文件。
        ObjectListing listing = ossClient.listObjects(listObjectsRequest);
        // 遍历所有文件。

        for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
            //文件访问路径
            Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 100);
            URL url = ossClient.generatePresignedUrl(BUCKET_NAME, objectSummary.getKey(), expiration);
            res.add(url.toString());
        }
        // 关闭OSSClient。
        ossClient.shutdown();
        return res;
    }

    /**
     * 判断文件是否存在
     *
     * @param objectName
     * @return
     */
    public  boolean isFileExist(String objectName) {
        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        boolean res = ossClient.doesObjectExist(BUCKET_NAME, objectName);
        return res;
    }

    /**
     * 通过文件名下载文件
     *
     * @param objectName    要下载的文件名
     * @param localFileName 本地要创建的文件名
     */
    public  void downloadFile(String objectName, String localFileName) {
        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        // 下载OSS文件到本地文件。如果指定的本地文件存在会覆盖，不存在则新建。
        ossClient.getObject(new GetObjectRequest(BUCKET_NAME, objectName), new File(localFileName));
        // 关闭OSSClient。
        ossClient.shutdown();
    }

    /**
     * 删除文件或目录
     *
     * @param objectName
     */
    public void delelteFile(String objectName) {
        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        ossClient.deleteObject(BUCKET_NAME, objectName);
        ossClient.shutdown();
    }

    /**
     * 批量删除文件或目录
     *
     * @param keys
     */
    public  void deleteFiles(List<String> keys) {
        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        // 删除文件。
        DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(new DeleteObjectsRequest(BUCKET_NAME).withKeys(keys));
        List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();

        ossClient.shutdown();
    }
    /**
     * 创建文件夹
     *
     * @param folder
     * @return
     */
    public  String createFolder(String folder) {
        // 文件夹名
        final String keySuffixWithSlash = folder;
        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        // 判断文件夹是否存在，不存在则创建
        if (!ossClient.doesObjectExist(BUCKET_NAME, keySuffixWithSlash)) {
            // 创建文件夹
            ossClient.putObject(BUCKET_NAME, keySuffixWithSlash, new ByteArrayInputStream(new byte[0]));
            // 得到文件夹名
            OSSObject object = ossClient.getObject(BUCKET_NAME, keySuffixWithSlash);
            String fileDir = object.getKey();
            ossClient.shutdown();
            return fileDir;
        }

        return keySuffixWithSlash;
    }

}
