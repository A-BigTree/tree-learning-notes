# Java新特性-Functon接口

[toc]

---

## Function简介

为了更好的将函数作为参数，Java遂引入了Function接口。

```java
@FunctionalInterface
public interface Function<T, R> {
  R apply(T t);
  
  default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
    Objects.requireNonNull(before);
    return (V v) -> apply(before.apply(v));
  }
  
  default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (T t) -> after.apply(apply(t));
  }
}
```

- 有一个抽象方法，暗示可以使用$\lambda$表达式；
- 该接口会接收一个参数，且会产生结果；
- 在使用这个接口前**需要明确定义参数类型和返回的结果类型**（Java的泛型就是这么回事）；
- **里面有一个`apply`方法将会对参数进行操作，并返回结果**；
- 因为是函数式接口（`@FunctionalInterface`），可以通过lambda表达式实现该接口；

## 默认方法

<img src="./Java新特性-Function.assets/截屏2023-06-27 20.45.59.png" alt="截屏2023-06-27 20.45.59" style="zoom:50%;" />

- 两个方法都使用了两次`apply`，只是`this.apply`执行的先后不同；
- 说明两个`Function`可以组合起来用 **=>** 两个`apply`可以组合使用 **=>** 可以用两个Lambda表达式实现`apply`；

### 测试

```java
public class FunctionTest {
    public static void main(String[] args) {
        FunctionTest test = new FunctionTest();
        System.out.println("-----------------------compose()");
        System.out.println(test.operate1(2,integer -> integer*3,integer -> integer*integer));//12
        System.out.println("-----------------------andThen()");
        System.out.println(test.operate2(2,integer -> integer*3,integer -> integer*integer));//36
    }
    public int operate1(int i,Function<Integer, Integer> function1 ,Function<Integer, Integer> function2){
        return function1.compose(function2).apply(i);//当前对象（this）为function1
    }
    public int operate2(int i,Function<Integer, Integer> function1 ,Function<Integer, Integer> function2){
        return function1.andThen(function2).apply(i);//当前对象（this）为function1
    }
}
```

### 总结

- **总之，要分清当前function，也就是当前对象，判断执行顺序**。对象有方法和成员变量；
- 本例中，function1都是当前对象，调用它的方法，以及将function2作为参数传给它；
- **function是对象，只是有函数的功能而已。面向对象不能丢**；



## BiFunction使用

<img src="./Java新特性-Function.assets/截屏2023-06-27 20.51.39.png" alt="截屏2023-06-27 20.51.39" style="zoom:50%;" />

- `BiFunction`接收两个参数返回一个结果；

- 它有一个抽象方法`apply`，接收两个参数；

- 它有一个`andThen`的默认方法和`Function`比起来多了一个参数，少了一个`compose`方法；

  - 因为`Function`只需要一个参数，`BiFunction`刚好可以返回一个参数，可以先`BiFuction`再`Function`，所以有`andThen`方法；

  - `BiFunction`是当前对象，如果它后执行，其他`Function`先执行，且它们都只会返回一个结果，就会导致`BiFunction`只有一个参数，满足不了BiFunction的需要两个参数的需求，所以没有`compose`方法；

## 作用

- 当需要对数据进行操作时且你想用lambda表达式时，就可以用它，而不需要自己去写一个满足函数式接口的接口；
- 而且对一个或两个数据的修改和操作是不同的，比如两个数的加减乘除，就不需要定义四个方法，只要Bifunction接口，用不同的Lambda表达式实现就好了；
- 某个操作只用一次用Lamdba表达式即可，就不需要单独封装成方法了；

