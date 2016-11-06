# ms-sample2

## Gradleのインストール

ダウンロードする
> https://gradle.org/gradle-download/

zipを展開する
> C:\develop\gradle-3.1

パスを通す
> GRADLE_HOME=C:\develop\gradle-3.1
> PATH=%PATH%;%GRADLE_HOME%\bin

バージョン確認
```
gradle -v
```

## Javaアプリ作成

プロジェクト用のディレクトリを作成して初期化
```
mkdir ms-sample2
cd ms-sample2
gradle init --type java-library
```

ソース
src/main/java/sample2/Hello.java

```java
package sample2;
class Hello {
    public static void main(String[] args) {
        System.out.println("Hello World.");
    }
}
```

ビルド
```bash
gradle build
```

実行
```bash
java -cp build/lib/ms-sample2.jar sample2.Hello
```

gradleから実行するには
build.gradleの修正
```
apply plugin: 'application'
mainClassName = 'Hello'
```

実行
```bash
gradle run
```

## Spring Bootアプリに書き換える

> http://projects.spring.io/spring-boot/

build.gradle に追加
```
dependencies {
    compile("org.springframework.boot:spring-boot-starter-web:1.4.1.RELEASE")
}
```

src/main/java/Hello.javaを書き換え

```java
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
@EnableAutoConfiguration
class Hello {

    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World 2!";
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Hello World.");
        SpringApplication.run(Hello.class, args);
    }
}
```

実行
```
gradle run
```

Webブラウザで確認
> http://localhost:8080/

## Spring Boot Gradle plugin を使う

Spring Boot Gradle plugin
> http://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-gradle-plugin.html

build.gradleを編集する

apply plugin を以下の２個にする
```
apply plugin: 'java'
apply plugin: "spring-boot"
```

buildScriptを追加する
```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:1.4.1.RELEASE")
    }
}
```

## S2I

S2Iビルダーイメージの作り方
How to Create an S2I Builder Image
> https://blog.openshift.com/create-s2i-builder-image/

S2Iの説明？
S2I Requirements
> https://docs.openshift.com/enterprise/3.2/creating_images/s2i.html

S2I Project Repository
> https://github.com/openshift/source-to-image

OpenShift v3 と source-to-image (s2i)
> http://qiita.com/nak3/items/6407c01cc2d1f153c0f1

## Spring Boot 用の S2Iビルダーイメージ

ここで公開されているビルダーイメージを使うとSpringBootのjarを組み込めそう
> https://github.com/jorgemoralespou/s2i-java

単にこのベースイメージを利用するだけなら以下のコマンドでイメージストリームに登録
```
oc create -f https://raw.githubusercontent.com/jorgemoralespou/s2i-java/master/ose3/s2i-java-imagestream.json
```

このベースイメージのビルドと、テンプレートを組み込むならこれ。
s2i-javaのBuildConfig,ImageStreamと、Maven, Gradle用のサンプルが登録される
```
oc create -f https://raw.githubusercontent.com/jorgemoralespou/s2i-java/master/ose3/s2i-java-build_in_ose3.json
```


### 旧バージョン
これをビルドするとSpringBoot + Gradle のビルダーイメージになる？
> https://github.com/jorgemoralespou/osev3-examples/tree/master/spring-boot/springboot-sti
よく見たらDepricatedって書いてあった


# Origin

### スタート on ec2

```
sudo -i
cd /home/ubuntu/origin/openshift-origin-server-v1.3.1
export PATH=$(pwd):$PATH
./openshift start --public-master=https://localhost:8443

export KUBECONFIG=`pwd`/openshift.local.config/master/admin.kubeconfig
export CURL_CA_BUNDLE=`pwd`/openshift.local.config/master/ca.crt
```

### レジストリ追加(たぶん何かやり方間違ってる)
```
$ oadm registry --credentials=./openshift.local.config/master/openshift-registry.kubeconfig --service-account=registry

$ echo \
    '{"kind":"ServiceAccount","apiVersion":"v1","metadata":{"name":"registry"}}' \
    | oc create -n default -f -

$ oc edit scc privileged
usersに以下を追加
 - system:serviceaccount:default:registry

$ oc volume deploymentconfigs/docker-registry --add --overwrite --name=registry-storage --mount-path=/registry --type=hostPath --path=/home/ubuntu/origin/registry
```

