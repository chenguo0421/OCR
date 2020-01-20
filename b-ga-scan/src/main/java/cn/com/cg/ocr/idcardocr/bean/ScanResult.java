package cn.com.cg.ocr.idcardocr.bean;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/17 11:14
 */
public class ScanResult {
    public String id;
    public String path;

    public ScanResult(String id, String tempPath) {
        this.id = id;
        this.path = tempPath;
    }
}
