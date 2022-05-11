# UML类图

#### UML基本介绍

- UML——Unified modeling language UML (统一建模语言)，是一种用于软件系统分析和设计的语言工具，它用于帮助软件开发人员进行思考和记录思路的结果。
- UML 本身是一套符号的规定，就像数学符号和化学符号一样，这些符号用于描述软件模型中的各个元素和他们之间的关系，比如类、接口、实现、泛化、依赖、组合、聚合等，如下图:

![image-20220124155328980](https://cdn.jsdelivr.net/gh/senluoye/BadGallery@main/image/202201241655517.png)

- 画类图的工具有StarUML等，网站有ioDraw等。

#### UML图

画 UML 图与写文章差不多，都是把自己的思想描述给别人看，关键在于思路和条理，UML 图分类：

- 用例图(use case)
- 静态结构图：类图、对象图、包图、组件图、部署图
- 动态行为图：交互图（时序图与协作图）、状态图、活动图

> 类图是描述类与类之间的关系的，是 UML 图中最核心的部分

#### UML类图

UML类图用于描述系统中的类(对象)本身的组成和类(对象)之间的各种静态关系。

> 类之间的关系：依赖、泛化（继承）、实现、关联、聚合与组合。

类图简单举例:

```java
class Person{
    private Integer id;
    private String name;
    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
}
```

把上述代码画成类图，可以得到如下结果：

![image-20220124172209803](https://cdn.jsdelivr.net/gh/senluoye/BadGallery@main/image/202201241722839.png)

顶头表示类名，第二部分表示属性，第三部分表示方法

> idea可以直接看到某一个类的类图，但功能有限

#### 依赖关系

如果在A类中用到了B类（成员属性、返回类型、方法参数、方法局域变量），那么可以称他们之间存在依赖关系。

```java
public class PersonServiceBean {
    public void save(Person person){}
    public void getIDCard(IDCard idCard){}
    public void modify(Department department){}
}
 class IDCard{}
 class Person{}
 class Department{}
```

例如上面的代码就存在依赖关系。UML类图如下图所示（虚线尖箭头）：

![](https://cdn.jsdelivr.net/gh/senluoye/BadGallery@main/image/202201251448228.png)

#### 泛化关系

如果A类继承了B类，我们就说A类和B类存在泛化关系。泛化关系实际上就是**继承**关系，他是**依赖关系的特例**。

下面的代码存在泛化关系：

```java
class IDCard extends Person{}
class Person{}
```

类图如下（实线三角箭头）：

![image-20220125145124970](https://cdn.jsdelivr.net/gh/senluoye/BadGallery@main/image/202201251451001.png)

#### 实现关系

如果A类实现了B接口，那我们就称它们为实现关系。实现关系也是依赖关系的特例。

例如下面的代码就存在实现关系：

```java
interface PersonService {
	void delete(Integer id);
}

public class PersonServiceBean implements PersonService {
 	public void delete(Integer id){}
}
```

类图如下（虚线三角箭头）：

![image-20220125145722940](https://cdn.jsdelivr.net/gh/senluoye/BadGallery@main/image/202201251457971.png)

#### 关联关系

关联关系实际上就是类与类之间的联系，也是依赖关系的特例。

需要注意的是：

- 关联具有导航性——即**双向关系**或**单向关系**。
- 关系具有多重性：如“1”（表示有且仅有一个），“0...”（表示0个或者多个）， “0，1”（表示0个或者一个），“n...m”(表示n到 m个都可以),“m...*”（表示至少m 个）。

下面的代码展示了关联关系中的单向一对一关系：

```java
public class PersonServiceBean{
	private IDCard idCard;
}

class IDCard {}
```

UML类图如下图所示（实线尖箭头）：

![image-20220125150308789](https://cdn.jsdelivr.net/gh/senluoye/BadGallery@main/image/202201251503823.png)

#### 聚合关系

聚合关系（Aggregation）表示的是**整体和部分**的关系，整体与部分可以分开。聚合关系是关联关系的特例，所以聚合具有关联的**导航性与多重性**。

之前的代码演示了类之间的聚合关系：

```java
public class PersonServiceBean{
	private IDCard idCard;
}

class IDCard {}
```

UML图跟关联一样（实线菱形空心箭头）：

![image-20220125150308789](https://cdn.jsdelivr.net/gh/senluoye/BadGallery@main/image/202201251506891.png)

#### 组合关系

组合关系：也是整体与部分的关系，但是整体与部分不可以分开。整体与部分的生命周期是一致的，一起生成或销毁。

下面的代码演示了类与类之间的组合关系：

```java
public class PersonServiceBean{
 Head head = new Head();
}

class Head {}
```

