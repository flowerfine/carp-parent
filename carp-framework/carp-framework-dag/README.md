# Dag Framework

DAG 存储服务。核心功能：

* DAG 存储。提供图的点、线存储，方便基于 DAG 建模的业务快速处理上层业务，无需关注底层存储问题。
* DAG 算法。基于 [jgrapht](https://github.com/jgrapht/jgrapht) 封装的算法实现
* 可视化。提供工具将 DAG 转化成 PlantUML 和 Mermaid 格式，用户将生成的  PlantUML 和 Mermaid 数据粘贴、复制到对应的在线编辑器，即可查看 DAG 依赖信息。如果要在应用中实现可视化信息，直接集成 PlantUML 和 Mermaid 也是一个方案，可满足简单的可视化需求
  * [PlantUML Editor](https://editor.plantuml.com/uml)
  * [Mermaid Live Editor](https://mermaid.live/edit)

## 可视化

对于有很强的定制化需求的可视化需求，需要开发复杂的 web 页面进行展示。可参考的 web 解决方案：

* React Flow。[xyflow](https://github.com/xyflow/xyflow)
  * [ProFlow](https://pro-flow.antdigital.dev/)
  * [flowify](https://github.com/radityaharya/flowify)
  * [flowmix-flow](https://github.com/MrXujiang/flowmix-flow)
    * [Flowmix 可视化编辑器](https://flowmix.turntip.cn/flow/edit?id=KHDX7XrVqRSQevboUJ_zG)
  * [React Flow Dev](https://react-flow-dev.netlify.app/)
  * [Reactflow Examples](https://reactflowexample.vercel.app/home)
* [x6](https://x6.antv.antgroup.com/)
* [FlowGram.AI](https://flowgram.ai/index.html)
