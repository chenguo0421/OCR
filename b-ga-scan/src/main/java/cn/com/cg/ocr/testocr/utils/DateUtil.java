package cn.com.cg.ocr.testocr.utils;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/16 19:19
 */
public class DateUtil {
    public static String getDateFormatString(Date date) {
        String dateString = null;
        if (null != date) {
            SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd_HHmmss");
            dateString = format.format(date);
        }

        return dateString;
    }
}
