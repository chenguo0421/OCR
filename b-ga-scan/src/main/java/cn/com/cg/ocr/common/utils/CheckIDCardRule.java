package cn.com.cg.ocr.common.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

/**
 * Discription  {身份证号码, 可以解析身份证号码的各个字段，以及验证身份证号码是否有效身份证号码构成：6位地址编码+8位生日+3位顺序码+1位校验码}
 * author  chenguo7
 * Date  2020/1/20 16:34
 */
public class CheckIDCardRule {
    /**
     * 身份证号码中的出生日期的格式
     */
    private static final String BIRTH_DATE_FORMAT = "yyyyMMdd";
    /**
     * 身份证的最小出生日期,1900年1月1日
     */
    private static final Date MIN_BIRTH_DATE = new Date(-2209017600000L);
    /**
     * 新版身份证号码长度
     */
    private static final int NEW_CARD_NUMBER_LENGTH = 18;
    /**
     * 旧版身份证号码长度
     */
    private static final int OLD_CARD_NUMBER_LENGTH = 15;
    /**
     * 18位身份证中最后一位校验码
     */
    private static final char[] VERIFY_CODE = {'1', '0', 'X', '9', '8', '7',
            '6', '5', '4', '3', '2'};
    /**
     * 18位身份证中，各个数字的生成校验码时的权值
     */
    private static final int[] VERIFY_CODE_WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1,
            6, 3, 7, 9, 10, 5, 8, 4, 2};
    /**
     * 完整的身份证号码
     */
    private final String cardNumber;
    /**
     * 缓存身份证是否有效，因为验证有效性使用频繁且计算复杂
     */
    private Boolean cacheValidateResult = null;
    /**
     * 缓存出生日期，因为出生日期使用频繁且计算复杂
     */
    private Date cacheBirthDate = null;
    
    


    
    /**
     * 当前时间
     */
    private Date currentDate = new Date();

    public CheckIDCardRule(String cardNumber, String serTime) {

        if (null != cardNumber) {
            cardNumber = cardNumber.trim();
            if (OLD_CARD_NUMBER_LENGTH == cardNumber.length()) {
                // 如果是15位身份证号码，则自动转换为18位
                cardNumber = contertToNewCardNumber(cardNumber);
            }
        }
        if (null != serTime) {
            currentDate = strToDate(serTime, "yyyyMMddhhmmss");
        }
        this.cardNumber = cardNumber;
    }

    /**
     * @return boolean
     * @Title: validate
     * @Description: 身份证号码校验
     * @author
     */
    public boolean validate() {
        if (null == this.cacheValidateResult) {
            boolean result = true;
            try {
                // 身份证号码不能为空
                result = result && (null != this.cardNumber);
                // 身份证号长度是18(新证)
                result = result
                        && NEW_CARD_NUMBER_LENGTH == this.cardNumber.length();
                char ch;
                // 身份证号的前17位必须是阿拉伯数字
                for (int i = 0; i < NEW_CARD_NUMBER_LENGTH - 1; i++) {
                    ch = cardNumber.charAt(i);
                    result = result && ch >= '0' && ch <= '9';
                }
                // 身份证号的第18位校验正确
                result = result
                        && (calculateVerifyCode(cardNumber) == cardNumber
                        .charAt(NEW_CARD_NUMBER_LENGTH - 1));
                // 出生日期不能晚于当前时间，并且不能早于1900年
                Date birthDate = getBirthDate();
                result = result && null != birthDate;
                result = result && birthDate.before(currentDate);
                result = result && birthDate.after(MIN_BIRTH_DATE);

                String birthdayPart = getBirthDayPart();
                String realBirthdayPart = this.createBirthDateParser().format(
                        birthDate);
                result = result && (birthdayPart.equals(realBirthdayPart));
            } catch (Exception e) {
                Log.e("方法执行失败:validate()", e.toString());
                result = false;
            }
            // 完整身份证号码的省市县区检验规则
            cacheValidateResult = Boolean.valueOf(result);
        }
        return cacheValidateResult;
    }

    /**
     * 功能：设置地区编码
     *
     * @return Hashtable 对象
     */
    private static Hashtable<String, String> GetAreaCode() {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }

    /**
     * @return String
     * @Title: getAddressCode
     * @Description: 获取身份证号码中的地址编码
     * @author
     */
    private String getAddressCode() {
        checkIfValid();
        Hashtable<String, String> h = GetAreaCode();
        if (h.get(cardNumber.substring(0, 2)) == null) {
            throw new RuntimeException("身地区编码不正确！");
        }
        // return this.cardNumber.substring(0, 6);
        return h.get(cardNumber.substring(0, 2));
    }

    /**
     * @return java.util.Date
     * @Title: getBirthDate
     * @Description: 获取身份证号码中的生日
     * @author
     */
    public Date getBirthDate() {

        if (null == this.cacheBirthDate) {
            try {
                this.cacheBirthDate = createBirthDateParser().parse(
                        getBirthDayPart());
            } catch (ParseException e) {
                Log.e("解析生日失败!", e.toString());
            } catch (Exception e) {
                Log.e("解析生日失败!", e.toString());
            }
        }
        return new Date(this.cacheBirthDate.getTime());
    }

    /**
     * @return boolean
     * @Title: isMale
     * @Description: 判断是否为男性
     * @author
     */
    public boolean isMale() {
        return 1 == getGenderCode();
    }

    /**
     * @return boolean
     * @Title: isMale
     * @Description: 判断是否为女性
     * @author
     */
    public boolean isFemal() {
        return false == isMale();
    }

    /**
     * @return int
     * @Title: getGenderCode
     * @Description: 获取身份证的第17位，奇数为男性，偶数为女性
     * @author
     */
    private int getGenderCode() {
        checkIfValid();
        char genderCode = this.cardNumber.charAt(NEW_CARD_NUMBER_LENGTH - 2);
        return (((int) (genderCode - '0')) & 0x1);
    }

    private String getBirthDayPart() {
        return this.cardNumber.substring(6, 14);
    }

    private SimpleDateFormat createBirthDateParser() {
        return new SimpleDateFormat(BIRTH_DATE_FORMAT);
    }

    private void checkIfValid() {
        if (false == validate()) {
            throw new RuntimeException("身份证号码不正确！");
        }
    }

    /**
     * @param cardNumber
     * @return char
     * @Title: calculateVerifyCode
     * @Description: 校验码（第十八位数）
     * 十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0...16 ，先对前17位数字的权求和
     * Ai:表示第i位置上的身份证号码数字值
     * Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
     * 计算模 Y = mod(S, 11)
     * 通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9 8 7 6 5 4 3 2
     */
    private static char calculateVerifyCode(CharSequence cardNumber) {

        int sum = 0;

        char ch;

        for (int i = 0; i < NEW_CARD_NUMBER_LENGTH - 1; i++) {
            ch = cardNumber.charAt(i);
            sum += ((int) (ch - '0')) * VERIFY_CODE_WEIGHT[i];
        }
        return VERIFY_CODE[sum % 11];
    }

    /**
     * @param oldCardNumber 15位身份证号码
     * @Title: contertToNewCardNumber
     * @Description: 把15位身份证号码转换到18位身份证号码
     * @return:
     */
    private static String contertToNewCardNumber(String oldCardNumber) {
        /*
         * 15位身份证号码与18位身份证号码的区别为：
         * 1: 15位身份证号码中，"出生年份"字段是2位，转换时需要补入"19"，表示20世纪;
         * 2: 15位身份证无最后一位校验码。18位身份证中，校验码根据根据前17位生成
         */
        StringBuilder buf = new StringBuilder(NEW_CARD_NUMBER_LENGTH);
        buf.append(oldCardNumber.substring(0, 6));
        buf.append("19");
        buf.append(oldCardNumber.substring(6));
        buf.append(CheckIDCardRule.calculateVerifyCode(buf));
        return buf.toString();
    }

    /**
     * @return the cardNumber
     */
    public String getCardNumber() {
        return cardNumber;
    }


    /**
     * String 转换成 时间
     *
     * @param str
     * @return
     */
    private static Date strToDate(String str, String patten) {
        if (str != null) {
            if (patten == null)
                patten = "yyyy-MM-dd";
            SimpleDateFormat formatter = new SimpleDateFormat(patten);
            try {
                Date dt = null;
                dt = formatter.parse(str);
                return dt;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
