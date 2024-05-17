package imageapex.main.java.model;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatModel {
    private static final double KB = 1024.0;
    private static final double MB = 1024.0*1024.0;
    private static final double GB = 1024.0*1024.0*1024.0;


    public static String getFormatSize(long fileLength){//将文件大小转字符串返回
        String Standardsize = null;
        if (fileLength < KB){
            Standardsize = String.format("%d Byte", fileLength);
        }else if(fileLength < MB){
            Standardsize = String.format("%.0f KB", fileLength/KB);
        }else if (fileLength < GB){
            Standardsize = String.format("%.2f MB", fileLength/MB);
        }else {
            Standardsize = String.format("%.2f GB", fileLength/GB);
        }
        return Standardsize;
    }


    public static String getFormatTime(long time){//将给定的时间戳（以毫秒为单位）转换为格式化的日期字符串
        Date data = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(data);
    }

    static byte[] getByteByFile(File file){//将一个文件转换为字节数组
        try(FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024)){
            byte[] bytes = new byte[1024];
            int i;
            while ((i = fis.read(bytes)) != -1) {
                bos.write(bytes, 0, i);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean getFileByByte(byte[] bytes, File file) {//将字节数组写入到指定的文件中
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos)){
            bos.write(bytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
