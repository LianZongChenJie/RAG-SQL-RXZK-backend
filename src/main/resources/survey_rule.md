SQL 生成规则
================

> **目标数据库：MySQL 5.7.28**
> 生成的所有 SQL 必须兼容 MySQL 5.7.28 语法。
> **注意：MySQL 5.7 不支持窗口函数（如 `OVER()`、`ROW_NUMBER()`、`RANK()`、`DENSE_RANK()`），请勿在 SQL 中使用。**

本文档定义了 AI 智能体在根据用户问题和表结构生成 SQL 时必须遵守的规则。
请严格按照以下规则生成对应的 SQL 查询语句。
清洗的字段列中的字段不统计在 SQL 查询中。
满意度计算公式：选项1~5的权重分别是（5,4,3,2,1），根据权重计算所有人作答的和除作答人数。
统计总人数和比例时，统计所有符合条件的清洗数据和没有清洗的数据

---

### 0. 数据表结构与字典映射

#### 调研数据表：`tbanswerrecordsnapshotdetail94part0`、`tbanswerrecordsnapshotdetail94part1`、`tbanswerrecordsnapshotdetail94part2`、`tbanswerrecordsnapshotdetail94part3...`
所有客观表的表结构完全一致,该表存储学生就业的客观数据。

| 字段名                | 类型 | 描述                                                                |
|:-------------------| :--- |:------------------------------------------------------------------|
| `lId` | bigint(20) | 答题纸id                                                             |
| `lOrgId` | bigint(20) | 机构id                                                              |
| `lResearchId` | bigint(20) | 调研id                                                              |
| `lQuestionnaireId` | bigint(20) | 问卷id                                                              |
| `lSnapshotId` | bigint(20) | 快照id                                                              |
| `strIdentity` | varchar(100) | 身份验证字段                                                            |
| `strAgent` | varchar(600) | 答题设备                                                              |
| `strIp` | varchar(100) | 答题ip                                                              |
| `lTaskId` | bigint(20) | 答题批次                                                              |
| `dtStartTime` | timestamp | 开始作答时间                                                            |
| `dtEndTime` | timestamp | 结束作答时间                                                            |
| `nAnswerState` | int(4) | 作答情况 0-默认状态 未作答,1-未完整作答,2-完整作答                                    |
| `bRequiredAllAnswered` | tinyint(1) | 必答题是否全部作答:0-否;1-是                                                 |
| `bWashed` | tinyint(1) | 具体清洗列以strWashColumn为准                                             |
| `strWashColumn` | text | 清洗的字段列:数据记录方式 1,2,3... |
| `strWashOption` | text | 清洗的选项:数据记录方式 问题id_选项id,...                                        |
| `strInitialSalary` | text | 当前薪酬初始清洗列                                                         |
| `strSalary` | text | 当前薪酬清洗列                                                           |
| `nInitialSalaryWashState` | tinyint(1) | 当前薪酬初始清洗列状态                                                       |
| `nSalaryWashState` | tinyint(1) | 当前薪酬清洗状态:0-未清洗(原始数据);1-已清洗(均值替代)                                  |
| `strInitialSalary1` | text | 初始薪酬初始清洗列                                                         |
| `strSalary1` | text | 初始薪酬清洗列                                                           |
| `nInitialSalaryWashState1` | tinyint(1) | 初始薪酬初始清洗列状态                                                       |
| `nSalaryWashState1` | tinyint(1) | 初始薪酬清洗状态:0-未清洗(原始数据);1-已清洗(均值替代)                                  |
| `strFindJob` | text | 简历投递反馈情况清洗列                                                       |
| `dtCreateTime` | timestamp | 创建时间                                                              |
| `dtUpdateTime` | timestamp | 修改时间                                                              |
| `strAnswer0` | text | 答案0                                                               |
| `strAnswer1` | text | 答案1                                                               |
| `strAnswer2` | text | 答案2                                                               |
| `strAnswer3` | text | 答案3                                                               |
| `strAnswer4` | text | 答案4                                                               |
| `strAnswer5` | text | 答案5                                                               |
| `strAnswer6` | text | 答案6                                                               |
| `strAnswer7` | text | 答案7                                                               |
| `strAnswer8` | text | 答案8                                                               |
| `strAnswer9` | text | 答案9                                                               |
| `strAnswer10` | text | 答案10                                                              |
| `strAnswer11` | text | 答案11                                                              |
| `strAnswer12` | text | 答案12                                                              |
| `strAnswer13` | text | 答案13                                                              |
| `strAnswer14` | text | 答案14                                                              |
| `strAnswer15` | text | 答案15                                                              |
| `strAnswer16` | text | 答案16                                                              |
| `strAnswer17` | text | 答案17                                                              |
| `strAnswer18` | text | 答案18                                                              |
| `strAnswer19` | text | 答案19                                                              |
| `strAnswer20` | text | 答案20                                                              |
| `strAnswer21` | text | 答案21                                                              |
| `strAnswer22` | text | 答案22                                                              |
| `strAnswer23` | text | 答案23                                                              |
| `strAnswer24` | text | 答案24                                                              |
| `strAnswer25` | text | 答案25                                                              |
| `strAnswer26` | text | 答案26                                                              |
| `strAnswer27` | text | 答案27                                                              |
| `strAnswer28` | text | 答案28                                                              |
| `strAnswer29` | text | 答案29                                                              |
| `strAnswer30` | text | 答案30                                                              |
| `strAnswer31` | text | 答案31                                                              |
| `strAnswer32` | text | 答案32                                                              |
| `strAnswer33` | text | 答案33                                                              |
| `strAnswer34` | text | 答案34                                                              |
| `strAnswer35` | text | 答案35                                                              |
| `strAnswer36` | text | 答案36                                                              |
| `strAnswer37` | text | 答案37                                                              |
| `strAnswer38` | text | 答案38                                                              |
| `strAnswer39` | text | 答案39                                                              |
| `strAnswer40` | text | 答案40                                                              |
| `strAnswer41` | text | 答案41                                                              |
| `strAnswer42` | text | 答案42                                                              |
| `strAnswer43` | text | 答案43                                                              |
| `strAnswer44` | text | 答案44                                                              |
| `strAnswer45` | text | 答案45                                                              |
| `strAnswer46` | text | 答案46                                                              |
| `strAnswer47` | text | 答案47                                                              |
| `strAnswer48` | text | 答案48                                                              |
| `strAnswer49` | text | 答案49                                                              |
| `strAnswer50` | text | 答案50                                                              |
| `strAnswer51` | text | 答案51                                                              |
| `strAnswer52` | text | 答案52                                                              |
| `strAnswer53` | text | 答案53                                                              |
| `strAnswer54` | text | 答案54                                                              |
| `strAnswer55` | text | 答案55                                                              |
| `strAnswer56` | text | 答案56                                                              |
| `strAnswer57` | text | 答案57                                                              |
| `strAnswer58` | text | 答案58                                                              |
| `strAnswer59` | text | 答案59                                                              |
| `strAnswer60` | text | 答案60                                                              |
| `strAnswer61` | text | 答案61                                                              |
| `strAnswer62` | text | 答案62                                                              |
| `strAnswer63` | text | 答案63                                                              |
| `strAnswer64` | text | 答案64                                                              |
| `strAnswer65` | text | 答案65                                                              |
| `strAnswer66` | text | 答案66                                                              |
| `strAnswer67` | text | 答案67                                                              |
| `strAnswer68` | text | 答案68                                                              |
| `strAnswer69` | text | 答案69                                                              |
| `strAnswer70` | text | 答案70                                                              |
| `strAnswer71` | text | 答案71                                                              |
| `strAnswer72` | text | 答案72                                                              |
| `strAnswer73` | text | 答案73                                                              |
| `strAnswer74` | text | 答案74                                                              |
| `strAnswer75` | text | 答案75                                                              |
| `strAnswer76` | text | 答案76                                                              |
| `strAnswer77` | text | 答案77                                                              |
| `strAnswer78` | text | 答案78                                                              |
| `strAnswer79` | text | 答案79                                                              |
| `strAnswer80` | text | 答案80                                                              |
| `strAnswer81` | text | 答案81                                                              |
| `strAnswer82` | text | 答案82                                                              |
| `strAnswer83` | text | 答案83                                                              |
| `strAnswer84` | text | 答案84                                                              |
| `strAnswer85` | text | 答案85                                                              |
| `strAnswer86` | text | 答案86                                                              |
| `strAnswer87` | text | 答案87                                                              |
| `strAnswer88` | text | 答案88                                                              |
| `strAnswer89` | text | 答案89                                                              |
| `strAnswer90` | text | 答案90                                                              |
| `strAnswer91` | text | 答案91                                                              |
| `strAnswer92` | text | 答案92                                                              |
| `strAnswer93` | text | 答案93                                                              |
| `strAnswer94` | text | 答案94                                                              |
| `strAnswer95` | text | 答案95                                                              |
| `strAnswer96` | text | 答案96                                                              |
| `strAnswer97` | text | 答案97                                                              |
| `strAnswer98` | text | 答案98                                                              |
| `strAnswer99` | text | 答案99                                                              |

#### 用户表：`tbstudent4`
该表存储用户信息，查询时需要将调研表中的 `strIdentity` 关联到用户表以获取用户名称

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `lId` | bigint(20) | 主键id |
| `lOrgId` | bigint(20) | 所属机构 |
| `lRespondentId` | bigint(20) | 调研对象id |
| `strIdentity` | varchar(100) | 用户身份验证 |
| `strStudentCode` | varchar(50) | 学生学号 |
| `strStudentName` | varchar(100) | 学生姓名 |
| `strSchoolName` | varchar(100) | 学校名称 |
| `strGraduationYear` | varchar(10) | 毕业年份 |
| `strSex` | varchar(10) | 性别 男/女 |
| `strBirthday` | varchar(20) | 出生年月 |
| `strDegreeCode` | varchar(10) | 学历编码 |
| `strDegreeName` | varchar(10) | 学历名称 |
| `strAcademyCode` | varchar(100) | 学院编码 |
| `strAcademyName` | varchar(100) | 学院名称 |
| `strSpecialityCode` | varchar(100) | 专业编码 |
| `strSpecialityName` | varchar(100) | 专业名称 |
| `strClassName` | varchar(100) | 班级名称 |
| `strRaceCode` | varchar(10) | 民族编码 |
| `strRaceName` | varchar(30) | 民族名称 |
| `strPoliticalCode` | varchar(20) | 政治面貌编码 |
| `strPoliticalName` | varchar(50) | 政治面貌 |
| `strProvinceCode` | varchar(20) | 生源地省份/直辖市编码 |
| `strProvinceName` | varchar(20) | 生源地省份/直辖市名称 |
| `strCityCode` | varchar(20) | 生源地市/区编码 |
| `strCityName` | varchar(50) | 生源地市/区名称 |
| `strEmail` | varchar(50) | 电子邮箱 |
| `strMobile` | varchar(30) | 手机号 |
| `strGraduationDestinationCode` | varchar(20) | 毕业去向编码 |
| `strGraduationDestinationName` | varchar(50) | 毕业去向名称 |
| `strDestinationProvinceCode` | varchar(50) | 毕业去向省份/直辖市编码 |
| `strDestinationProvinceName` | varchar(50) | 毕业去向省份/直辖市名称 |
| `strDestinationCityCode` | varchar(20) | 毕业去向市/区编码 |
| `strDestinationCityName` | varchar(50) | 毕业去向地市/区名称 |
| `lCompanyId` | bigint(20) | 单位主键id |
| `strComName` | varchar(100) | 单位名称 |
| `strNormal` | varchar(50) | 师范生情况 |
| `strDifficult` | varchar(50) | 困难生情况 |
| `strLevel1Discipline` | varchar(50) | 一级学科 |
| `strLevel2Discipline` | varchar(50) | 二级学科 |
| `dtCreateTime` | timestamp | 创建时间 |
| `dtUpdateTime` | timestamp | 修改时间 |

#### 关联的机构组织表
在查询时，需要将调研数据表中的结构字段关联机构表以获取名称。
映射关系如下：

| 字段名                | 类型 | 描述 |
|:-------------------| :--- | :--- |
| `lId`              | bigint(20)    | 机构id                          |
| `strOrgName`       | varchar(100)  | 机构名称                          |
| `lParentId`        | bigint(20)    | 父机构id                         |
| `strOrgType`       | varchar(20)   | 机构类型：BOSS、SCHOOL、CONSULTATION |
| `strDomain`        | varchar(100)  | 学校访问二级域名                      |
| `strPlatformName`  | varchar(100)  | 机构平台名称                        |
| `dtValidity`       | timestamp     | 有效期                           |
| `bLocked`          | tinyint(1)    | 机构是否被锁定:0-否;1-是               |
| `strContact`       | varchar(20)   | 联系人                           |
| `strContactNumber` | varchar(20)   | 联系电话                          |
| `strLogo`          | varchar(1000) | 机构logo图片                      |
| `strProvinceCode`   | varchar(10)   | 学校所在省编码                       |
| `strCityCode`        | varchar(10)   | 学校所在市编码                       |
| `strPermission`      | longtext      | 权限                            |
| `dtCreateTime`       | timestamp     | 创建时间                          |
| `dtUpdateTime`       | timestamp     | 修改时间                          |

#### 关联学生的属性表
在查询时，需要将调研数据表中的结构字段关联学生表以获取名称。
映射关系如下：

| 字段名                | 类型 | 描述 |
|:-------------------| :--- | :--- |
| `lId`              | bigint(20)    | 机构id                          |
| `lOrgId`              | bigint(20)    | 所属机构                         |
| `lRespondentId`              | bigint(20)    | 调研对象id                         |
| `strIdentity`              | varchar(100)    | 用户身份验证                        |
| `strStudentCode`              | varchar(50)   | 学生学号                         |
| `strStudentName`              | varchar(100)    | 学生姓名                          |
| `strSchoolName`              | varchar(100)    | 学校名称                        |
| `strGraduationYear`              | varchar(20)    | 毕业年份/学年                         |
| `strSex`              | varchar(10)    | 性别 男/女                         |
| `strBirthday`              | varchar(20)    | 出生年月                         |
| `strDegreeCode`              | varchar(10)    | 学历编码                          |
| `strDegreeName`              | varchar(10)    | 学历名称                         |
| `strAcademyCode`              | varchar(100)    | 学院编码                          |
| `strAcademyName`              | varchar(100)   | 学院名称                        |
| `strSpecialityCode`              | varchar(100)    | 专业编码                          |
| `strSpecialityName`              | varchar(100)   | 专业名称                         |
| `strClassName`              | varchar(100)    | 班级名称                         |
| `strRaceCode`              | varchar(10)   | 民族编码                         |
| `strRaceName`              | varchar(30)    | 民族名称                         |
| `strPoliticalCode`              | varchar(20)    | 政治面貌编码                          |
| `strPoliticalName`              | varchar(50)    | 政治面貌                          |
| `strProvinceCode`              | varchar(20)    | 生源地省份/直辖市编码                          |
| `strProvinceName`              | varchar(20)    | 生源地省份/直辖市名称                          |
| `strCityCode`              | varchar(20)   | 生源地市/区编码                         |
| `strCityName`              | varchar(50)    | 生源地市/区名称                          |
| `strEmail`              | varchar(50)    | 电子邮箱                          |
| `strMobile`              | varchar(30)   | 手机号                        |
| `strGraduationDestinationCode`              | varchar(20)   | 毕业去向编码                          |
| `strGraduationDestinationName`              | varchar(50)   | 毕业去向名称                          |
| `strDestinationProvinceCode`              | varchar(50)    | 毕业去向省份/直辖市编码                         |
| `strDestinationProvinceName`              | varchar(50)    | 毕业去向省份/直辖市名称                         |
| `strDestinationCityCode`              | varchar(20)   | 毕业去向市/区编码                          |
| `strDestinationCityName`              | varchar(50)    | 毕业去向地市/区名称                         |
| `lCompanyId`              | bigint(20)   | 单位主键id                          |
| `strComName`              | varchar(100)   | 单位名称                       |
| `strNormal`              | varchar(50)   | 师范生情况                        |
| `strDifficult`              | varchar(50)    | 困难生情况                         |
| `strLevel1Discipline`              | varchar(50)    | 一级学科                         |
| `strLevel2Discipline`              | varchar(50)    | 二级学科                        |
| `dtCreateTime`              | timestamp    | 创建时间                          |
| `dtUpdateTime`              | timestamp   | 修改时间                          |
		
### 0.1 数据清洗统计核心原则（⚠️ 生成SQL前必读）

当查询涉及 `strWashColumn` 清洗字段时，必须遵守以下原则：

| 统计项 | 是否排除清洗数据 | 说明 |
|:-------|:----------------|:-----|
| **分子（各选项作答人数/计数）** | ✅ 排除 | 只统计未被清洗的有效数据 |
| **分母（总人数/答题总人数）** | ✅ 排除 | 只统计未被清洗的有效数据（与分子口径一致） |
| **占比 = 分子 / 分母** | 分子排除，分母排除 | 反映各选项在清洗后有效数据中的分布比例，各选项占比合计 = 100% |

**核心规则：统计总人数和比例时，分子和分母统一使用清洗后的数据（排除 `strWashColumn` 中包含对应题目序号的数据）**

#### 一、作答状态统计口径

`nAnswerState` 字段含义：
- `0`：默认状态，未作答
- `1`：未完整作答（已开始答题但未完成）
- `2`：完整作答

**统计规则：**
- **统计"答题人数/总人数"时**：统计所有有作答行为的记录，即 `nAnswerState IN (1, 2)`
- **统计"完整作答人数"时**：仅统计 `nAnswerState = 2`
- **占比计算的分母**：使用 `nAnswerState IN (1, 2)`，即所有有作答行为的人数

**⚠️ 默认行为：除非用户明确指定"完整作答"，否则均使用 `nAnswerState IN (1, 2)`**

#### 二、数据清洗统计口径

当查询涉及 `strWashColumn` 清洗字段时，必须遵守以下原则：

| 统计项 | 是否排除清洗数据 | 说明 |
|:-------|:----------------|:-----|
| **分子（各选项作答人数/计数）** | ✅ 排除 | 只统计未被清洗的有效数据 |
| **分母（总人数/答题总人数）** | ✅ 排除 | 只统计未被清洗的有效数据（与分子口径一致） |
| **占比 = 分子 / 分母** | 分子排除，分母排除 | 反映各选项在清洗后有效数据中的分布比例，各选项占比合计 = 100% |

**核心规则：统计总人数和比例时，分子和分母统一使用清洗后的数据（排除 `strWashColumn` 中包含对应题目序号的数据）**


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
某条件的占比（百分比，保留2位小数）：
SELECT ROUND(COUNT(CASE WHEN <condition> THEN 1 END) * 100.0 / COUNT(*), 2) AS ratio FROM <table>

某类别占总数的百分比（带总数）：
SELECT
COUNT() AS total,
SUM(CASE WHEN <condition> THEN 1 ELSE 0 END) AS matched,
ROUND(SUM(CASE WHEN <condition> THEN 1 ELSE 0 END) * 100.0 / COUNT(), 2) AS ratio
FROM <table>

分类聚合占比：
SELECT
<group_column>,
COUNT() AS cnt,
ROUND(COUNT() * 100.0 / (SELECT COUNT(*) FROM <table>), 2) AS ratio
FROM <table>
GROUP BY <group_column>
ORDER BY cnt DESC

分类看某条件下的占比：
SELECT
<group_column>,
COUNT() AS total,
SUM(CASE WHEN <condition> THEN 1 ELSE 0 END) AS matched,
ROUND(SUM(CASE WHEN <condition> THEN 1 ELSE 0 END) * 100.0 / COUNT(), 2) AS ratio
FROM <table>
GROUP BY <group_column>
ORDER BY ratio DESC

**【重要】涉及数据清洗（strWashColumn）时的占比计算规则：**
  统计占比时，分子（各选项作答人数）和分母（答题总人数）都使用清洗后的数据，保持口径一致，确保各选项占比合计 = 100%。
  
  ✅ **正确示例：**
  ```sql
  SELECT 
      JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) AS `毕业去向`,
      COUNT(*) AS `作答人数`,
      ROUND(
          COUNT(*) * 100.0 / (
              SELECT COUNT(*) 
              FROM `research1`.`tbanswerrecordsnapshotdetail94part0` 
              WHERE `lQuestionnaireId` = 999 
                  AND `lSnapshotId` = 1946
                  AND `nAnswerState` = 2 
                  AND `strAnswer1` IS NOT NULL 
                  AND `strAnswer1` != '' 
                  AND JSON_VALID(`strAnswer1`) = 1 
                  AND JSON_LENGTH(`strAnswer1`) > 0
                  AND (`strWashColumn` IS NULL OR FIND_IN_SET('1', `strWashColumn`) = 0)  -- 分母排除清洗数据
          ), 
          2
      ) AS `占比`,
      (
          SELECT COUNT(*) 
          FROM `research1`.`tbanswerrecordsnapshotdetail94part0` 
          WHERE `lQuestionnaireId` = 999 
              AND `lSnapshotId` = 1946
              AND `nAnswerState` = 2 
              AND `strAnswer1` IS NOT NULL 
              AND `strAnswer1` != '' 
              AND JSON_VALID(`strAnswer1`) = 1 
              AND JSON_LENGTH(`strAnswer1`) > 0
              AND (`strWashColumn` IS NULL OR FIND_IN_SET('1', `strWashColumn`) = 0)  -- 分母排除清洗数据
      ) AS `答题总人数`
  FROM 
      `research1`.`tbanswerrecordsnapshotdetail94part0`
  WHERE 
      `lQuestionnaireId` = 999 
      AND `lSnapshotId` = 1946
      AND `nAnswerState` = 2 
      AND `strAnswer1` IS NOT NULL 
      AND `strAnswer1` != '' 
      AND JSON_VALID(`strAnswer1`) = 1 
      AND JSON_LENGTH(`strAnswer1`) > 0
      AND (`strWashColumn` IS NULL OR FIND_IN_SET('1', `strWashColumn`) = 0)  -- 分子排除清洗数据
  GROUP BY 
      JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value'))
  ORDER BY 
      `作答人数` DESC;
      
❌ 错误示例（分母错误地排除了清洗数据）：
-- 错误写法：分母中使用了 FIND_IN_SET 排除清洗数据
SELECT 
    JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) AS `毕业去向`,
    COUNT(*) AS `作答人数`,
    ROUND(
        COUNT(*) * 100.0 / (
            SELECT COUNT(*) 
            FROM `research1`.`tbanswerrecordsnapshotdetail94part0` 
            WHERE `lQuestionnaireId` = 999 
                AND `nAnswerState` = 2 
                AND `strAnswer1` IS NOT NULL 
                AND `strAnswer1` != '' 
                AND JSON_VALID(`strAnswer1`) = 1 
                AND JSON_LENGTH(`strAnswer1`) > 0
                AND (`strWashColumn` IS NULL OR FIND_IN_SET('1', `strWashColumn`) = 0)  -- ❌ 错误！分母不应排除清洗数据
        ), 
        2
    ) AS `占比`
FROM ...

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

17. 数据清洗约定
----------------------------------------------------------------------------------------------------
核心原则：

分子（作答人数/各选项计数）：统计时需排除 strWashColumn 中包含对应题目序号的数据

分母（总人数/答题总人数）：统计时不排除清洗数据，统计所有有效作答数据

占比计算：分子（清洗后）/ 分母（所有有效数据）

排除清洗数据示例（单表查询，包含总人数）：
SELECT 
    JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) AS `毕业去向`,
    COUNT(*) AS `作答人数`,
    ROUND(
        COUNT(*) * 100.0 / (
            SELECT COUNT(*) 
            FROM `research1`.`tbanswerrecordsnapshotdetail94part0` 
            WHERE `lQuestionnaireId` = 999 
                AND `nAnswerState` = 2 
                AND `strAnswer1` IS NOT NULL 
                AND `strAnswer1` != '' 
                AND JSON_VALID(`strAnswer1`) = 1 
                AND JSON_LENGTH(`strAnswer1`) > 0
            -- 注意：分母不排除清洗数据，统计所有有效数据
        ), 
        2
    ) AS `占比`,
    (
        SELECT COUNT(*) 
        FROM `research1`.`tbanswerrecordsnapshotdetail94part0` 
        WHERE `lQuestionnaireId` = 999 
            AND `nAnswerState` = 2 
            AND `strAnswer1` IS NOT NULL 
            AND `strAnswer1` != '' 
            AND JSON_VALID(`strAnswer1`) = 1 
            AND JSON_LENGTH(`strAnswer1`) > 0
        -- 分母：所有有效数据，不排除清洗数据
    ) AS `答题总人数`
FROM 
    `research1`.`tbanswerrecordsnapshotdetail94part0`
WHERE 
    `lQuestionnaireId` = 999 
    AND `nAnswerState` = 2 
    AND `strAnswer1` IS NOT NULL 
    AND `strAnswer1` != '' 
    AND JSON_VALID(`strAnswer1`) = 1 
    AND JSON_LENGTH(`strAnswer1`) > 0
    AND (`strWashColumn` IS NULL OR FIND_IN_SET('1', `strWashColumn`) = 0)  -- 分子排除清洗数据
GROUP BY 
    JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value'))
ORDER BY 
    `作答人数` DESC;
    
❌ 常见错误：分母错误地排除了清洗数据
-- 错误写法：分母中使用了 FIND_IN_SET 或 LIKE 排除清洗数据
-- 错误写法：分母中使用了 FIND_IN_SET 或 LIKE 排除清洗数据
SELECT 
    JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) AS `毕业去向`,
    COUNT(*) AS `作答人数`,
    ROUND(
        COUNT(*) * 100.0 / (
            SELECT COUNT(*) 
            FROM `research1`.`tbanswerrecordsnapshotdetail94part0` 
            WHERE `lQuestionnaireId` = 999 
                AND `nAnswerState` = 2 
                AND `strAnswer1` IS NOT NULL 
                AND `strAnswer1` != '' 
                AND JSON_VALID(`strAnswer1`) = 1 
                AND JSON_LENGTH(`strAnswer1`) > 0
                AND (`strWashColumn` IS NULL OR FIND_IN_SET('1', `strWashColumn`) = 0)  -- ❌ 错误！分母不应排除清洗数据
        ), 
        2
    ) AS `占比`
FROM ...
   
    
18. 薪酬统计约定
--------------------------------------------------------------------------------------------------------------------------    
薪酬统计示例：
SELECT 
    ROUND(AVG(CAST(JSON_UNQUOTE(JSON_EXTRACT(`strSalary`, '$[0].value')) AS DECIMAL(18,2))), 2) AS '平均薪资',
    ROUND(
        (
            SELECT AVG(salary)
            FROM (
                SELECT 
                    CAST(JSON_UNQUOTE(JSON_EXTRACT(`strSalary`, '$[0].value')) AS DECIMAL(18,2)) AS salary,
                    @rownum := @rownum + 1 AS row_num
                FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
                CROSS JOIN (SELECT @rownum := 0) AS vars
                WHERE `lQuestionnaireId` = 686 
                    AND `lSnapshotId` = 2280 
                    AND `nAnswerState` IN (1, 2)
                    AND `strSalary` IS NOT NULL 
                    AND `strSalary` != '' 
                    AND JSON_VALID(`strSalary`) = 1 
                    AND JSON_LENGTH(`strSalary`) > 0
                    AND (`strWashColumn` IS NULL OR FIND_IN_SET('strSalary', `strWashColumn`) = 0)
                    AND (
                        JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%国内就业%'
                        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%境内就业%'
                        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%单位就业%'
                        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%签订就业协议或劳动合同就业%'
                    )
                ORDER BY salary
            ) t,
            (
                SELECT COUNT(*) AS total_count 
                FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
                WHERE `lQuestionnaireId` = 686 
                    AND `lSnapshotId` = 2280 
                    AND `nAnswerState` IN (1, 2)
                    AND `strSalary` IS NOT NULL 
                    AND `strSalary` != '' 
                    AND JSON_VALID(`strSalary`) = 1 
                    AND JSON_LENGTH(`strSalary`) > 0
                    AND (`strWashColumn` IS NULL OR FIND_IN_SET('strSalary', `strWashColumn`) = 0)
                    AND (
                        JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%国内就业%'
                        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%境内就业%'
                        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%单位就业%'
                        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%签订就业协议或劳动合同就业%'
                    )
            ) AS tc
            WHERE row_num IN (FLOOR((tc.total_count + 1) / 2), CEIL((tc.total_count + 1) / 2))
        ),
        2
    ) AS '中位数'
FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
WHERE `lQuestionnaireId` = 686 
    AND `lSnapshotId` = 2280 
    AND `nAnswerState` IN (1, 2)
    AND `strSalary` IS NOT NULL 
    AND `strSalary` != '' 
    AND JSON_VALID(`strSalary`) = 1 
    AND JSON_LENGTH(`strSalary`) > 0
    AND (`strWashColumn` IS NULL OR FIND_IN_SET('strSalary', `strWashColumn`) = 0)
    AND (
        JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%国内就业%'
        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%境内就业%'
        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%单位就业%'
        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%签订就业协议或劳动合同就业%'
    );
    

19. 单选五维题型/单选组合题型示例
- 单选五维题型示例：
SELECT 
  '占比' AS `指标`,
  CONCAT(ROUND(COALESCE(SUM(CASE WHEN t.`选项` = '完全胜任' THEN t.`作答人数` END), 0) * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `非常胜任`,
  CONCAT(ROUND(COALESCE(SUM(CASE WHEN t.`选项` = '比较胜任' THEN t.`作答人数` END), 0) * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `比较胜任`,
  CONCAT(ROUND(COALESCE(SUM(CASE WHEN t.`选项` = '基本胜任' THEN t.`作答人数` END), 0) * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `基本胜任`,
  CONCAT(ROUND(COALESCE(SUM(CASE WHEN t.`选项` = '较不胜任' THEN t.`作答人数` END), 0) * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `不太胜任`,
  CONCAT(ROUND(COALESCE(SUM(CASE WHEN t.`选项` = '不能胜任' THEN t.`作答人数` END), 0) * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `完全不胜任`,
  -- 修改：满意度 = 非常胜任 + 比较胜任 + 基本胜任 的人数占比
  CONCAT(ROUND(
    (COALESCE(SUM(CASE WHEN t.`选项` = '完全胜任' THEN t.`作答人数` END), 0) 
   + COALESCE(SUM(CASE WHEN t.`选项` = '比较胜任' THEN t.`作答人数` END), 0) 
   + COALESCE(SUM(CASE WHEN t.`选项` = '基本胜任' THEN t.`作答人数` END), 0)) 
    * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `满意度`,
  ROUND((COALESCE(SUM(CASE WHEN t.`选项` = '完全胜任' THEN t.`作答人数` END), 0) * 5.0 
       + COALESCE(SUM(CASE WHEN t.`选项` = '比较胜任' THEN t.`作答人数` END), 0) * 4.0 
       + COALESCE(SUM(CASE WHEN t.`选项` = '基本胜任' THEN t.`作答人数` END), 0) * 3.0 
       + COALESCE(SUM(CASE WHEN t.`选项` = '较不胜任' THEN t.`作答人数` END), 0) * 2.0 
       + COALESCE(SUM(CASE WHEN t.`选项` = '不能胜任' THEN t.`作答人数` END), 0) * 1.0) / NULLIF(t_total.`答题总人数`, 0), 2) AS `均值`,
  t_total.`答题总人数` AS `样本量`
FROM (
  SELECT JSON_UNQUOTE(JSON_EXTRACT(`strAnswer23`, '$[0].value')) AS `选项`, COUNT(*) AS `作答人数`
  FROM `research2`.`tbanswerrecordsnapshotdetail12part0`
  WHERE `lQuestionnaireId` = 798 
    AND `lSnapshotId` = 2128 
    AND `nAnswerState` IN (1, 2)
    AND `strAnswer23` IS NOT NULL 
    AND `strAnswer23` != '' 
    AND JSON_VALID(`strAnswer23`) = 1 
    AND JSON_LENGTH(`strAnswer23`) > 0
    AND (`strWashColumn` IS NULL OR FIND_IN_SET('23', `strWashColumn`) = 0)
  GROUP BY JSON_UNQUOTE(JSON_EXTRACT(`strAnswer23`, '$[0].value'))
) AS t
CROSS JOIN (
  SELECT COUNT(*) AS `答题总人数`
  FROM `research2`.`tbanswerrecordsnapshotdetail12part0`
  WHERE `lQuestionnaireId` = 798 
    AND `lSnapshotId` = 2128 
    AND `nAnswerState` IN (1, 2)
    AND `strAnswer23` IS NOT NULL 
    AND `strAnswer23` != '' 
    AND JSON_VALID(`strAnswer23`) = 1 
    AND JSON_LENGTH(`strAnswer23`) > 0
    AND (`strWashColumn` IS NULL OR FIND_IN_SET('23', `strWashColumn`) = 0)
) AS t_total;

- 单选组合题型示例：
SELECT 
    CASE t.idx
        WHEN 0 THEN '师德师风（情操高尚，教风端正，遵章守纪，关爱学生）'
        WHEN 1 THEN '教学组织（清晰地阐述课程目标与知识逻辑联系、教学内容层次分明）'
        WHEN 2 THEN '教学投入（准备充分，上课投入，讲课感染力、亲和力强）'
        WHEN 3 THEN '教学能力（理论联系实际、教学方式方法多样，教学信息化技术运用恰当）'
        WHEN 4 THEN '师生交流互动（注重课内外师生互动交流，乐于服务指导学生）'
        WHEN 5 THEN '教师学业指导（指导改进学习方法，关于课堂表现或学业困惑及时反馈）'
        WHEN 6 THEN '教学科研水平总体满意度'
    END AS `指标`,
    CONCAT(ROUND(t.`很满意人数` * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `很满意`,
    CONCAT(ROUND(t.`比较满意人数` * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `比较满意`,
    CONCAT(ROUND(t.`基本满意人数` * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `基本满意`,
    CONCAT(ROUND(t.`比较不满意人数` * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `比较不满意`,
    CONCAT(ROUND(t.`很不满意人数` * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `很不满意`,
    CONCAT(ROUND(t.`满意人数` * 100.0 / NULLIF(t_total.`答题总人数`, 0), 2), '%') AS `满意度`,
    ROUND(t.`总分` / NULLIF(t.`作答人数`, 0), 2) AS `均值`,
    t_total.`答题总人数` AS `样本量`
FROM (
    SELECT 
        0 AS idx,
        COUNT(*) AS `作答人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) = '很满意' THEN 5
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) = '比较满意' THEN 4
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) = '基本满意' THEN 3
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) = '比较不满意' THEN 2
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) = '很不满意' THEN 1
                 ELSE 0 END) AS `总分`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) = '很满意' THEN 1 ELSE 0 END) AS `很满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) = '比较满意' THEN 1 ELSE 0 END) AS `比较满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) = '基本满意' THEN 1 ELSE 0 END) AS `基本满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) = '比较不满意' THEN 1 ELSE 0 END) AS `比较不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) = '很不满意' THEN 1 ELSE 0 END) AS `很不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[0].value')) IN ('很满意', '比较满意', '基本满意') THEN 1 ELSE 0 END) AS `满意人数`
    FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
    WHERE `lQuestionnaireId` = 686 
        AND `lSnapshotId` = 2280 
        AND `nAnswerState` IN (1, 2)
        AND `strAnswer62` IS NOT NULL 
        AND `strAnswer62` != '' 
        AND JSON_VALID(`strAnswer62`) = 1
        AND (`strWashColumn` IS NULL OR FIND_IN_SET('62', `strWashColumn`) = 0)
    
    UNION ALL
    SELECT 
        1 AS idx,
        COUNT(*) AS `作答人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) = '很满意' THEN 5
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) = '比较满意' THEN 4
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) = '基本满意' THEN 3
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) = '比较不满意' THEN 2
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) = '很不满意' THEN 1
                 ELSE 0 END) AS `总分`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) = '很满意' THEN 1 ELSE 0 END) AS `很满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) = '比较满意' THEN 1 ELSE 0 END) AS `比较满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) = '基本满意' THEN 1 ELSE 0 END) AS `基本满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) = '比较不满意' THEN 1 ELSE 0 END) AS `比较不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) = '很不满意' THEN 1 ELSE 0 END) AS `很不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[1].value')) IN ('很满意', '比较满意', '基本满意') THEN 1 ELSE 0 END) AS `满意人数`
    FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
    WHERE `lQuestionnaireId` = 686 
        AND `lSnapshotId` = 2280 
        AND `nAnswerState` IN (1, 2)
        AND `strAnswer62` IS NOT NULL 
        AND `strAnswer62` != '' 
        AND JSON_VALID(`strAnswer62`) = 1
        AND (`strWashColumn` IS NULL OR FIND_IN_SET('62', `strWashColumn`) = 0)
    
    UNION ALL
    SELECT 
        2 AS idx,
        COUNT(*) AS `作答人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) = '很满意' THEN 5
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) = '比较满意' THEN 4
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) = '基本满意' THEN 3
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) = '比较不满意' THEN 2
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) = '很不满意' THEN 1
                 ELSE 0 END) AS `总分`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) = '很满意' THEN 1 ELSE 0 END) AS `很满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) = '比较满意' THEN 1 ELSE 0 END) AS `比较满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) = '基本满意' THEN 1 ELSE 0 END) AS `基本满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) = '比较不满意' THEN 1 ELSE 0 END) AS `比较不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) = '很不满意' THEN 1 ELSE 0 END) AS `很不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[2].value')) IN ('很满意', '比较满意', '基本满意') THEN 1 ELSE 0 END) AS `满意人数`
    FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
    WHERE `lQuestionnaireId` = 686 
        AND `lSnapshotId` = 2280 
        AND `nAnswerState` IN (1, 2)
        AND `strAnswer62` IS NOT NULL 
        AND `strAnswer62` != '' 
        AND JSON_VALID(`strAnswer62`) = 1
        AND (`strWashColumn` IS NULL OR FIND_IN_SET('62', `strWashColumn`) = 0)
    
    UNION ALL
    SELECT 
        3 AS idx,
        COUNT(*) AS `作答人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) = '很满意' THEN 5
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) = '比较满意' THEN 4
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) = '基本满意' THEN 3
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) = '比较不满意' THEN 2
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) = '很不满意' THEN 1
                 ELSE 0 END) AS `总分`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) = '很满意' THEN 1 ELSE 0 END) AS `很满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) = '比较满意' THEN 1 ELSE 0 END) AS `比较满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) = '基本满意' THEN 1 ELSE 0 END) AS `基本满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) = '比较不满意' THEN 1 ELSE 0 END) AS `比较不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) = '很不满意' THEN 1 ELSE 0 END) AS `很不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[3].value')) IN ('很满意', '比较满意', '基本满意') THEN 1 ELSE 0 END) AS `满意人数`
    FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
    WHERE `lQuestionnaireId` = 686 
        AND `lSnapshotId` = 2280 
        AND `nAnswerState` IN (1, 2)
        AND `strAnswer62` IS NOT NULL 
        AND `strAnswer62` != '' 
        AND JSON_VALID(`strAnswer62`) = 1
        AND (`strWashColumn` IS NULL OR FIND_IN_SET('62', `strWashColumn`) = 0)
    
    UNION ALL
    SELECT 
        4 AS idx,
        COUNT(*) AS `作答人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) = '很满意' THEN 5
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) = '比较满意' THEN 4
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) = '基本满意' THEN 3
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) = '比较不满意' THEN 2
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) = '很不满意' THEN 1
                 ELSE 0 END) AS `总分`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) = '很满意' THEN 1 ELSE 0 END) AS `很满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) = '比较满意' THEN 1 ELSE 0 END) AS `比较满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) = '基本满意' THEN 1 ELSE 0 END) AS `基本满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) = '比较不满意' THEN 1 ELSE 0 END) AS `比较不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) = '很不满意' THEN 1 ELSE 0 END) AS `很不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[4].value')) IN ('很满意', '比较满意', '基本满意') THEN 1 ELSE 0 END) AS `满意人数`
    FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
    WHERE `lQuestionnaireId` = 686 
        AND `lSnapshotId` = 2280 
        AND `nAnswerState` IN (1, 2)
        AND `strAnswer62` IS NOT NULL 
        AND `strAnswer62` != '' 
        AND JSON_VALID(`strAnswer62`) = 1
        AND (`strWashColumn` IS NULL OR FIND_IN_SET('62', `strWashColumn`) = 0)
    
    UNION ALL
    SELECT 
        5 AS idx,
        COUNT(*) AS `作答人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) = '很满意' THEN 5
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) = '比较满意' THEN 4
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) = '基本满意' THEN 3
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) = '比较不满意' THEN 2
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) = '很不满意' THEN 1
                 ELSE 0 END) AS `总分`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) = '很满意' THEN 1 ELSE 0 END) AS `很满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) = '比较满意' THEN 1 ELSE 0 END) AS `比较满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) = '基本满意' THEN 1 ELSE 0 END) AS `基本满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) = '比较不满意' THEN 1 ELSE 0 END) AS `比较不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) = '很不满意' THEN 1 ELSE 0 END) AS `很不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[5].value')) IN ('很满意', '比较满意', '基本满意') THEN 1 ELSE 0 END) AS `满意人数`
    FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
    WHERE `lQuestionnaireId` = 686 
        AND `lSnapshotId` = 2280 
        AND `nAnswerState` IN (1, 2)
        AND `strAnswer62` IS NOT NULL 
        AND `strAnswer62` != '' 
        AND JSON_VALID(`strAnswer62`) = 1
        AND (`strWashColumn` IS NULL OR FIND_IN_SET('62', `strWashColumn`) = 0)
    
    UNION ALL
    SELECT 
        6 AS idx,
        COUNT(*) AS `作答人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) = '很满意' THEN 5
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) = '比较满意' THEN 4
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) = '基本满意' THEN 3
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) = '比较不满意' THEN 2
                 WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) = '很不满意' THEN 1
                 ELSE 0 END) AS `总分`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) = '很满意' THEN 1 ELSE 0 END) AS `很满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) = '比较满意' THEN 1 ELSE 0 END) AS `比较满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) = '基本满意' THEN 1 ELSE 0 END) AS `基本满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) = '比较不满意' THEN 1 ELSE 0 END) AS `比较不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) = '很不满意' THEN 1 ELSE 0 END) AS `很不满意人数`,
        SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(`strAnswer62`, '$[6].value')) IN ('很满意', '比较满意', '基本满意') THEN 1 ELSE 0 END) AS `满意人数`
    FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
    WHERE `lQuestionnaireId` = 686 
        AND `lSnapshotId` = 2280 
        AND `nAnswerState` IN (1, 2)
        AND `strAnswer62` IS NOT NULL 
        AND `strAnswer62` != '' 
        AND JSON_VALID(`strAnswer62`) = 1
        AND (`strWashColumn` IS NULL OR FIND_IN_SET('62', `strWashColumn`) = 0)
) AS t
CROSS JOIN (
    SELECT COUNT(*) AS `答题总人数`
    FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
    WHERE `lQuestionnaireId` = 686 
        AND `lSnapshotId` = 2280 
        AND `nAnswerState` IN (1, 2)
        AND `strAnswer62` IS NOT NULL 
        AND `strAnswer62` != '' 
        AND JSON_VALID(`strAnswer62`) = 1
) AS t_total
ORDER BY t.idx;

    
20. 关联学生毕业年份、学历名称、学院名称 、专业属性示例：
-----------------
SELECT s.strStudentName, s.strStudentCode,
      s.strAcademyName, s.strSpecialityName,
      s.strGraduationYear, s.strSex,
      a.lResearchId, a.lSnapshotId,
      a.nAnswerState, a.bWashed,
      a.strAnswer0, a.strAnswer1
  FROM research0.tbstudent51 s
  JOIN research2.tbanswerrecordsnapshotdetail28part0 a
    ON s.lOrgId = a.lOrgId AND s.strIdentity = a.strIdentity
  WHERE s.lRespondentId = 627
    AND a.nAnswerState = 2
    
21. 应届毕业生_就业情况_薪酬；毕业生_就业结果_月收入；应届毕业生_就业情况_工作满意度；应届毕业生_就业情况_专业相关度这几个问题要关联应届毕业生去向结果中结果包含“国内就业"、"境内就业"、"单位就业"、"签订就业协议或劳动合同就业"的人
-------------------------------------------------------------
应届毕业生去向包含“国内就业"、"境内就业"、"单位就业"、"签订就业协议或劳动合同就业"示例：
SELECT 
    JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) AS `毕业去向`,
    COUNT(*) AS `作答人数`,
    CONCAT(ROUND(COUNT(*) * 100.0 / ( 
        SELECT COUNT(*) 
        FROM `research2`.`tbanswerrecordsnapshotdetail28part0` 
        WHERE `lQuestionnaireId` = 686 
            AND `lSnapshotId` = 2280 
            AND `nAnswerState` IN (1, 2) 
            AND `strAnswer1` IS NOT NULL 
            AND `strAnswer1` != '' 
            AND JSON_VALID(`strAnswer1`) = 1 
            AND JSON_LENGTH(`strAnswer1`) > 0
    ), 2), '%') AS `占比`,
    ( 
        SELECT COUNT(*) 
        FROM `research2`.`tbanswerrecordsnapshotdetail28part0` 
        WHERE `lQuestionnaireId` = 686 
            AND `lSnapshotId` = 2280 
            AND `nAnswerState` IN (1, 2) 
            AND `strAnswer1` IS NOT NULL 
            AND `strAnswer1` != '' 
            AND JSON_VALID(`strAnswer1`) = 1 
            AND JSON_LENGTH(`strAnswer1`) > 0
    ) AS `答题总人数`
FROM `research2`.`tbanswerrecordsnapshotdetail28part0`
WHERE `lQuestionnaireId` = 686 
    AND `lSnapshotId` = 2280 
    AND `nAnswerState` IN (1, 2) 
    AND `strAnswer1` IS NOT NULL 
    AND `strAnswer1` != '' 
    AND JSON_VALID(`strAnswer1`) = 1 
    AND JSON_LENGTH(`strAnswer1`) > 0 
    AND (`strWashColumn` IS NULL OR FIND_IN_SET('1', `strWashColumn`) = 0)
    AND (
        JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%国内就业%'
        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%境内就业%'
        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%单位就业%'
        OR JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value')) LIKE '%签订就业协议或劳动合同就业%'
    )
GROUP BY JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value'))
ORDER BY `作答人数` DESC;

21. SQL编写规范
-----------------

- 所有 SQL 关键字统一大写（SELECT, FROM, WHERE, GROUP BY, ORDER BY, LIMIT 等）
- 表名和字段名使用反引号 `` ` `` 包裹，避免与关键字冲突
- 字符串值使用单引号 `'`
- 生成的 SQL 只返回查询语句，不要包含 USE 或 SET 等管理命令
- 单条 SQL 即可完成时，不要使用 UNION 或复杂的多层嵌套
- 保持 SQL 可读性，合理缩进
- 如果用户问题与表结构不相关或不明确，返回空字符串
- 生成的 SQL 必须在 **MySQL 5.7.28** 语法下合法可执行
- 查询字段别名避免使用mysql中的关键字和保留字
- **禁止使用** MySQL 8.0+ 的语法特性，包括但不限于：窗口函数（`OVER()`、`ROW_NUMBER()`、`RANK()`、`DENSE_RANK()`、`LAG()`、`LEAD()` 等）、`WITH`（CTE 公用表表达式）、`LATERAL`、`JSON_TABLE` 等
