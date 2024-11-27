# Carp

carp-parent 为 [carp](https://github.com/flowerfine/carp) 项目提供统一的依赖管理，确保依赖全局一致。

## 使用方式

#### Maven’s Bill of Material (BOM)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>cn.sliew</groupId>
            <artifactId>carp-dependencies</artifactId>
            <version>0.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 基于继承关系的Maven父依赖

```shell
<parent>
    <groupId>cn.sliew</groupId>
    <artifactId>carp-spring-boot-parent</artifactId>
    <version>0.0.1</version>
    <relativePath/>
</parent>
```

## 项目发版

参考：[Release](./docs/Release.md)

## Code of Conduct

This project adheres to the Contributor Covenant [code of conduct](https://www.contributor-covenant.org/version/2/1/code_of_conduct/)

## Contributing

For contributions, please refer [CONTRIBUTING](https://github.com/flowerfine/carp)

Thanks for all people who already contributed to Carp!

<a href="https://github.com/flowerfine/carp/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=flowerfine/carp" /></a>

## Contact

* Bugs and Features: [Issues](https://github.com/flowerfine/carp/issues)

## License

Carp is licenced under the Apache License Version 2.0, link is [here](https://www.apache.org/licenses/LICENSE-2.0.txt).