package com.dyx.voice.function;
 
/**
 * @author Administrator
 * @date 2018/6/8
 */
public class SwitchNumber {

    public static void main(String[] args) {
        SwitchNumber modifyFileName = new SwitchNumber();

        int number = modifyFileName.getIntegerByNumberStr("十二");
        System.out.println(number);

        number = modifyFileName.getIntegerByNumberStr("三十一");
        System.out.println(number);
    }

    /**
     * 支持到12位
     *
     * @param numberStr 中文数字
     * @return int 数字
     */
    public static int getIntegerByNumberStr(String numberStr) {
        // 返回结果
        int sum = 0;
 
        // null或空串直接返回
        if (numberStr == null || ("").equals(numberStr)) {
            return sum;
        }
 
        // 过亿的数字处理
        if (numberStr.indexOf("亿") > 0) {
            String currentNumberStr = numberStr.substring(0, numberStr.indexOf("亿"));
            int currentNumber = testA(currentNumberStr);
            sum += currentNumber * Math.pow(10, 8);
            numberStr = numberStr.substring(numberStr.indexOf("亿") + 1);
        }
 
        // 过万的数字处理
        if (numberStr.indexOf("万") > 0) {
            String currentNumberStr = numberStr.substring(0, numberStr.indexOf("万"));
            int currentNumber = testA(currentNumberStr);
            sum += currentNumber * Math.pow(10, 4);
            numberStr = numberStr.substring(numberStr.indexOf("万") + 1);
        }
 
        // 小于万的数字处理
        if (!("").equals(numberStr)) {
            int currentNumber = testA(numberStr);
            sum += currentNumber;
        }
 
        return sum;
    }
 
    /**
     * 把亿、万分开每4位一个单元，解析并获取到数据
     * @param testNumber
     * @return
     */
    public static int testA(String testNumber) {
        // 返回结果
        int sum = 0;
 
        // null或空串直接返回
        if(testNumber == null || ("").equals(testNumber)){
            return sum;
        }
 
        // 获取到千位数
        if (testNumber.indexOf("千") > 0) {
            String currentNumberStr = testNumber.substring(0, testNumber.indexOf("千"));
            sum += testB(currentNumberStr) * Math.pow(10, 3);
            testNumber = testNumber.substring(testNumber.indexOf("千") + 1);
        }
 
        // 获取到百位数
        if (testNumber.indexOf("百") > 0) {
            String currentNumberStr = testNumber.substring(0, testNumber.indexOf("百"));
            sum += testB(currentNumberStr) * Math.pow(10, 2);
            testNumber = testNumber.substring(testNumber.indexOf("百") + 1);
        }
 
        // 对于特殊情况处理 比如10-19是个数字，十五转化为一十五，然后再进行处理
        if (testNumber.indexOf("十") == 0) {
            testNumber = "一" + testNumber;
        }
 
        // 获取到十位数
        if (testNumber.indexOf("十") > 0) {
            String currentNumberStr = testNumber.substring(0, testNumber.indexOf("十"));
            sum += testB(currentNumberStr) * Math.pow(10, 1);
            testNumber = testNumber.substring(testNumber.indexOf("十") + 1);
        }
 
        // 获取到个位数
        if(!("").equals(testNumber)){
            sum += testB(testNumber.replaceAll("零",""));
        }
 
        return sum;
    }
 
    public static int testB(String replaceNumber) {
        switch (replaceNumber) {
            case "一":
                return 1;
            case "二":
                return 2;
            case "三":
                return 3;
            case "四":
                return 4;
            case "五":
                return 5;
            case "六":
                return 6;
            case "七":
                return 7;
            case "八":
                return 8;
            case "九":
                return 9;
            case "零":
                return 0;
            default:
                return 0;
        }
    }
 
}
