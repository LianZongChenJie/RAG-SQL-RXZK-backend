SQL 生成规则
================

> **目标数据库：MySQL 5.7.28**
> 生成的所有 SQL 必须兼容 MySQL 5.7.28 语法。
> **注意：MySQL 5.7 不支持窗口函数（如 `OVER()`、`ROW_NUMBER()`、`RANK()`、`DENSE_RANK()`），请勿在 SQL 中使用。**

本文档定义了 AI 智能体在根据用户问题和表结构生成 SQL 时必须遵守的规则。
请严格按照以下规则生成对应的 SQL 查询语句。
就业流向问题要对应tbobjectivedata系列表中的相应数据字段，添加行营查询条件，如nSystemEmploymentAreaGroup=2这类就业就业流向条件

---

**重要** 
七大生源地和七大地理区域交叉严格按照sql范式形式生成sql
国家战略区域就业流向只生成一条sql

## 用户问题关键词 → 字段映射规则

| 用户问法关键词 | 对应字段 | strCode 值 |
|--------------|---------|-----------|
| 广东地区、广东省 | strEmploymentGuangDongArea1Code | EmploymentGuangDongArea1Code |
| 三大地理区域、3MG | strEmployment3MGArea1Code | Employment3MGArea1Code |
| 七大地理区域、7MG | strEmployment7MGArea1Code | Employment7MGArea1Code |
| 四大经济区域、4ME | strEmployment4MEArea1Code | Employment4MEArea1Code |
| 就业省、省份 | strEmploymentProvinceCode | Location1Code |
| 就业市、城市 | strEmploymentCityCode | Location2Code |

**重要**：当用户问题中包含具体区域名称（如"广东"）时，必须优先匹配对应的具体字段，而非通用字段。

### 0. 数据表结构与字典映射

#### 基础数据表：`tbobjectivedata`、`tbobjectivedata0`、`tbobjectivedata1`、`tbobjectivedata...`
所有客观表的表结构完全一致,该表存储学生就业的客观数据。

| 字段名 | 类型 | 描述                                    |
| :--- | :--- |:--------------------------------------|
| `lId` | bigint(20) | 主键id                                  |
| `lOrgId` | bigint(20) | 机构id                                  |
| `lDataSetId` | bigint(20) | 数据集id                                 |
| `strStudentCode` | varchar(50) | 学号                                    |
| `strStudentName` | varchar(100) | 姓名                                    |
| `nGraduateYear` | int(4) | 毕业年份                                  |
| `strGender` | varchar(10) | 性别                                    |
| `nGender` | int(4) | 性别 1-男生，2-女生                          |
| `strDegree` | varchar(10) | 学历                                    |
| `strDegreeCode` | varchar(10) | 学历编码                                  |
| `strDepartment` | varchar(100) | 学院                                    |
| `strSpeciality` | varchar(100) | 专业                                    |
| `strPolitical` | varchar(50) | 政治面貌                                  |
| `strPoliticalCode` | varchar(10) | 政治面貌编码                                |
| `strRace` | varchar(50) | 民族                                    |
| `strRaceCode1` | varchar(10) | 民族编码1                                 |
| `strRaceCode2` | varchar(10) | 民族编码2                                 |
| `strDifficult` | varchar(50) | 困难生类别                                 |
| `nDifficult` | int(4) | 困难生类别 1-非困难生，2-困难生                    |
| `strSpecialtyAttribute` | varchar(50) | 学生（专业）属性                              |
| `strOriginLocation` | varchar(500) | 生源所在地                                 |
| `strOriginProvinceCode` | varchar(10) | 生源省编码                                 |
| `strOriginCityCode` | varchar(10) | 生源市编码                                 |
| `nOriginInsideOrOutside` | int(4) | 省内外生源 1-省内生源，2-省外生源                   |
| `strOrigin3MGArea1Code` | varchar(10) | 三大地理区域生源编码                            |
| `strOrigin4MEArea1Code` | varchar(10) | 四大经济区域生源编码                            |
| `strOrigin7MGArea1Code` | varchar(10) | 七大地理区域生源编码                            |
| `strOrigin8MEArea1Code` | varchar(10) | 八大经济区域生源编码                            |
| `strOriginGuangDongArea1Code` | varchar(10) | 广东省生源区域编码                             |
| `strGraduation` | varchar(100) | 原始毕业去向                                |
| `strGraduationType` | varchar(100) | 毕业去向类别-高校                             |
| `strSystemGraduationType` | varchar(100) | 毕业去向类别-系统                             |
| `strGraduationCategory` | varchar(100) | 毕业去向大类-高校                             |
| `strSystemGraduationCategory` | varchar(100) | 毕业去向大类-系统                             |
| `strGraduationType1` | varchar(100) | 毕业去向类别-高校临时                           |
| `strGraduationCategory1` | varchar(100) | 毕业去向大类-高校临时                           |
| `strCompanyName` | varchar(500) | 单位名称                                  |
| `strCompanyLocation` | varchar(500) | 单位所在地                                 |
| `strEmploymentProvinceCode` | varchar(10) | 就业省编码                                 |
| `strEmploymentCityCode` | varchar(10) | 就业市编码                                 |
| `strEmployment3MGArea1Code` | varchar(10) | 三大地理区域就业编码                            |
| `strEmployment4MEArea1Code` | varchar(10) | 四大经济区域就业编码                            |
| `strEmployment7MGArea1Code` | varchar(10) | 七大地理区域就业编码                            |
| `strEmployment8MEArea1Code` | varchar(10) | 八大经济区域就业编码                            |
| `strEmployment8MEArea1Code2` | varchar(10) | 八大经济区域2就业编码                           |
| `strEmploymentCityTypeCode` | varchar(10) | 就业城市类别编码                              |
| `strEmploymentGuangDongArea1Code` | varchar(10) | 广东省就业区域编码                             |
| `nEmploymentInsideOrOutside` | int(4) | 省内外就业 1-省内就业，2-省外就业                   |
| `nEmploymentLocal` | int(4) | 学校属地市就业 1-否，2-是                       |
| `nSystemEmploymentAreaGroup` | int(4) | 系统就业地区分析群体 1-否，2-是                    |
| `nEmploymentAreaGroup` | int(4) | 高校就业地区分析群体 1-否，2-是                    |
| `nSystemEmploymentCompanyGroup` | int(4) | 系统就业单位分析群体 1-否，2-是                    |
| `nEmploymentCompanyGroup` | int(4) | 高校就业单位分析群体 1-否，2-是                    |
| `nSystemEmploymentIndustryGroup` | int(4) | 系统就业行业分析群体 1-否，2-是                    |
| `nEmploymentIndustryGroup` | int(4) | 高校就业行业分析群体 1-否，2-是                    |
| `nSystemEmploymentProfessionGroup` | int(4) | 系统就业职业分析群体 1-否，2-是                    |
| `nEmploymentProfessionGroup` | int(4) | 高校就业职业分析群体 1-否，2-是                    |
| `strEmploymentYGAAreaCode` | varchar(10) | 粤港澳大湾区就业区域编码                          |
| `strEmploymentWestAreaCode` | varchar(10) | 西部地区就业区域编码                            |
| `strEmploymentYDYLAreaCode` | varchar(10) | 一带一路就业区域编码                            |
| `strEmploymentJJJAreaCode` | varchar(10) | 京津冀地区就业区域编码                           |
| `strEmploymentCJEAreaCode` | varchar(10) | 长江经济带就业区域编码                           |
| `strEmploymentHHAreaCode` | varchar(10) | 黄河流域就业区域编码                            |
| `strEmploymentCYEAreaCode` | varchar(10) | 成渝经济圈就业就业区域编码                         |
| `nEmploymentInsideOrOutside2` | int(4) | 省内外就业2 1-省内就业，2-回生源所在省就业，3-其他区域就业     |
| `nOriginEmploymentCross` | int(4) | 省内生源就业交叉 1-省外就业，2-回生源所在地就业，3-省内其他地方就业 |
| `strCompanyNature` | varchar(100) | 单位性质                                  |
| `strCompanyNatureCategory` | varchar(100) | 单位性质归类-高校                             |
| `strSystemCompanyNatureCategory` | varchar(100) | 单位性质归类-系统                             |
| `strCompanyNatureCategory1` | varchar(100) | 单位性质归类-高校临时                           |
| `strCompanyIndustry` | varchar(100) | 单位所属行业                                |
| `strCompanyIndustryCategory` | varchar(100) | 单位所属行业归类-高校                           |
| `strSystemCompanyIndustryCategory` | varchar(100) | 单位所属行业归类-系统                           |
| `strCompanyIndustryCategory1` | varchar(100) | 单位所属行业归类-高校临时                         |
| `strEmploymentProfession` | varchar(100) | 就业职业                                  |
| `strEmploymentProfessionCategory` | varchar(100) | 就业职业归类-高校                             |
| `strSystemEmploymentProfessionCategory` | varchar(100) | 就业职业归类-系统                             |
| `strEmploymentProfessionCategory1` | varchar(100) | 就业职业归类-高校临时                           |
| `strSchoolLevel` | varchar(50) | 升学院校层次                                |
| `QSRanks` | int(4) | QS排名                                  |
| `strExtend1` - `strExtend50` | varchar(100) | 扩展字段                                  |
| `dtCreateTime` | timestamp | 创建时间                                  |
| `dtUpdateTime` | timestamp | 修改时间                                  |

#### 数据字典表：`tbappcodeitem`来源于dataanalyze
该表存储枚举值及其对应的名称。

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `strCode` | varchar(70) | 字典类型编码 (主键) |
| `strItemCode` | varchar(70) | 字典项编码 (主键) |
| `strItemName` | varchar(280) | 字典项名称 |
| `strRemark` | varchar(700) | 备注 |
| `strParentCode` | varchar(70) | 父级字典类型编码 (主键) |
| `strParentItemCode` | varchar(70) | 父级字典项编码 (主键) |
| `nOrder` | int(11) | 排序 |

#### 字段与字典映射规则
在查询时，需要将基础数据表中的编码字段关联到数据字典表以获取名称。
映射关系如下：

| 基础数据表字段 | 对应字典表 `strCode` | 字典项编码字段 | 字典项名称字段 |
| :--- | :--- | :--- | :--- |
| `strOriginProvinceCode` | `Location1Code` | `strItemCode` | `strItemName` |
| `strOriginCityCode` | `Location2Code` | `strItemCode` | `strItemName` |
| `strOrigin3MGArea1Code` | `Origin3MGArea1Code` | `strItemCode` | `strItemName` |
| `strOrigin4MEArea1Code` | `Origin4MEArea1Code` | `strItemCode` | `strItemName` |
| `strOrigin7MGArea1Code` | `Origin7MGArea1Code` | `strItemCode` | `strItemName` |
| `strOrigin8MEArea1Code` | `Origin8MEArea1Code` | `strItemCode` | `strItemName` |
| `strOriginGuangDongArea1Code` | `OriginGuangDongArea1Code` | `strItemCode` | `strItemName` |
| `strEmploymentProvinceCode` | `Location1Code` | `strItemCode` | `strItemName` |
| `strEmploymentCityCode` | `Location2Code` | `strItemCode` | `strItemName` |
| `strEmployment3MGArea1Code` | `Employment3MGArea1Code` | `strItemCode` | `strItemName` |
| `strEmployment4MEArea1Code` | `Employment4MEArea1Code` | `strItemCode` | `strItemName` |
| `strEmployment7MGArea1Code` | `Employment7MGArea1Code` | `strItemCode` | `strItemName` |
| `strEmployment8MEArea1Code` | `Employment8MEArea1Code` | `strItemCode` | `strItemName` |
| `strEmployment8MEArea1Code2` | `Employment8MEArea1Code2` | `strItemCode` | `strItemName` |
| `strEmploymentCityTypeCode` | `EmploymentCityTypeCode` | `strItemCode` | `strItemName` |
| `strEmploymentGuangDongArea1Code` | `EmploymentGuangDongArea1Code` | `strItemCode` | `strItemName` |
| `strEmploymentYGAAreaCode` | `EmploymentYGAAreaCode` | `strItemCode` | `strItemName` |
| `strEmploymentWestAreaCode` | `EmploymentWestAreaCode` | `strItemCode` | `strItemName` |
| `strEmploymentYDYLAreaCode` | `EmploymentYDYLAreaCode` | `strItemCode` | `strItemName` |
| `strEmploymentJJJAreaCode` | `EmploymentJJJAreaCode` | `strItemCode` | `strItemName` |
| `strEmploymentCJEAreaCode` | `EmploymentCJEAreaCode` | `strItemCode` | `strItemName` |
| `strEmploymentHHAreaCode` | `EmploymentHHAreaCode` | `strItemCode` | `strItemName` |
| `strEmploymentCYEAreaCode` | `EmploymentCYEAreaCode` | `strItemCode` | `strItemName` |

#### 数据机构组织表：`tborg` 来源于dataanalyze
该表存机构组织对应的名称。

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `lId` | bigint(20) | 机构id |
| `strOrgName` | varchar(100) | 机构名称 |

**关联查询示例：**
```sql
SELECT 
    d.lOrgId,
    org.strOrgName,
    d.strOriginProvinceCode,
    p.strItemName AS origin_province_name
FROM 
    `tbobjectivedata` d
LEFT JOIN 
    `dataanalyze`.`tbappcodeitem` p ON d.strOriginProvinceCode = p.strItemCode AND p.strCode = 'Location1Code'
LEFT JOIN 
    `dataanalyze`.`tborg` org ON d.lOrgId = org.lId

1. COUNT / 数量统计
----------------------

- 统计总数：
  SELECT COUNT(*) AS total FROM <table>

- 统计指定字段非空数量：
  SELECT COUNT(<column>) AS cnt FROM <table>

- 统计某列的去重数量：
  SELECT COUNT(DISTINCT <column>) AS distinct_cnt FROM <table>

- 同时统计总数和去重数：
  SELECT COUNT(*) AS total, COUNT(DISTINCT <column>) AS distinct_cnt FROM <table>

- 按条件统计数量：
  SELECT COUNT(*) AS cnt FROM <table> WHERE <condition>

- 多条件统计（符合特定条件的数量）：
  SELECT COUNT(CASE WHEN <condition> THEN 1 END) AS matched_cnt FROM <table>

2. SUM / 求和统计
--------------------

- 对某列求和：
  SELECT SUM(<numeric_column>) AS total_sum FROM <table>

- 按条件求和：
  SELECT SUM(CASE WHEN <condition> THEN <numeric_column> ELSE 0 END) AS conditional_sum FROM <table>

- 求平均值：
  SELECT AVG(<numeric_column>) AS avg_value FROM <table>

- 最大值/最小值：
  SELECT MAX(<numeric_column>) AS max_value, MIN(<numeric_column>) AS min_value FROM <table>

3. 占比/比例计算
------------------

- 某条件的占比（百分比，保留2位小数）：
  SELECT ROUND(COUNT(CASE WHEN <condition> THEN 1 END) * 100.0 / COUNT(*), 2) AS ratio FROM <table>

- 某类别占总数的百分比（带总数）：
  SELECT
    COUNT(*) AS total,
    SUM(CASE WHEN <condition> THEN 1 ELSE 0 END) AS matched,
    ROUND(SUM(CASE WHEN <condition> THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS ratio
  FROM <table>

- 分类聚合占比：
  SELECT
    <group_column>,
    COUNT(*) AS cnt,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM <table>), 2) AS ratio
  FROM <table>
  GROUP BY <group_column>
  ORDER BY cnt DESC

- 分类看某条件下的占比：
  SELECT
    <group_column>,
    COUNT(*) AS total,
    SUM(CASE WHEN <condition> THEN 1 ELSE 0 END) AS matched,
    ROUND(SUM(CASE WHEN <condition> THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS ratio
  FROM <table>
  GROUP BY <group_column>
  ORDER BY ratio DESC

4. GROUP BY / 分组聚合
-----------------------

- 分组统计数量：
  SELECT <group_column>, COUNT(*) AS cnt
  FROM <table>
  GROUP BY <group_column>
  ORDER BY cnt DESC

- 分组求和：
  SELECT <group_column>, SUM(<numeric_column>) AS total_sum
  FROM <table>
  GROUP BY <group_column>
  ORDER BY total_sum DESC

- 分组统计多个指标：
  SELECT
    <group_column>,
    COUNT(*) AS cnt,
    SUM(<numeric_column>) AS total_sum,
    AVG(<numeric_column>) AS avg_value
  FROM <table>
  GROUP BY <group_column>
  ORDER BY cnt DESC

- 分组看最大值/最小值：
  SELECT <group_column>, MAX(<numeric_column>) AS max_val, MIN(<numeric_column>) AS min_val
  FROM <table>
  GROUP BY <group_column>

- HAVING 过滤分组后结果：
  SELECT <group_column>, COUNT(*) AS cnt
  FROM <table>
  GROUP BY <group_column>
  HAVING cnt > <threshold>
  ORDER BY cnt DESC

5. 日期/时间相关
------------------

- 按日期分组统计：
  SELECT DATE(<datetime_column>) AS dt, COUNT(*) AS cnt
  FROM <table>
  GROUP BY dt
  ORDER BY dt

- 按月统计：
  SELECT DATE_FORMAT(<datetime_column>, '%Y-%m') AS month, COUNT(*) AS cnt
  FROM <table>
  GROUP BY month
  ORDER BY month

- 按年统计：
  SELECT DATE_FORMAT(<datetime_column>, '%Y') AS year, COUNT(*) AS cnt
  FROM <table>
  GROUP BY year
  ORDER BY year

- 时间范围内的数据：
  SELECT * FROM <table>
  WHERE <datetime_column> BETWEEN '<start_date>' AND '<end_date>'

- 最近N天数据：
  SELECT * FROM <table>
  WHERE <datetime_column> >= DATE_SUB(CURDATE(), INTERVAL <N> DAY)

- 按日期和时间段联合统计：
  SELECT DATE(<datetime_column>) AS dt, <group_column>, COUNT(*) AS cnt
  FROM <table>
  GROUP BY dt, <group_column>
  ORDER BY dt, cnt DESC

6. ORDER BY / 排序
-------------------

- 降序排列取TOP N：
  SELECT <columns>
  FROM <table>
  ORDER BY <numeric_column> DESC
  LIMIT <N>

- 多字段排序：
  SELECT <columns>
  FROM <table>
  ORDER BY <col1> DESC, <col2> ASC

- 分组后取每组前N（MySQL 5.7不使用窗口函数，改用变量实现）：
  SELECT <group_column>, <numeric_column>, <other_columns>
  FROM (
    SELECT
      <group_column>, <numeric_column>, <other_columns>,
      @rn := IF(@prev_group = <group_column>, @rn + 1, 1) AS rn,
      @prev_group := <group_column>
    FROM <table>, (SELECT @rn := 0, @prev_group := NULL) vars
    ORDER BY <group_column>, <numeric_column> DESC
  ) t
  WHERE rn <= <N>

7. 条件过滤 / WHERE
---------------------

- 精确匹配：
  SELECT * FROM <table> WHERE <column> = '<value>'

- 模糊匹配（LIKE）：
  SELECT * FROM <table> WHERE <column> LIKE '%<keyword>%'

- 多条件组合（AND）：
  SELECT * FROM <table>
  WHERE <condition1> AND <condition2>

- 多条件组合（OR）：
  SELECT * FROM <table>
  WHERE <condition1> OR <condition2>

- IN 查询：
  SELECT * FROM <table>
  WHERE <column> IN (<value1>, <value2>, <value3>)

- 非空判断：
  SELECT * FROM <table>
  WHERE <column> IS NOT NULL

- 范围查询：
  SELECT * FROM <table>
  WHERE <numeric_column> BETWEEN <min> AND <max>

- 排除空值查询：
  SELECT * FROM <table>
  WHERE <column> IS NOT NULL AND <column> != ''

8. 多表关联 / JOIN
--------------------

- 内连接：
  SELECT a.<col1>, b.<col2>
  FROM <table_a> a
  INNER JOIN <table_b> b ON a.<key> = b.<key>

- 左连接：
  SELECT a.<col1>, b.<col2>
  FROM <table_a> a
  LEFT JOIN <table_b> b ON a.<key> = b.<key>

- 多表关联统计：
  SELECT a.<group_col>, COUNT(*) AS cnt
  FROM <table_a> a
  INNER JOIN <table_b> b ON a.<key> = b.<key>
  GROUP BY a.<group_col>
  ORDER BY cnt DESC

- 关联带条件：
  SELECT a.<col1>, b.<col2>
  FROM <table_a> a
  LEFT JOIN <table_b> b ON a.<key> = b.<key>
  WHERE a.<condition>

9. 子查询 / 嵌套查询
----------------------

- WHERE 子查询：
  SELECT * FROM <table>
  WHERE <column> = (SELECT <column> FROM <another_table> WHERE <condition> LIMIT 1)

- FROM 子查询（子查询作为临时表）：
  SELECT t.<col>, COUNT(*) AS cnt
  FROM (
    SELECT * FROM <table> WHERE <condition>
  ) t
  GROUP BY t.<col>

- EXISTS：
  SELECT * FROM <table_a> a
  WHERE EXISTS (SELECT 1 FROM <table_b> b WHERE b.<key> = a.<key>)

10. Top N / 排行
-----------------

- 取前N条：
  SELECT <columns> FROM <table> ORDER BY <numeric_column> DESC LIMIT <N>

- 取后N条（最小N个）：
  SELECT <columns> FROM <table> ORDER BY <numeric_column> ASC LIMIT <N>

- 分组取每组前N：
  参见第6节"分组后取每组前N"（基于MySQL 5.7变量实现）

- 排名（MySQL 5.7不支持窗口函数 RANK/DENSE_RANK，使用变量实现排名）：
  SELECT
    <columns>,
    @rk := @rk + 1 AS rk,
    @dr := IF(@prev_val = <numeric_column>, @dr, @rk) AS dense_rk,
    @prev_val := <numeric_column>
  FROM <table>, (SELECT @rk := 0, @dr := 0, @prev_val := NULL) vars
  ORDER BY <numeric_column> DESC
  说明：变量 @rk 实现类似 RANK（行号递增），@dr 实现类似 DENSE_RANK（值相等时排名不变）
  注意：MySQL 5.7 不支持 RANK() / DENSE_RANK() / ROW_NUMBER() 等窗口函数

11. 去重 / DISTINCT
--------------------

- 单列去重：
  SELECT DISTINCT <column> FROM <table>

- 多列组合去重：
  SELECT DISTINCT <col1>, <col2> FROM <table>

- 去重后统计：
  SELECT COUNT(DISTINCT <column>) AS distinct_cnt FROM <table>

- 去重后排序：
  SELECT DISTINCT <column> FROM <table> ORDER BY <column>

12. 字符串处理
----------------

- 字符串拼接：
  SELECT CONCAT(<col1>, ' - ', <col2>) AS combined_name FROM <table>

- 子串提取：
  SELECT SUBSTRING(<column> FROM 1 FOR <N>) AS prefix FROM <table>

- 字符串长度：
  SELECT <column>, LENGTH(<column>) AS len
  FROM <table>
  WHERE LENGTH(<column>) > <threshold>

- 大小写转换：
  SELECT UPPER(<column>) AS upper_val, LOWER(<column>) AS lower_val FROM <table>

- 替换字符串：
  SELECT REPLACE(<column>, '<old>', '<new>') AS replaced FROM <table>

13. NULL 值处理
----------------

- 将 NULL 替换为默认值：
  SELECT COALESCE(<column>, '<default_value>') AS col_alias FROM <table>

- NULL 值统计：
  SELECT
    COUNT(*) AS total,
    SUM(CASE WHEN <column> IS NULL THEN 1 ELSE 0 END) AS null_cnt,
    SUM(CASE WHEN <column> IS NOT NULL THEN 1 ELSE 0 END) AS not_null_cnt
  FROM <table>

- IFNULL 用法：
  SELECT <column>, IFNULL(<column>, '<default>') AS handled FROM <table>

14. 高级函数
--------------

- IF 条件表达式：
  SELECT <column>, IF(<condition>, '<true_val>', '<false_val>') AS label FROM <table>

- CASE WHEN 多条件分支：
  SELECT
    <column>,
    CASE
      WHEN <condition1> THEN '<label1>'
      WHEN <condition2> THEN '<label2>'
      ELSE '<other>'
    END AS category
  FROM <table>

- 多种统计在一行（CASE 聚合）：
  SELECT
    COUNT(*) AS total,
    SUM(CASE WHEN <condition1> THEN 1 ELSE 0 END) AS cat1,
    SUM(CASE WHEN <condition2> THEN 1 ELSE 0 END) AS cat2,
    SUM(CASE WHEN <condition3> THEN 1 ELSE 0 END) AS cat3
  FROM <table>

- 分组后看每组各情况汇总：
  SELECT
    <group_column>,
    COUNT(*) AS total,
    SUM(CASE WHEN <condition> THEN 1 ELSE 0 END) AS matched,
    ROUND(AVG(<numeric_column>), 2) AS avg_val
  FROM <table>
  GROUP BY <group_column>
  ORDER BY total DESC

15. 分页查询
---------------

- LIMIT 分页（page从1开始）：
  SELECT <columns>
  FROM <table>
  ORDER BY <order_column> DESC
  LIMIT <page_size> OFFSET <(page - 1) * page_size>

- 查询+总条数（一起返回）：
  SELECT SQL_CALC_FOUND_ROWS <columns>
  FROM <table>
  ORDER BY <order_column> DESC
  LIMIT <page_size> OFFSET <offset>;
  SELECT FOUND_ROWS() AS total;

16. 常用别名约定
------------------

- 数量统计用 total、cnt、count
- 去重数量用 distinct_cnt
- 求和用 total_sum、sum
- 平均值用 avg_value、avg
- 最大值/最小值用 max_value/min_value 或 max/min
- 占比用 ratio、rate、percentage
- 排名用 rk、rank、row_num
- 结果用 result、data
- 分组字段保持原字段名或加 group 后缀

17. SQL编写规范
-----------------

- 所有 SQL 关键字统一大写（SELECT, FROM, WHERE, GROUP BY, ORDER BY, LIMIT 等）
- 表名和字段名使用反引号 `` ` `` 包裹，避免与关键字冲突
- 字符串值使用单引号 `'`
- 生成的 SQL 只返回查询语句，不要包含 USE 或 SET 等管理命令
- 单条 SQL 即可完成时，不要使用 UNION 或复杂的多层嵌套
- 保持 SQL 可读性，合理缩进
- 如果用户问题与表结构不相关或不明确，返回空字符串
- 生成的 SQL 必须在 **MySQL 5.7.28** 语法下合法可执行
- **禁止使用** MySQL 8.0+ 的语法特性，包括但不限于：窗口函数（`OVER()`、`ROW_NUMBER()`、`RANK()`、`DENSE_RANK()`、`LAG()`、`LEAD()` 等）、`WITH`（CTE 公用表表达式）、`LATERAL`、`JSON_TABLE` 等

18. 注意事项遇到用户问题中含有这些下面表格where字段列 或者group by 字段列，一定要在要生成的sql语句中添加这些过滤条件或者聚合。
| where 字段列 | group by 字段列 | 对应数据库表列名 | 对应数据库表列注释 |
|--------------|----------------|------------------|---------------------|
| 学历 | 学历 | strDegree | 学历编码 |
| 性别 | 性别 | strGender | 性别 |
| 学院 | 学院 | strDepartment | 学院 |
| 专业 | 专业 | strSpeciality | 专业 |
| 学生（专业）属性 | 学生（专业）属性 | strSpecialtyAttribute | 学生（专业）属性 |
| 省内外生源 | 省内外生源 | nOriginInsideOrOutside | 省内外生源 1-省内生源，2-省外生源 |
| 困难生 | 困难生 | strDifficult | 困难生类别 |
| 生源省 | 生源省 | strOriginProvinceCode | 生源省编码 |

19. 毕业去向落实率 注意事项
-- 分子：该分组内已就业的人数
-- 分子：该分组内已就业的人数
(SELECT COUNT(*) 
 FROM `表名` {tableName} 
 WHERE t.`lDataSetId` = {lDataSetId}          -- ✅ 必须加数据集过滤
   AND t.`分组字段` = d.`分组字段`          -- ✅ 关联分组字段
   AND t.`strSystemGraduationCategory` IS NOT NULL 
   AND t.`strSystemGraduationCategory` != '' 
   AND t.`strSystemGraduationCategory` != '未就业'
)

-- 分母：该分组内的总人数
(SELECT COUNT(*) 
 FROM `表名` 
 WHERE `lDataSetId` = `数据集ID`            -- ✅ 必须加数据集过滤
   AND `分组字段` = d.`分组字段`            -- ✅ 关联分组字段
)