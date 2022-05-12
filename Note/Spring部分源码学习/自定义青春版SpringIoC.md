## 自定义 SpringIoC

现要对下面的配置文件进行解析，并自定义 Spring 框架的 IOC 对涉及到的对象进行管理。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
    <bean id="userService" class="com.itheima.service.impl.UserServiceImpl">
        <property name="userDao" ref="userDao"></property>
    </bean>
    <bean id="userDao" class="com.itheima.dao.impl.UserDaoImpl"></bean>
</beans>
```

### 1.1 定义 bean 相关的 pojo 类

#### 1.1.1 PropertyValue 类

用于封装 bean 的属性，在上面的配置文件中就是封装 bean 标签的子标签 property 标签的属性。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyValue {
  private String name;
  private String ref;
  private String value;
}
```

#### 1.1.2 MutablePropertyValues 类

一个 bean 标签可以有多个 property 子标签，所以再定义一个 MutablePropertyValues 类，用来存储并管理多个 PropertyValue 对象。

```java
public class MutablePropertyValues implements Iterable<PropertyValue> {

    /**
     * 定义list集合对象，用来存储PropertyValue对象
     */
    private final List<PropertyValue> propertyValueList;

    public MutablePropertyValues() {
        this.propertyValueList = new ArrayList<PropertyValue>();
    }

    public MutablePropertyValues(List<PropertyValue> propertyValueList) {
        if(propertyValueList == null) {
            this.propertyValueList = new ArrayList<PropertyValue>();
        } else {
            this.propertyValueList = propertyValueList;
        }
    }

    /**
     * 获取所有的PropertyValue对象，返回以数组的形式
     * @return PropertyValue[]
     */
    public PropertyValue[] getPropertyValues() {
        //将集合转换为数组并返回
        return propertyValueList.toArray(new PropertyValue[0]);
    }

    /**
     * 根据name属性值获取PropertyValue对象
     * @param propertyName 属性名称
     * @return PropertyValue
     */
    public PropertyValue getPropertyValue(String propertyName) {
        //遍历集合对象
        for (PropertyValue propertyValue : propertyValueList) {
            if (propertyValue.getName().equals(propertyName)) {
                return propertyValue;
            }
        }
        return null;
    }

    /**
     * 判断集合是否为空
     * @return boolean
     */
    public boolean isEmpty() {
        return propertyValueList.isEmpty();
    }

    /**
     * 添加PropertyValue对象
     * @param pv PropertyValue对象
     * @return MutablePropertyValues
     */
    public MutablePropertyValues addPropertyValue(PropertyValue pv) {
        //判断集合中存储的PropertyValue对象是否和传递进行的重复了，如果重复了，进行覆盖
        for (int i = 0; i < propertyValueList.size(); i++) {
            //获取集合中每一个PropertyValue对象
            PropertyValue currentPv = propertyValueList.get(i);
            if(currentPv.getName().equals(pv.getName())) {
                propertyValueList.set(i, pv);
                //返回this目的就是实现链式编程
                return this;
            }
        }
        this.propertyValueList.add(pv);
        //返回this目的就是实现链式编程
        return this;
    }

    /**
     * 判断是否有指定name属性值的对象
     * @param propertyName property名称
     * @return boolean
     */
    public boolean contains(String propertyName) {
        return getPropertyValue(propertyName) != null;
    }

    /**
     * 获取迭代器对象
     * @return Iterator
     */
    public Iterator<PropertyValue> iterator() {
        return propertyValueList.iterator();
    }
}
```

> 继承 Iterable 是为了使用迭代器

#### 1.1.3 BeanDefinition 类

BeanDefinition 类用来封装 bean 信息的，主要包含 id（即 bean 对象的名称）、class（需要交由 spring 管理的类的全类名）及子标签 property 数据。

```java
@Data
public class BeanDefinition {
    private String id;
    private String className;
    private MutablePropertyValues propertyValues;

    public BeanDefinition() {
        propertyValues = new MutablePropertyValues();
    }
}
```

### 1.2 定义注册表相关类

#### 1.2.1 BeanDefinitionRegistry 接口

BeanDefinitionRegistry 接口定义了注册表的相关操作，定义如下功能：

- 注册 BeanDefinition 对象到注册表中
- 从注册表中删除指定名称的 BeanDefinition 对象
- 根据名称从注册表中获取 BeanDefinition 对象
- 判断注册表中是否包含指定名称的 BeanDefinition 对象
- 获取注册表中 BeanDefinition 对象的个数
- 获取注册表中所有的 BeanDefinition 的名称

```java
public interface BeanDefinitionRegistry {
    /**
     * 注册BeanDefinition对象到注册表中
     * @param beanName Bean的名称
     * @param beanDefinition BeanDefinition对象
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    /**
     * 从注册表中删除指定名称的BeanDefinition对象
     * @param beanName Bean的名称
     * @throws Exception 抛出错误
     */
    void removeBeanDefinition(String beanName) throws Exception;

    /**
     * 根据名称从注册表中获取BeanDefinition对象
     * @param beanName Bean的名称
     * @return BeanDefinition
     * @throws Exception 抛出错误
     */
    BeanDefinition getBeanDefinition(String beanName) throws Exception;

    /**
     * 判断注册表中是否包含执行名称的BeanDefinition对象
     * @param beanName Bean的名称
     * @return boolean
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 获取注册表中BeanDefinition对象的个数
     * @return int
     */
    int getBeanDefinitionCount();

    /**
     * 获取注册表中所有BeanDefinition的名称
     * @return String
     */
    String[] getBeanDefinitionNames();
}
```

#### 1.2.2 SimpleBeanDefinitionRegistry 类

该类实现了 BeanDefinitionRegistry 接口，定义了 Map 集合作为注册表容器。

```java
public class SimpleBeanDefinitionRegistry implements BeanDefinitionRegistry {

    /**
     * 定义一个容器，用来存储BeanDefinition对象
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName,beanDefinition);
    }

    @Override
    public void removeBeanDefinition(String beanName) throws Exception {
        beanDefinitionMap.remove(beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws Exception {
        return beanDefinitionMap.get(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[0]);
    }
}
```

### 1.3 定义解析器相关类

这里只模拟实现针对 xml 格式的解析器相关类。

#### 1.3.1 BeanDefinitionReader 接口

BeanDefinitionReader 是用来解析配置文件并在注册表中注册 bean 的信息。定义了两个规范：

- 获取注册表的功能，让外界可以通过该对象获取注册表对象。
- 加载配置文件，并注册 bean 数据。

```java
public interface BeanDefinitionReader {
    /**
     * 获取注册表对象
     * @return BeanDefinitionRegistry
     */
    BeanDefinitionRegistry getRegistry();

    /**
     * 加载配置文件并在注册表中进行注册
     * @param configLocation 配置文件路径
     * @throws Exception
     */
    void loadBeanDefinitions(String configLocation) throws Exception;
}
```

#### 1.3.2 XmlBeanDefinitionReader 类

首先需要引入 dom4j 相关依赖：

```xml
<dependency>
    <groupId>dom4j</groupId>
    <artifactId>dom4j</artifactId>
    <version>1.6.1</version>
</dependency>
```

XmlBeanDefinitionReader 类是专门用来解析 xml 配置文件的。该类实现 BeanDefinitionReader 接口并实现接口中的两个功能。

```java
public class XmlBeanDefinitionReader implements BeanDefinitionReader {

    private BeanDefinitionRegistry registry;

    public XmlBeanDefinitionReader() {
        this.registry = new SimpleBeanDefinitionRegistry();
    }

    @Override
    public BeanDefinitionRegistry getRegistry() {
        return registry;
    }

    @Override
    public void loadBeanDefinitions(String configLocation) throws Exception {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocation);
        SAXReader reader = new SAXReader();
        Document document = reader.read(is);
        Element rootElement = document.getRootElement();
        //解析bean标签
        parseBean(rootElement);
    }

    private void parseBean(Element rootElement) {
        List<Element> elements = rootElement.elements();
        for (Element element : elements) {
            String id = element.attributeValue("id");
            String className = element.attributeValue("class");
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setId(id);
            beanDefinition.setClassName(className);
            List<Element> list = element.elements("property");
            MutablePropertyValues mutablePropertyValues = new MutablePropertyValues();
            for (Element element1 : list) {
                String name = element1.attributeValue("name");
                String ref = element1.attributeValue("ref");
                String value = element1.attributeValue("value");
                PropertyValue propertyValue = new PropertyValue(name,ref,value);
                mutablePropertyValues.addPropertyValue(propertyValue);
            }
            beanDefinition.setPropertyValues(mutablePropertyValues);

            registry.registerBeanDefinition(id, beanDefinition);
        }
    }
}
```

### 1.4 IOC 容器相关类

#### 1.4.1 BeanFactory 接口

在该接口中定义 IOC 容器的统一规范即获取 bean 对象。

```java
public interface BeanFactory {

    /**
     * 根据名称获取Bean对象
     * @param name Bean名称
     * @return Object
     * @throws Exception 抛出错误
     */
    Object getBean(String name) throws Exception;

    /**
     * 根据Bean的名称取对应的Bean对象，并进行类型转换
     * @param name Bean的名称
     * @param clazz Class对象
     * @param <T> 声明这是一个泛型方法
     * @return T
     * @throws Exception 抛出错误
     */
    <T> T getBean(String name, Class<? extends T> clazz) throws Exception;
}
```

#### 1.4.2 ApplicationContext 接口

该接口的所有的子实现类对 bean 对象的创建都是非延时的，所以在该接口中定义 `refresh()` 方法，该方法主要完成以下两个功能：

- 加载配置文件。
- 根据注册表中的 BeanDefinition 对象封装的数据进行 bean 对象的创建。

```java
public interface ApplicationContext extends BeanFactory {
    //进行配置文件加载并进行对象创建
    void refresh() throws IllegalStateException, Exception;
}
```

#### 1.4.3 AbstractApplicationContext 类

- 作为 ApplicationContext 接口的子类，所以该类也是非延时加载，所以需要在该类中定义一个 Map 集合，作为 bean 对象存储的容器。

- 声明 BeanDefinitionReader 类型的变量，用来进行 xml 配置文件的解析，符合单一职责原则。
  BeanDefinitionReader 类型的对象创建交由子类实现，因为只有子类明确到底创建 BeanDefinitionReader 哪儿个子实现类对象。

```java
public abstract class AbstractApplicationContext implements ApplicationContext {
    /**
     * 声明解析器变量
     */
    protected BeanDefinitionReader beanDefinitionReader;

    /**
     * 定义用于存储bean对象的map容器
     */
    protected Map<String, Object> singletonObjects = new HashMap<String, Object>();

    /**
     * 声明配置文件路径的变量
     */
    protected String configLocation;

    @Override
    public void refresh() throws Exception {
        //加载BeanDefinition对象
        beanDefinitionReader.loadBeanDefinitions(configLocation);

        //初始化bean
        this.finishBeanInitialization();
    }

    /**
     * bean的初始化
     * @throws Exception 抛出错误
     */
    private void finishBeanInitialization() throws Exception {
        //获取注册表对象
        BeanDefinitionRegistry registry = beanDefinitionReader.getRegistry();

        //获取BeanDefinition对象
        String[] beanNames = registry.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            //进行bean的初始化
            this.getBean(beanName);
        }
    }
}
```

> 注意：该类 finishBeanInitialization()方法中调用 getBean()方法使用到了模板方法模式。

#### 1.4.4 ClassPathXmlApplicationContext 类

该类主要是加载类路径下的配置文件，并进行 bean 对象的创建，主要完成以下功能：

- 在构造方法中，创建 BeanDefinitionReader 对象。
- 在构造方法中，调用 refresh()方法，用于进行配置文件加载、创建 bean 对象并存储到容器中。
- 重写父接口中的 getBean()方法，并实现依赖注入操作。

```java
public class ClassPathXmlApplicationContext extends AbstractApplicationContext {
    public ClassPathXmlApplicationContext(String configLocation) {
        this.configLocation = configLocation;
        //构建解析器对象
        beanDefinitionReader = new XmlBeanDefinitionReader();
        try{
            this.refresh();
        } catch (Exception ignored) {}
    }

    /**
     * 根据bean对象的名称获取bean对象
     * @param name Bean名称
     * @return
     * @throws Exception
     */
    @Override
    public Object getBean(String name) throws Exception {
        //判断对象容器中是否包含指定名称的bean对象，如果包含，直接返回即可，如果不包含，需要自行创建
        Object obj = singletonObjects.get(name);
        if (obj != null) {
            return obj;
        }

        //获取BeanDefinition对象
        BeanDefinitionRegistry registry = beanDefinitionReader.getRegistry();
        BeanDefinition beanDefinition = registry.getBeanDefinition(name);

        //获取bean信息中的className
        String className = beanDefinition.getClassName();

        //通过反射创建对象
        Class<?> clazz = Class.forName(className);
        Object beanObj = clazz.getDeclaredConstructor().newInstance();

        //进行依赖注入操作（bean可能依赖于其他bean）
        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        for (PropertyValue propertyValue : propertyValues) {
            //获取name属性值
            String propertyName = propertyValue.getName();

            //获取value属性
            String value = propertyValue.getValue();

            //获取ref属性
            String ref = propertyValue.getRef();
            if(ref != null && !"".equals(ref)) {
                //获取依赖的bean对象
                Object bean = getBean(ref);

                //拼接方法名
                String methodName = StringUtils.getSetterMethodByFieldName(propertyName);

                //获取所有的方法对象
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (methodName.equals(method.getName())) {
                        //执行该setter方法
                        method.invoke(beanObj,bean);
                    }
                }
            }

            if(value != null && !"".equals(value)) {
                //拼接方法名
                String methodName = StringUtils.getSetterMethodByFieldName(propertyName);

                //获取method对象
                Method method = clazz.getMethod(methodName, String.class);
                method.invoke(beanObj, value);
            }
        }

        //在返回beanObj对象之前，将该对象存储到map容器中
        singletonObjects.put(name, beanObj);
        return beanObj;
    }

    @Override
    public <T> T getBean(String name, Class<? extends T> clazz) throws Exception {
        Object bean = getBean(name);
        if(bean == null) {
            return null;
        }
        return clazz.cast(bean);
    }
}
```

#### 1.4.5 自定义工具类

主要是存储 ClassPathXmlApplicationContext 类中 getBean 方法用到的拼接方法名的工具方法

```java
public class StringUtils {
    private StringUtils() {
    }

    /**
     * userDao ==> setUserDao
     * @param fieldName
     * @return
     */
    public static String getSetterMethodByFieldName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
```

### 1.5 测试

首先通过 maven install 命令，将上面的代码打包。

![20220512202821](https://raw.githubusercontent.com/senluoye/BadGallery/master/image/20220512202821.png)

回到之前写的 spring_demo，更换 pom 文件的依赖：

```xml
<dependency>
    <groupId>com.itheima</groupId>
    <artifactId>itheima_spring</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

运行 Controller 的 main 方法，可以看到，已经成功运行了：

![20220512202936](https://raw.githubusercontent.com/senluoye/BadGallery/master/image/20220512202936.png)

### 1.6 自定义 Spring IOC 总结

#### 1.6.1 使用到的设计模式

- 工厂模式。这个使用工厂模式 + 配置文件的方式。
- 单例模式。Spring IOC 管理的 bean 对象都是单例的，此处的单例不是通过构造器进行单例的控制的，而是 spring 框架对每一个 bean 只创建了一个对象。
- 模板方法模式。AbstractApplicationContext 类中的 finishBeanInitialization()方法调用了子类的 getBean()方法，因为 getBean()的实现和环境息息相关。
- 迭代器模式。对于 MutablePropertyValues 类定义使用到了迭代器模式，因为此类存储并管理 PropertyValue 对象，也属于一个容器，所以给该容器提供一个遍历方式。

spring 框架其实使用到了很多设计模式，如 AOP 使用到了代理模式，选择 JDK 代理或者 CGLIB 代理使用到了策略模式，还有适配器模式，装饰者模式，观察者模式等。

#### 1.6.2 符合大部分设计原则

#### 1.6.3 整个设计和 Spring 的设计还是有一定的出入

spring 框架底层是很复杂的，进行了很深入的封装，并对外提供了很好的扩展性。而我们自定义 SpringIOC 有以下几个目的：

- 了解 Spring 底层对对象的大体管理机制。
- 了解设计模式在具体的开发中的使用。
- 以后学习 spring 源码，通过该案例的实现，可以降低 spring 学习的入门成本。
