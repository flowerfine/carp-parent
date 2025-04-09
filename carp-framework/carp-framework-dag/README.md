# Dag Framework

DAG 存储服务。核心功能：

* DAG 存储。提供图的点、线存储，方便基于 DAG 建模的业务快速处理上层业务，无需关注底层存储问题。
* DAG 算法。基于 [jgrapht](https://github.com/jgrapht/jgrapht) 封装的算法实现
* 可视化。提供工具将 DAG 转化成 PlantUML 和 Mermaid 格式，用户将生成的  PlantUML 和 Mermaid 数据粘贴、复制到对应的在线编辑器，即可查看 DAG 依赖信息。
  * [PlantUML Editor](https://editor.plantuml.com/uml)
  * [Mermaid Live Editor](https://mermaid.live/edit)
