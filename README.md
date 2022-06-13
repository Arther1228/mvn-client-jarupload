# maoko.jarupload.mvnClient
使用mvn命令搭建的批量上传jar包到私有库客户端  
# 环境依赖
* java安装且配置环境变量  
* maven客户端安装且配置环境变量  
# 实现以下功能
1.配置简单，指定扫描文件目录位置，上传服务器配置  
2.自动生成mvn的settings.xml文件  
3.多线程上传，加快上传速度  
4.支持nexus3.x版本  


## 项目介绍
### 一、用途
##### 1、批量上传jar包，原理是扫描指定文件夹中的jar包，然后调用cmd执行 mvn命令
##### 2、目前仅支持包含完整jar包和pom文件的文件夹上传，单独jar文件上传暂不支持

### 二、环境依赖
##### 1、java安装且配置环境变量
##### 2、maven客户端安装且配置环境变量

##### 三、配置文件介绍
文件名及路径    | 介绍        
---------|--------------
conf/app.properties     | 库相关设置
conf/log4j2.xml     | 日志配置文件
conf/mvnSeetings.xml     | 第三方库配置（C盘目录下必须也要有这个设置）

#### 四、运行
##### 1、本地安装maven的setting.xml中 servers节点需要配置maven仓库的用户名和密码
```
  <servers>
   <server>
      <id>thirdparty</id>
      <username>admin</username>
      <password>softabc.123</password>
   </server>
  </servers>
``` 
##### 2、设置conf/app.properties中maven.repository.local上传文件夹路径，并在本地新建对应文件夹
##### 3、拷贝文件夹到maven.repository.local对应的文件夹中
##### 4、启动服务