# Release

发版规则：

* 只对 main 和 `v*` 有效。一般只在 tag 的时候，才会携带 `v` 前缀，所以只对 main 和 tag 做发版
* main 分支：发布 SNPASHOT 版本
* tag：从 main 分支 checkout 发版分支，修改发版分支版本为 RELEASE 版本，创建 tag，push tag 触发发版
* 发版完成后，需及时更新 dev 分支至下个迭代版本

## 版本号

```shell
# 设置新的版本号
mvn versions:set -DnewVersion=0.0.1-SNAPSHOT 

# 撤销设置
mvn versions:revert

# 提交设置
mvn versions:commit
```

## 打包

```shell
mvn clean install -B -U -T 4C -Dfast -Pdist 
mvn clean install -B -U -T 4C -pl carp-dependencies -am
mvn clean deploy -B -U -T 4C -Dfast -DskipTests -Pdist -Poss-release -Dgpg.passphrase=${{ secrets.GPG_PASSWORD }}
```

| 参数 | 全名称                 | 说明                                                         |
| ---- | ---------------------- | ------------------------------------------------------------ |
| -B   |                        | 启用批处理模式。在批处理模式下，Maven 会以非交互的方式运行，Maven 不会等待用户的确认或输入而是直接按照预设的行为进行处理 |
| -U   |                        | 强制更新快照版本依赖项及插件。项目依赖于其他项目的最新开发版本（即SNAPSHOT版本），或想要确保是最新的版本时，可以使用这个选项 |
| -T   |                        | 多线程构建。4C 即代表 4 个线程。当项目插件或模块依赖于特定顺序执行的情况下，并行构建需禁用 |
| -pl  | --projects             | 选项后可跟随{groupId}:{artifactId}或者所选模块的相对路径(多个模块以逗号分隔) |
| -am  | --also-make            | 表示同时处理选定模块所依赖的模块                             |
| -amd | --also-make-dependents | 表示同时处理依赖选定模块的模块                               |
| -N   | --non-                 | 表示不递归子模块                                             |
| -rf  | --resume-frm           | 表示从指定模块开始继续处理                                   |

## Tag

```shell
# 创建 tag
git tag -a v0.0.1 -m "0.0.1 release"

# 提交 tag
git push origin v0.0.1

# 提交 tags
git push origin --tags
```

## Sonatype

在 github 的 `Settings` -> `Secrets and variables` -> `Actions` -> `Repository secrets` 添加 maven 发布密钥：

* SONATYPE_USER。查看 [sonatype](https://s01.oss.sonatype.org/#welcome) 登陆用户名，参考：[sonatype](https://issues.sonatype.org/secure/Signup!default.jspa)
* SONATYPE_PASSWORD。查看 [sonatype](https://s01.oss.sonatype.org/#welcome) 登陆密码，参考：[sonatype](https://issues.sonatype.org/secure/Signup!default.jspa)
* GPG_PASSWORD。gpg 密码。创建 gpg 时需设置用户名、密码和邮箱。
* GPG_SECRET。使用 `gpg --list-secret-keys` 查看 gpg 私钥，在用 `gpg -a --export-secret-keys KEY_ID` 导出密钥，添加至 github。

参考：

* [maven发布jar 到中央仓库](https://juejin.cn/post/7089402732649381896)
* [发布Jar包到Maven中央仓库](https://github.com/xuxueli/xuxueli.github.io/blob/master/blog/notebook/9-%E5%85%B6%E4%BB%96/%E5%8F%91%E5%B8%83Jar%E5%8C%85%E5%88%B0Maven%E4%B8%AD%E5%A4%AE%E4%BB%93%E5%BA%93.md)
* [Deploy to Maven Central with Github Actions: Step-by-step guide](https://www.bitshifted.co/blog/deploy-maven-central-github-actions-step-by-step-guide/)。使用 user token

## gpg

```shell
# 查看公钥
gpg --list-keys
# 查看私钥
gpg --list-secret-keys

gpg --delete-key [your key]
gpg --delete-secret-keys [your key]

gpg --keyserver hkp://keyserver.ubuntu.com --send-keys [your key]
gpg --keyserver hkp://keyserver.ubuntu.com --recv-keys [your key]
```

gpg --keyserver hkp://keyserver.ubuntu.com --recv-keys A6289E39FDCBC181ADD14994076C30ADAC85D591