package com.wnsse.sqlRag.enumeration;

/**
 * 问卷题型枚举（简洁版）
 */
public enum QuestionTypeEnum {

    RADIO("qradio", "普通单选", "单选",
            false, false, false,
            "统计及每个选项的作答人数、占比、答题总人数", "各选项占比=各选项作答人数/题目作答总人数"),

    CHECKBOX("qcheckbox", "多选", "多选",
            false, false, false,
            "统计每个选项的作答人数、占比、答题总人数", "各选项占比=各选项作答人数/题目作答总人数"),

    FIVE_DIMENSIONAL("q5dimensional", "单选五维", "单选",
            true, true, false,
            "统计每个选项的作答人数、选项占比、答题总人数，计算满意度（权重5,4,3,2,1）和均值", "各选项占比=各选项作答人数/题目作答总人数，满意度=前三项占比总和，每个选项都有权重，分别为（5,4,3,2,1）均值=(选项1选择总人数*5+选项2选择总人数*4+选项3选择总人数*3+选项4选择总人数*2+选项5选择总人数*1)/样本量"),

    COMBO_RADIO("qcomboradio", "组合单选", "组合单选",
            true, true, true,
            "按子题干分别统计作答子题人数、选项作答人数、占比、满意度、均值、答题总人数", "各子题选项占比=各子题选项作答人数/子题目作答总人数，各子题满意度=各子题前三项占比总和，每个选项都有权重，分别为（5,4,3,2,1）各子题均值=各子题(选项1选择总人数*5+选项2选择总人数*4+选项3选择总人数*3+选项4选择总人数*2+选项5选择总人数*1)/各子题样本量"),

    INPUT("qinput", "填空", "填空",
            false, false, false,
            "统计平均薪酬和薪酬中位数（需映射薪酬字段）", "平均薪酬=作答薪酬总和/作答薪酬总人数，如果薪酬作答总人数是奇数：中位数 = 排序后薪酬的第 (n+1)/2 个数（n为薪酬作答总人数）,如果薪酬作答总人数是偶数：中位数 = (排序后薪酬的第 n/2 个数 + 排序后薪酬的第 (n/2 + 1) 个数) / 2"),

    SELECT("qselect", "下拉", "下拉",
            false, false, false,
            "统计省/市人数及占比，市的人数和占比前十（需映射地区字段）", "各省份占比=选择省份人数/题目作答总人数，各城市占比（前十）=选择城市人数/题目作答总人数");

    private final String code;
    private final String name;
    private final String category;
    private final boolean needSatisfaction;   // 是否需要计算满意度
    private final boolean needMean;           // 是否需要计算均值
    private final boolean isCombo;            // 是否组合题型
    private final String description;
    private final String desc;

    QuestionTypeEnum(String code, String name, String category,
                     boolean needSatisfaction, boolean needMean, boolean isCombo,
                     String description, String desc) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.needSatisfaction = needSatisfaction;
        this.needMean = needMean;
        this.isCombo = isCombo;
        this.description = description;
        this.desc = desc;
    }

    // Getter方法...
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public boolean isNeedSatisfaction() { return needSatisfaction; }
    public boolean isNeedMean() { return needMean; }
    public boolean isCombo() { return isCombo; }
    public String getDescription() { return description; }
    public String getDesc() { return desc; }

    public static QuestionTypeEnum fromCode(String code) {
        for (QuestionTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}