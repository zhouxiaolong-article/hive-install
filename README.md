# HIVE的安装及使用
@[周小龙]

> hive其实就是一个客户端工具，根据sql语义转化为相应的mapreduce,跑在hdfs集群上面，返回结果在返回给hive,这篇操作手册是在HDFS集群搭建成功的基础上而写，所有前提是你已经有HDFS集群,此文不介绍高大上的架构，只介绍怎么使用。

[http://archive.apache.org/](http://archive.apache.org/).在这里可以找到apache`所有应用`的历史版本，因为我的HDFS集群是在搭建在jdk6的版本上面，我下载的是hive-0.12.0

## HIVE能做什么
**can do**:数据挖掘相关的，实际应用如日志分析、统计等，是不是说了跟没说是一样的? 因为牵扯到业务就说得太专业了，从一段日志中找出某一条日志你懂，往大了说这个叫数据挖掘了，你就不懂了。   
简单举例你有一个text.log格式如下

| id  | username  | password| reg time|
|:----|:-----:|---:|------:|
| 1 | zhouxiaolong |123456|2015-8-12|
| 2 | laiwei       | xxxafadf |2014-5-11|
| 3 | god          | tttttt |2013-2-22|
| .. |.....          | .... |.....|

**问题**：

* 能统计出来所有password为123456的帐户个数吗？
* 可以查找到注册时间在2015年前的个数吗？
* ..........................................................................

**回答**:   

* 数据直接写入`mysql`，select count(1) from table where password='123456' 分分钟搞定以上需求   
* 数据直接写入`mysql`，select count(1) from table where regtime>'2015' 秒秒钟搞定以上需求   

**反问**：  
>text.log 如果1个G呢？或10个G呢？或100G呢？或 TB你要怎么才能解决以上需求呢？

HIVE就可以解决数据源量大，利用sql语法，解决用户需求，用法和MYSQL部份相同

* 数据直接写入`hdfs`，使用hive运行select count(1) from table where password='123456' 分分钟搞定以上需求   
* 数据直接写入`hdfs`，使用hive运行select count(1) from table where regtime>'2015' 秒秒钟搞定以上需求   



## 第一部份配置HIVE

hive-0.12.0解压目录下，只需要编辑二个文件`hive-default.xml.template`和`hive-env.sh.template`   
cp hive-default.xml.template hive-site.xml   
cp hive-env.sh.template hive-env.sh



>配置hive之前，先安装mysql数据库，hive创建表，需要存储meta信息，1、默认是放derby数据库中，2、可以安装在mysql数据库中

yum install mysql-server   
yum install mysql-devel   
service mysqld start  
登陆mysql并创建用户hive，密码hivetest，创建数据库hive,并赋予hive数据库的权限   


hive-site.xml     copy`下以数据`到configuration节点下
   
```
<property>
  <name>javax.jdo.option.ConnectionURL</name>
  <value>jdbc:mysql://localhost:3306/hive?createDatabaseIfNotExist=true</value>
</property>

<property>
  <name>hadoop.tmp.dir</name>
    <value>/hive/tmp</value>
 </property>

<property>
  <name>javax.jdo.option.ConnectionDriverName</name>
  <value>com.mysql.jdbc.Driver</value>
</property>

<property>
  <name>javax.jdo.option.ConnectionUserName</name>
  <value>hive</value>
</property>

<property>
  <name>javax.jdo.option.ConnectionPassword</name>
  <value>hivetest</value>
</property>

<property>
  <name>hive.exec.mode.local.auto</name>
  <value>false</value>
</property>

<property>
  <name>hive.exec.scratchdir</name>
  <value>/user/zhouxiaolong/new-hive</value>
</property>

<property>
  <name>hive.metastore.warehouse.dir</name>
  <value>/user/zhouxiaolong/hive</value>
</property>

<property>
  <name>hive.hwi.result</name>
  <value>/user/zhouxiaolong/hwi/hwi_result</value>
</property>

```

hive-env.sh只修改一行，删除其注释

```
HADOOP_HOME=${bin}/../../hadoop
```
## 第二部份hdfs包
* 随便找一个目录mkdir hive   
* hdfs客户端包解压到hive目录
* 并把hive-0.12.0解压到hive目录  
* 并创建软链hadoop   ln -s  hdfs客户端目录名  hadoop  

![tree](https://raw.githubusercontent.com/zxl200406/pic/master/pic/hive-directory-tree.png)
 
>hive目录下，只有2个目录，和增加的一个软链



## 第三部份hive的使用

hive其实有三部份，hive-cli,hiveserver和hwi   
1. hive-cli：`命令行工具`(例如mysql的客户端操作mysql）  
2. hiveserver: 提供一个thrift接口，方便程序去调用，得到返回结果(应用程序直接连结hive接口)    
3. hwi:是一个web接口（如：phpmyadmin去操作mysql）

>1,2,3从功能上来说都是一样的，就是提供不一样的使用方式

### hive对于压缩算法的支持
| 压缩格式  | 工具   | 算法 | 文件扩展名|多文件|可分割性|
|:--------:|:-----:|----:|--------:|--------:|--------:|
|default|无| default |.deflate|不|不|
|gzip|gzip| default |.gz|不|不|
|zip|zip| default |.zip|是|是|
|bzip2|bzip2| bzip2 |.bz2|不|是|
|lzo|lzop| lzo |.lzo|不|是|

### hive客户端使用

比如已经存在数据text.txt格式如下,表格是为了好看，其实数据不是空格分割，是以`单引号`

| id  | name  | age| gender|
|:----|:-----:|---:|------:|
| 1 | zhouxiaolong |23|male|
| 2 | laiwei       |40|male|
| 3 | god          |100|male|

./bin/haddoop dfs -put ../text.txt /zhouxiaolong
> text.txt在本地，/zhouxiaolong是hdfs上的目录，也就是把本地text.txt目录上传到hdfs://zhouxiaolong/text.txt

然后运行hive即可 

1. create databases sadev;
2. create external table user (id int,name string,age int,gender string) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' stored as TEXTFILE location '/zhouxiaolong';
3. select * from user;

![](https://raw.githubusercontent.com/zxl200406/pic/master/pic/hadoop-hive-select.png)
>运行hive创建表可能会报错，如果是libthrift版本原因，可能是libthrift-0.9.0.jar和libthrift-0.8.0.jar冲突，直接在hadoop目录下，查找所有的libthrift-0.8.0.jar并删除，cp libthrift-0.9.0.jar 到删除的目录下即可



## 总结
最终的效果就是hive目录下，有2个子目录，1个是hadoop目录，提供访问读写hdfs的操作，第2个是hive-0.12.0提供查询的操作,最终打一个tar包，解压把任何linux都可以运行，（你问为什么？copy到任意机器都可以运行？在次比喻告诉你hdfs就像一个httpserver，hive就像浏览器）






#HIVESERVER

>hive-cli很实用,但是不能覆盖所有的应用场景，比方你的应用程序想去调用hive,并取得返回结果。cli端就不能做这个事情了，HIVESERVER提供的是一个thrift接口

hive就是一个客户端，但是集成了HIVESERVER的功能，每个客户端都可以启动HIVESERVER

hive --service hiveserver2

hive-site.xml中配置的是80端口

```
<property>
  <name>hive.server2.thrift.port</name>
  <value>80</value>
</property>

```

* hive --service hiveserver2 //启动成功，就会开放80端口
* ./bin/beeline 
* !connect jdbc:hive2://10.108.97.222:80/sadev   //这里的ip就是本地IP, sadev是数据库,HIVE中use sadev?还记得吗？
* 输入用户名和密码，应用连接上来

以上步骤看图即可,左边是操作beeline 这里的意思就是创建JDBC连接，方便程序连接,后边是启动hiveserver

![1](https://raw.githubusercontent.com/zxl200406/pic/master/pic/hivebeeline-create-jdbc.png)

这些步骤完成以后，就可以写代码，直接连接hive thrift接口了
![2](https://raw.githubusercontent.com/zxl200406/pic/master/pic/hiveserver-javaclientvisit.png)

最后放一张maven打包后和程序，可以完美通信了
![](https://raw.githubusercontent.com/zxl200406/pic/master/pic/hiveclient-maven.png)


