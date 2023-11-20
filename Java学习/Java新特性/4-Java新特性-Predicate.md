# Java新特性-Predicate

[toc]

---

## 前言

有时候我们需要对某种类型的数据进行判断，从而得到一个`boolean`值结果。这时可以使用`java.util.function.Predicate`接口。



## 抽象方法：test

Predicate 接口中包含一个抽象方法: boolean test(T t) 。用于条件判断的场景：

```typescript
import java.util.function.Predicate;
 
public class Demo01Predicate {
    public static void main(String[] args) {
        method(s -> s.length() > 5);
    }
 
    private static void method(Predicate<String> predicate) {
        boolean veryLong = predicate.test("HelloWorld");
        System.out.println("字符串很长吗:" + veryLong);
    }
}
```

运行程序，控制台输出：

```bash
字符串很长吗:true
```

条件判断的标准是传入的Lambda表达式逻辑，只要字符串长度大于5则认为很长。



## 默认方法：and

既然是条件判断，就会存在与、或、非三种常见的逻辑关系。其中将两个 `Predicate` 条件使用“与”逻辑连接起来实 现“并且”的效果时，可以使用default方法 `and` 。其JDK源码为：

```scss
default Predicate<T> and(Predicate<? super T> other) {
    Objects.requireNonNull(other);
    return (t) -> test(t) && other.test(t);
}
```

如果要判断一个字符串既要包含大写“H”，又要包含大写“W”，那么：

```typescript
import java.util.function.Predicate;
 
public class DemoPredicateAnd {
    public static void main(String[] args) {
        boolean isValid = method(
                // String.contains()方法，仅当此字符串包含指定的字符值序列时返回true。
                s -> s.contains("H"),
                s -> s.contains("W")
        );
        System.out.println("字符串符合要求吗:" + isValid);
    }
 
    private static boolean method(Predicate<String> one, Predicate<String> two) {
        boolean isValid = one.and(two).test("Hello world");
        return isValid;
    }
}
```

运行程序，控制台输出：

```bash
字符串符合要求吗:false
```



##  默认方法：or

与 and 的“与”类似，默认方法 or 实现逻辑关系中的“或”。JDK源码为：

```javascript
default Predicate<T> or(Predicate<? super T> other) {
    Objects.requireNonNull(other);
    return (t) -> test(t) || other.test(t);
}
```

如果希望实现逻辑“字符串包含大写“H”或者包含大写“W”，那么代码只需要将“and”修改为“or”名称即可，其他都不变：

```typescript
import java.util.function.Predicate;
 
public class DemoPredicateOr {
    public static void main(String[] args) {
        boolean isValid = method(
                // String.contains()方法，仅当此字符串包含指定的字符值序列时返回true。
                s -> s.contains("H"),
                s -> s.contains("W")
        );
        System.out.println("字符串符合要求吗:" + isValid);
    }
 
    private static boolean method(Predicate<String> one, Predicate<String> two) {
        boolean isValid = one.or(two).test("Hello world");
        return isValid;
    }
}
```

运行程序，控制台输出：

```bash
字符串符合要求吗:true
```



## 默认方法：negate

“与”、“或”已经了解了，剩下的“非”(取反)也会简单。默认方法 negate 的JDK源代码为：

```scss
default Predicate<T> negate() {
    return (t) -> !test(t);
}
```

从实现中很容易看出，它是执行了test方法之后，对结果boolean值进行“!”取反而已。一定要在 test 方法调用之前

调用 negate 方法，正如 and 和 or 方法一样：

```typescript
import java.util.function.Predicate;
 
public class DemoPredicateNegate {
    public static void main(String[] args) {
        method(s -> s.length() > 5);
    }
 
    private static void method(Predicate<String> predicate) {
        boolean veryLong = predicate.negate().test("HelloWorld");
        System.out.println("字符串很长吗:" + veryLong);
    }
}
```

运行程序，控制台输出：

```bash
字符串很长吗:false
```