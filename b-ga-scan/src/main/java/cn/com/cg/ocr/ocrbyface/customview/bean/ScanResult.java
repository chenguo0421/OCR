package cn.com.cg.ocr.ocrbyface.customview.bean;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/19 11:51
 */
public class ScanResult {
    public String id;
    public String path;

    public ScanResult(String id, String tempPath) {
        this.id = id;
        this.path = tempPath;
    }
}
