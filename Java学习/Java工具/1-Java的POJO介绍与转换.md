
# Java的POJO介绍与转换方法

[toc]

---

## 1 POJO介绍

### 1.1 POJO是什么

 POJO（Plain Old Java Object/Plain OrdinaryJava Object）是指**普通的Java对象**，它是一个简单的Java类，通常没有实现特定的接口或继承特定的类。POJO对象的设计原则是**简单、普通、纯粹**的Java对象，不依赖于特定的框架或技术。当然,如果有一个简单的运算属性也是可以的,但不允许有业务方法,也不能携带有connection之类的方法。

创建一个基本的用户POJO，它有三个属性：名字、姓氏和年龄

```java
public class BaseUserPojo { 
   

    public String firstName;
    public String lastName;
    private int age;

    public BaseUserPojo(String firstName, String lastName, int age) { 
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public String name() { 
        return this.firstName + " " + this.lastName;
    }

    public int getAge() { 
        return this.age;
    }
}
```

这个类可以被任何Java程序使用，因为它不绑定到任何框架。但是，对于POJO来说，我们**==没有遵循任何真正的约定==**来构造、访问或修改类的状态。

### 1.2 JavaBean介绍

JavaBean**仍然是一个POJO**，用另一句话说就是JavaBean是POJO的一个子集，但围绕如何实现一个JavaBean引入了一组**==严格的规则和约定==**：

- 访问级别—要求属性是私有的，并暴露公开getter和setter方法。
- 方法名–getter和setter遵循getX和setX约定（对于布尔值，isX可以用于getter）
- 默认构造函数–必须存在无参数构造函数，以便在不提供参数的情况下创建实例，例如在反序列化期间（实现`Serializable`接口）

将上面提到的POJO转为一个JavaBean如下：

```java
public class BaseUserBean implements Serializable { 
  	private static final long serialVersionUID = 123456789L;
    private String firstName;
    private String lastName;
    private int age;
  
  	public BaseUserBean() { 
    }

    public BaseUserPojo(String firstName, String lastName, int age) { 
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    // Getters/Setters方法
}
```

>在我们实际项目我一般都是利用`org.projectlombok:lombok`依赖中的`@Data\@Getter\@Getter`等相关注解在**编译过程**中自动生成相关方法

> **扩展：对于Java序列化为什么要实现`Serializable`接口？**
>
> 序列化是将对象状态转换为可保持或传输的格式的过程。与序列化相对的是反序列化，它将流转换为对象。这两个过程结合起来，可以轻松地存储和传输数据。
>
> 对于`Serializable`接口我们点进去发现什么也没有是一个空接口，我们可以将它理解成一个标识接口。在Java中的这个`Serializable`接口其实是给jvm看的，通知jvm，我不对这个类做序列化了，你(jvm)帮我序列化就好了~
>
> 对于Java序列化详细内容这里先挖个坑🕳️～后面开坑做详细介绍

## 2 搞懂VO、DTO、BO、DO、PO

随着项目复杂度越来越高，编程模型越来越多，从简单的POJO衍生出了对于不同业务层有不同的模型定义，比如VO、DTO、BO、DO、PO等等，我们先从数据流转和业务层面来看看各个对象的定义：

<img src="./1-Java%E7%9A%84POJO%E4%BB%8B%E7%BB%8D%E4%B8%8E%E8%BD%AC%E6%8D%A2.assets/F1A24268-4AB9-4B06-8BAF-77F86072B0B3_1_201_a.jpeg" alt="F1A24268-4AB9-4B06-8BAF-77F86072B0B3_1_201_a" style="zoom: 50%;" />

### 2.1 VO（Value Object）

VO就是展示用的数据，不管展示方式是网页，还是客户端，还是APP，只要是这个东西是让人看到的，这就叫VO VO主要的存在形式就是js里面的对象（也可以简单理解成json）

> 有的地方也这样写：VO（`View Object`）：**视图对象**，用于展示层，它的作用是把某个指定页面（或组件）的所有数据封装起来。 这个问题，有些资料是这样解释： 尽管 "View Object" 也是一个有效的解释，但在**面向对象设计和领域驱动设计（DDD）的上下文中**，VO 通常指的是 "Value Object"。选择哪种解释取决于具体的上下文和使用场景。
>
> 注：在展示业务不复杂的系统，可直接使用DTO

### 2.2 DTO（Data Transfer Object）

数据传输对象，DTO用于在不同层之间传输数据，它通常用于将业务逻辑层（Service层）的数据传输给表示层（Presentation层）或持久化层（Persistence层）。DTO对象通常包含业务领域的数据，但不包含业务逻辑。

> 这个传输通常指的前后端之间的传输，DTO本身的一个隐含的意义是要能够完整的表达一个业务模块
> 的输出。如果服务和服务之间相对独立，那就可以叫DTO；如果服务和服务之间不独立，每个都不是一个完整的
> 业务模块，拆开可能仅仅是因为计算复杂度或者性能的问题，那这就不能够叫做DTO，只能是BO。

### 2.3 PO（Persistant Object）

持久对象，从名字也可以知道PO是数据库中的记录，一个PO的数据结构对应着库中表的结构，表中的一条记录就是一个PO对象通常PO里面除了get，set之外没有别的方法。对于PO来说，数量是相对固定的，一定不会超过数据库表的数量，等同于常说的`Entity`

> PO对象通常用于ORM（对象关系映射）框架中，如Hibernate、MyBatis等。
>
> `Entity`: 指的是java实体，在软件开发中，实体通常用于表示现实世界中的具体对象或概念，如用户、订单、产品等。在JPA（Java Persistence API）或ORM（对象关系映射）框架中，实体通常指的是**与数据库表相对应的Java对象**，用于表示数据库中的一条记录。实体对象通常包含与数据库表字段对应的属性，以及对应的getter和setter方法。

### 2.4 BO（Business Object）

BO是一个业务对象，一类业务就会对应一个BO，数量上没有限制。而且BO会有很多业务操作，也就是说除了PO对象的get，set方法以外，BO会有很多针对自身数据进行计算的方法。

现在很多持久层框架自身就提供了数据组合的功能，因此BO有可能是在业务层由业务来拼装PO而成，也有可能是在数据库访问层由框架直接生成。

**很多情况下为了追求查询的效率，框架跳过PO直接生成BO的情况非常普遍，PO只是用来增删改使用。**

> 注：以上图为例，`PO1`是交易记录，`PO2`是登录记录，`PO3`是商品浏览记录，`PO4`是添加购物车记录，`PO5`是搜索记录，`BO`是个人网站行为对象
>
> 一些工具类的系统和一些业务不是很复杂的**系统DTO是可以和BO合并成一个**，当业务扩展的时候再进行拆分


### 2.5 DO（Domain/Data Object）

领域对象，通常用于表示业务领域中的实体或业务对象。DO对象通常包含了业务逻辑和数据，是业务逻辑的实体表示。在某些情况下，DO对象可能与PO对象相似，但它们的用途和含义不同。DO对象通常用于表示业务领域中的复杂业务逻辑和业务实体。

> DO 现在主要有两个版本：
>
> 1. 阿里巴巴的开发手册中的定义，DO（ Data Object）这个**等同于上面的PO**
> 2. DDD（Domain Design）领域驱动设计中，DO（Domain Object）这个等**同于上面的BO**

这些对象在Java开发中常用于不同的层次和场景，有助于将数据、业务逻辑和持久化操作进行有效地分离和组织。在实际应用中，根据具体的需求和设计理念，可以选择合适的对象模型来实现业务功能。

> **实际工程中是否==必须==按照固定的概念来定义对象？**
>
> 答案当然是否定的。各个系统复杂情况不同，概念只是一个辅助，没有必要做照搬概念的教条主义。设计出一个灵活易懂的系统才是我们的最终目标。

## 3 JavaBean间的转换

前面提到了VO、DTO、BO、PO等概念，在实际工程中对于一次请求可能涉及到多个对象转换过程，如果对于每一次转换都手动去维护一个转换方法工作量是非常大的，而且在后续业务拓展时，如果我们有增加字段的需求，对于每一层的转换方法都要去维护变更，这是一件很麻烦的事情，所以下面推荐一些可以实现自动转换的方法，这里我们先定义一个用户的DTO对象和PO对象

- UserDTO：

```java
@Data
public class UserDTO implements Serializable {
  private long id;
  private String userName;
  private String shortName;
  private int age;
}
```

- UserPO:

```java
@Data
public class UserPO implements Serializable {
  private long id;
  private String userName;
  private int age;
}
```

### 3.1 Apache Commons BeanUtils

**Apache Commons BeanUtils** 提供了 `PropertyUtils.copyProperties()` 和`BeanUtils.copyProperties()`方法来实现对象之间属性的拷贝。

以 `PropertyUtils.copyProperties()` 使用方法为例：

```java
// 方法封装
public static void copyProperties(Object dest, Object orig) {
  try {
    // 入参1为目标转换对象
    PropertyUtils.copyProperties(dest, orig);
  } catch (Exception e）
  	throw e;
}
```

> 特点：
>
> - `PropertyUtils`支持为`null`的场景；
>
> 缺点：
>
> 1. 它的性能问题相当差；
> 2. `PropertyUtils`有自动类型转换功能，而`java.util.Date`恰恰是其不支持的类型；

### 3.2 Spring的BeanUtils

对于一个Spring项目来说，这是一种常用方法，封装示例如下：

```java
// 方法封装
public static void copyProperties(Object orig, Object dest) {
  try {
    // 入参2为目标转换对象
    PropertyUtils.copyProperties(orig, dest);
  } catch (Exception e）
  	throw e;
}
```

> 特点：
>
> - 速度相对快一些；
> - **第一个参数是源bean,第二个参数是目标bean**，与上面的相反；
>
> 缺点：
>
> 1. `BeanUtils`没有自动转换功能，遇到参数名相同，类型不同的参数不会进行赋值；
> 2. `BeanUtils`对部分属性不支持null的情况，Ineger、Boolean、Long等不支持；

### 3.3 Json序列化转换

这种方法就是将java对象转换为json，然后将JSON转换成Java对象。这里以Spring默认的序列化框架`jackson`为例：

```java
public class JsonConvertExample {
  
	private static final ObjectMapper MAPPER = new ObjectMapper();
  
  public static void main(String[] args) {
    UserPO userPO = new UserPO();
    userPO.setId(1L);
    userPO.setUserName("ABigTree");
    userPO.setAge(22);
    String json = MAPPER.writeValueAsString(userPO);
    UserDTO userDTO = MAPPER.readValue(json, UserDTO.class);
  }
}
```

> 特点：
>
> - 属性名需保持一致，或通过配置Json序列化的相关设置来满足相关需求
> - 多了序列化与反序列化过程，性能大打折扣；
>
> 缺点：
>
> - 多此一举，全是缺点😂

### 3.4 MapStruct

现在要介绍一个性能爆炸、高级优雅、大厂标准的转换方法，MapStruct框架，要使用MapStruct需要先在`pom.xml`中添加依赖：

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>
```

如果要使用 MapStruct 库进行对象之间的映射，首先需要定义一个 Mapper 接口，并在接口中编写映射方法。然后，MapStruct 库会自动生成对应的映射实现类，接口定义如下：

```java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "shortname", target = "userName")
    UserPO toUserPO(UserDTO dto);
}
```

除了同名字段自动转换的功能，MapStruct还支持`@Mapping`等注解去配置转换过程中细节实现。

> 对于MapStruct为什么性能爆炸和MapStruct的进阶用法，这里先挖个坑，后面去开坑详细介绍～

