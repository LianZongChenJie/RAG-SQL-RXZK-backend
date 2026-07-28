# ECharts 图表生成规则
====================

> **目标图表库：Apache ECharts**
> 生成的所有图表配置必须遵循以下规则。
> **ECharts 版本：5.6.0**

本文档定义了 AI 智能体在根据用户问题和查询结果生成 ECharts 图表配置时必须遵守的规则。
请严格按照以下规则生成对应的 ECharts option JSON。

---

## 1. 图表类型支持

仅支持以下三种图表类型：

| 图表类型 | 适用场景 |
|---------|---------|
| `pie`（饼图） | 占比、分布、构成比例 |
| `bar`（柱状图） | 比较分类数据大小、排名 |
| `line`（折线图） | 趋势变化、时间序列 |

- 有分组对比需求时，柱状图和折线图使用**多个 series**；仅一个分组维度时使用单个 series。

---

## 2. 图例（legend）规则

### 2.1 图例位置

| 图表类型 | 图例位置 | 配置 |
|---------|---------|------|
| 柱状图 / 折线图 | 标题下方，水平居中 | `orient: 'horizontal'`, `top: 40`, `left: 'center'` |
| 饼图 | 右侧垂直居中 | `orient: 'vertical'`, `right: '1%'`, `top: 'middle'` |

**柱状图/折线图图例配置：**
```json
"legend": {
    "type": "scroll",
    "orient": "horizontal",
    "top": 40,
    "left": "center"
}
```
需配合 `grid.top: 70` 留出间距。

**饼图图例配置：**
```json
"legend": {
    "type": "scroll",
    "orient": "vertical",
    "right": "1%",
    "top": "middle"
}
```

### 2.2 图例翻页
- 图例项数量 **> 6** 时，必须设置 `"type": "scroll"`，开启翻页按钮。
- ≤ 6 时可省略 `type` 或保留 `"scroll"`。

### 2.3 图例数据
- **柱状图/折线图**：`legend.data` 对应每个 series 的 `name`。
- **饼图**：`legend.data` 必须为 `series[0].data` 中所有 `name` 的列表，禁止为空数组。

---

## 3. 柱状图规则

### 3.1 标题居中
```json
"title": { "text": "...", "left": "center" }
```

### 3.2 多系列（分组柱状图）
- 每个分组对应一个 series，`name` 不同。
- `legend.data` 与 series 的 `name` 一一对应。
- `xAxis.axisLabel.rotate` 设为 `45`（左低右高斜向上），标签较短时可酌情减小或省略。

**示例（多系列）：**
```json
{
    "title": {"text": "各学院各年级毕业人数统计", "left": "center"},
    "tooltip": {},
    "legend": {
        "type": "scroll",
        "orient": "horizontal",
        "top": 40,
        "left": "center",
        "data": ["2021届", "2022届", "2023届"]
    },
    "grid": { "top": 70 },
    "xAxis": {
        "type": "category",
        "data": ["计算机学院", "数学学院", "物理学院"],
        "axisLabel": { "rotate": 45 }
    },
    "yAxis": { "type": "value" },
    "series": [
        {"name": "2021届", "type": "bar", "data": [320, 280, 210]},
        {"name": "2022届", "type": "bar", "data": [350, 300, 230]},
        {"name": "2023届", "type": "bar", "data": [380, 310, 250]}
    ]
}
```

### 3.3 单系列
- 使用单个 series，`legend.data` 包含该 series 的 `name`。

---

## 4. 饼图规则

### 4.1 标题居中
```json
"title": { "text": "...", "left": "center" }
```

### 4.2 图例数据
- `legend.data` 必须为所有数据分片的 `name` 列表，与 `series[0].data` 一一对应。

### 4.3 饼图中心点动态调整（核心规则）
- **默认情况**（数据项 ≤ 10）：`"center": ["40%", "50%"]`
- **饼图默认采用圆角环形图
**示例：**
```json
{
  "tooltip": {
    "trigger": "item"
  },
  "legend": {
    "top": "5%",
    "left": "center"
  },
  "series": [
    {
      "name": "Access From",
      "type": "pie",
      "radius": ["40%", "70%"],
      "avoidLabelOverlap": false,
      "itemStyle": {
        "borderRadius": 10,
        "borderColor": "#fff",
        "borderWidth": 2
      },
      "label": {
        "show": false,
        "position": "center"
      },
      "emphasis": {
        "label": {
          "show": true,
          "fontSize": 40,
          "fontWeight": "bold"
        }
      },
      "labelLine": {
        "show": false
      },
      "data": [
        { "value": 1048, "name": "Search Engine" },
        { "value": 735, "name": "Direct" },
        { "value": 580, "name": "Email" },
        { "value": 484, "name": "Union Ads" },
        { "value": 300, "name": "Video Ads" }
      ]
    }
  ]
}
```

> **注意**：图例项数量也受相同阈值影响，若 > 6 时已开启 `scroll`，但中心点下调独立以 `数据项数量 > 10` 为准。

---

## 5. 折线图规则

### 5.1 标题居中
```json
"title": { "text": "...", "left": "center" }
```

### 5.2 图例配置
- 同柱状图：`orient: 'horizontal'`, `top: 40`, `left: 'center'`，`grid.top: 70`。
- 图例项 > 6 时设置 `"type": "scroll"`。

### 5.3 横坐标标签旋转
- **xAxis 数据项数量 > 6** 时，`axisLabel.rotate` 设为 `45`（左低右高）。
- ≤ 6 时可省略。

### 5.4 多系列 / 单系列
- 多组趋势使用多个 series，`legend.data` 包含所有系列名称。
- 单条趋势使用单个 series。

**示例（多系列）：**
```json
{
    "title": {"text": "历年各学院招生人数趋势", "left": "center"},
    "tooltip": {},
    "legend": {
        "type": "scroll",
        "orient": "horizontal",
        "top": 40,
        "left": "center",
        "data": ["计算机学院", "数学学院", "物理学院"]
    },
    "grid": { "top": 70 },
    "xAxis": {
        "type": "category",
        "data": ["2020年", "2021年", "2022年", "2023年"],
        "axisLabel": { "rotate": 45 }
    },
    "yAxis": { "type": "value" },
    "series": [
        {"name": "计算机学院", "type": "line", "data": [1200, 1350, 1480, 1600]},
        {"name": "数学学院", "type": "line", "data": [800, 850, 900, 950]},
        {"name": "物理学院", "type": "line", "data": [500, 550, 600, 650]}
    ]
}
```

---

## 6. 通用要求

- **必含字段**：`title`、`tooltip`、`legend`、`xAxis`（饼图可为 `[]`）、`yAxis`（饼图可为 `[]`）、`series`（含 `type`/`data`）。
- **文字语言**：所有标签使用中文。
- **饼图额外要求**：必须设置 `radius` 和 `center`，根据数据项数量动态调整 `center`（默认 `["40%","50%"]`，>10 项时为 `["40%","75%"]`）并且渲染高度大于600px。
- **不适配情况**：若查询结果不适合图表展示（如仅单条数据或非数值数据），返回空字符串 `""`。
- **输出格式**：直接输出 JSON，不包含任何解释性文字，不用 markdown 代码块包裹。

---

以上规则已整合并更新，可直接用于 AI 图表生成指令。如有进一步调整需求，请告知。