# Gradleのインストール

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

# アプリ作成

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

実行する
```
gradle bootRun
```


# S2I

## S2Iに関する参考情報

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

<br>

## Spring Boot 用の S2Iビルダーイメージ その１ Gradle・Maven用

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

上記で作成したSpringBootアプリをビルドする例
```bash
$ oc login -u test
$ oc new-project test
$ oc create -f https://raw.githubusercontent.com/jorgemoralespou/s2i-java/master/ose3/s2i-java-imagestream.json
$ oc new-app s2i-java~https://github.com/kuresato/ms-sample2.git
$ oc logs -f bc/ms-sample2
$ oc get pods
$ oc get svc
$ curl http://$(oc get svc | grep ms-sample2 | awk '{print $2;}'):8080
```

### 旧バージョン
これをビルドするとSpringBoot + Gradle のビルダーイメージになる？
> https://github.com/jorgemoralespou/osev3-examples/tree/master/spring-boot/springboot-sti
よく見たらDepricatedって書いてあった

<br>

## Spring Boot 用の S2Iビルダーイメージ その２ Maven3用

Maven3のSpringBoot用のイメージ

* https://blog.codecentric.de/en/2016/03/deploy-spring-boot-applications-openshift
* https://github.com/codecentric/springboot-maven3-centos


# Origin環境メモ

## スタート on ec2

```
sudo -i
cd /home/ubuntu/origin/openshift-origin-server-v1.3.1
export PATH=$(pwd):$PATH
./openshift start --public-master=https://localhost:8443

export KUBECONFIG=`pwd`/openshift.local.config/master/admin.kubeconfig
export CURL_CA_BUNDLE=`pwd`/openshift.local.config/master/ca.crt
```

## レジストリ追加(たぶん何かやり方間違ってる)
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

## Origin Advanced Install メモ()

- https://docs.openshift.org/latest/install_config/install/prerequisites.html
- https://docs.openshift.org/latest/install_config/install/advanced_install.html#running-the-advanced-installation

* 基本はOSEと一緒
* EPELを使う
* Dockerはyumでdocker-1.10を入れる
* ansibleはGitHubから取得

### インストール手順

```bash
   13  yum install wget git net-tools bind-utils iptables-services bridge-utils bash-completion
   17  yum -y install https://dl.fedoraproject.org/pub/epel/7/x86_64/e/epel-release-7-8.noarch.rpm
   18  sed -i -e "s/^enabled=1/enabled=0/" /etc/yum.repos.d/epel.repo
   19  yum -y --enablerepo=epel install ansible pyOpenSSL
   20  git clone https://github.com/openshift/openshift-ansible
   29  yum install docker
   30  systemctl is-active docker
   31  systemctl enable docker
   32  systemctl start docker
   34  docker info
   36  ssh-keygen
   47  cat .ssh/id_rsa.pub >> .ssh/authorized_keys
   48  chmod 0600 .ssh/authorized_keys
   49  ssh openshift33
   55  cp -p /etc/ansible/hosts /etc/ansible/hosts.org
   56  vi /etc/ansible/hosts
   57  cat /etc/ansible/hosts
   58  ansible-playbook ~/openshift-ansible/playbooks/byo/config.yml
   60  oc get node
   61  oc get pods
   62  oc get events -w
   15  yum install httpd-tools
   17  htpasswd -c /etc/origin/master/htpasswd <ユーザ名>
   18  cat /etc/origin/master/htpasswd

```

### /etc/ansible/hosts 例

```
[root@openshift33 ~]# cat /etc/ansible/hosts
# Create an OSEv3 group that contains the masters and nodes groups
[OSEv3:children]
masters
nodes

# Set variables common for all OSEv3 hosts
[OSEv3:vars]
# SSH user, this user should allow ssh based auth without requiring a password
ansible_ssh_user=root

# If ansible_ssh_user is not root, ansible_become must be set to true
#ansible_become=true

deployment_type=origin

# uncomment the following to enable htpasswd authentication; defaults to DenyAllPasswordIdentityProvider
openshift_master_identity_providers=[{'name': 'htpasswd_auth', 'login': 'true', 'challenge': 'true', 'kind': 'HTPasswdPasswordIdentityProvider', 'filename': '/etc/origin/master/htpasswd'}]

openshift_master_cluster_method=native
openshift_master_cluster_hostname=openshift33
openshift_master_cluster_public_hostname=<外部アクセスFQDN>
openshift_master_default_subdomain=x.x.x.x.xip.io

# host group for masters
[masters]
openshift33

# host group for nodes, includes region info
[nodes]
openshift33 openshift_node_labels="{'region': 'infra', 'zone': 'default'}" openshift_schedulable=true
```
