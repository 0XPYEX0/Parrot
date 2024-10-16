package me.xpyex.plugin.parrot.mirai.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import me.xpyex.plugin.parrot.mirai.api.TryCallable;
import me.xpyex.plugin.parrot.mirai.api.TryRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ValueUtil {
    /**
     * 当值非null时执行方法体，类似Optional
     *
     * @param obj       要检查的对象
     * @param ifPresent 若obj非null，执行此方法体
     */
    public static <T> void ifPresent(T obj, Consumer<T> ifPresent) {
        notNull("待执行的方法体不应为null", ifPresent);
        optional(obj, ifPresent, null);
    }

    /**
     * 根据值是否为null执行不同的方法体，类似Optional
     *
     * @param obj       要检查的对象
     * @param ifPresent 若obj非null，执行此方法体
     * @param ifNull    若obj为null，执行此方法体
     */
    public static <T> void optional(T obj, Consumer<T> ifPresent, Runnable ifNull) {
        if (obj != null) {
            if (ifPresent == null) {
                return;
            }
            try {
                ifPresent.accept(obj);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (ifNull != null) {
            try {
                ifNull.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 当值为null时执行方法体，类似Optional
     *
     * @param obj    要检查的对象
     * @param ifNull 若obj为null，执行此方法体
     */
    public static void ifNull(Object obj, Runnable ifNull) {
        notNull("参数不应为null", ifNull);
        optional(obj, null, ifNull);
    }

    /**
     * 安全获取值，类似Optional
     *
     * @param value     需要判定的值.
     * @param defaulted 若value为null，则返回此实例
     * @return 返回安全的值
     */
    @NotNull
    public static <T> T getOrDefault(T value, T defaulted) {
        notNull("待执行的方法体不应为null", defaulted);
        return value != null ? value : defaulted;
    }

    /**
     * 安全获取值，类似Optional
     *
     * @param callable  执行返回值的方法.
     * @param defaulted 如callable的返回值为null，或过程中出现错误，则返回此
     * @param errMsg    描述错误的信息
     * @return 返回安全的值，不会出现空指针
     */
    @NotNull
    public static <T> T getOrDefault(TryCallable<T> callable, T defaulted, String errMsg) {
        notNull("Default参数不应为空", defaulted);
        if (callable == null) {
            return defaulted;
        }
        try {
            return getOrDefault(callable.call(), defaulted);
        } catch (Throwable e) {
            new Throwable(errMsg, e).printStackTrace();
            return defaulted;
        }
    }

    /**
     * 安全获取值，类似Optional
     *
     * @param callable  执行返回值的方法.
     * @param defaulted 如callable的返回值为null，或过程中出现错误，则返回此
     * @return 返回安全的值，不会出现空指针
     */
    @NotNull
    public static <T> T getOrDefault(TryCallable<T> callable, T defaulted) {
        return getOrDefault(callable, defaulted, "在执行XPLib的 ValueUtil.getOrDefault(Callable, Object) 方法时，callable过程出现错误: ");
        //
    }

    /**
     * 检查Callable的返回值是否存在null，是安全的方法
     *
     * @param callables 某些会返回实例的方法体
     * @return 其中是否出现null
     */
    public static boolean isNull(Callable<?>... callables) {
        if (callables.length == 0) {
            return true;
        }
        for (Callable<?> callable : callables) {
            try {
                if (callable.call() == null) {
                    return true;
                }
            } catch (Throwable ignored) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查Callable的返回值是否存在空，是安全的方法
     *
     * @param callables 某些会返回实例的方法体
     * @return 其中是否出现空
     */
    public static boolean isEmpty(Callable<?>... callables) {
        if (callables.length == 0) {
            return true;
        }
        for (Callable<?> callable : callables) {
            try {
                if (isEmpty(callable.call())) {
                    return true;
                }
            } catch (Throwable ignored) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查传入的值是否存在null
     *
     * @param objects 要检查的实例
     * @return 是否存在null
     */
    public static boolean isNull(Object... objects) {
        if (objects == null || objects.length == 0) {
            return true;
        }
        for (Object o : objects) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public static void checkNull(String errMsg, Object... objects) {
        notNull(errMsg, objects);
        //
    }

    /**
     * 检查传入的值是否存在null
     *
     * @param msg     若存在null，抛出IllegalArgumentException，此为描述信息
     * @param objects 要检查的实例
     */
    public static void notNull(String msg, Object... objects) {
        if (isNull(objects))
            throw new IllegalArgumentException(msg);
    }

    /**
     * 检查传入的值是否存在空
     *
     * @param objects 要检查的实例
     * @return 是否存在空
     */
    public static boolean isEmpty(Object... objects) {
        if (objects.length == 0) {
            return true;
        }
        for (Object o : objects) {
            if (o == null) {
                return true;
            }
            if (o instanceof String) {
                if (((String) o).isEmpty()) return true;
            }
            try {
                if (Array.getLength(o) == 0) return true;  //如果是Object[]，且没有内容
            } catch (IllegalArgumentException ignored) {
            }
            if (o instanceof Map) {
                if (((Map<?, ?>) o).isEmpty()) return true;
            }
            if (o instanceof Collection) {
                if (((Collection<?>) o).isEmpty()) return true;
            }
            if (o instanceof Iterable) {
                if (!((Iterable<?>) o).iterator().hasNext()) return true;
            }
            if (o instanceof Iterator) {
                if (!((Iterator<?>) o).hasNext()) return true;
            }
        }
        return false;
    }

    @Deprecated
    public static void checkTrue(String errMsg, boolean... results) {
        mustTrue(errMsg, results);
        //
    }

    /**
     * 若条件为false，则抛出IllegalStateException
     * 若传入多个条件，且其中之一为false，则抛出IllegalStateException
     *
     * @param errMsg  错误信息
     * @param results 条件
     */
    public static void mustTrue(String errMsg, boolean... results) {
        for (boolean result : results)
            if (!result)
                throw new IllegalStateException(errMsg);
    }

    @SafeVarargs
    public static void mustTrue(String errMsg, TryCallable<Boolean>... results) {
        for (TryCallable<Boolean> result : results) {
            try {
                if (!result.call()) {
                    throw new IllegalStateException(errMsg);
                }
            } catch (Throwable e) {
                throw new IllegalStateException(errMsg, e);
            }
        }
    }

    @Deprecated
    public static void checkEmpty(String errMsg, Object... objects) {
        notEmpty(errMsg, objects);
        //
    }

    /**
     * 检查传入的值是否存在空值
     *
     * @param errMsg  若存在空值(不止null)，抛出IllegalArgumentException，此为描述信息
     * @param objects 要检查的实例
     */
    public static void notEmpty(String errMsg, Object... objects) {
        if (isEmpty(objects))
            throw new IllegalArgumentException(errMsg);
    }

    @SafeVarargs
    public static <T> boolean equalsOr(T target, T... values) {
        notNull("值不应为null", target, values);
        for (T value : values) {
            if (Objects.equals(target, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当需要获取值，而方法可能抛出错误时，使用该方法重复获取.
     *
     * @param callable    获取值的方法体
     * @param repeatTimes 重复获取的次数，超出次数则返回null. 填入0则不限制获取次数，将不断尝试直至取到值. 填入0时请注意安全，避免堵塞主线程！
     * @return 返回需要获取的值
     */
    public static <T> T repeatIfError(TryCallable<T> callable, long repeatTimes) {
        return repeatIfError(callable, repeatTimes, 0);
        //
    }

    /**
     * 当需要获取值，而方法可能抛出错误时，使用该方法重复获取.
     *
     * @param callable    获取值的方法体
     * @param repeatTimes 重复获取的次数，超出次数则返回null. 填入0则不限制获取次数，将不断尝试直至取到值. <br> 填入0时请注意安全，避免堵塞主线程！
     * @param waitMillis  当出现错误时，等待多久(单位: 毫秒)再次执行. 若为0则不等待. <br> 填入非0时请注意安全，避免在主线程等待.
     * @return 返回需要获取的值
     */
    @Nullable
    public static <T> T repeatIfError(TryCallable<T> callable, long repeatTimes, long waitMillis) {
        if (repeatTimes == 0) {
            while (true) {
                try {
                    return callable.call();
                } catch (Throwable ignored) {
                    if (waitMillis > 0) {
                        try {
                            Thread.sleep(waitMillis);
                        } catch (InterruptedException ignored1) {
                            return null;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < repeatTimes; i++) {
                try {
                    return callable.call();
                } catch (Throwable ignored) {
                    if (waitMillis > 0) {
                        try {
                            Thread.sleep(waitMillis);
                        } catch (InterruptedException ignored1) {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 当需要执行方法，而方法有可能抛出错误时，使用该方法重复执行
     *
     * @param r           需要执行的方法体
     * @param repeatTimes 重复获取的次数，超出次数则返回null. 填入0则不限制获取次数，将不断尝试直至取到值. 填入0时请注意安全，避免堵塞主线程！
     */
    public static void repeatIfError(TryRunnable r, long repeatTimes) {
        repeatIfError(() -> {
            r.run();
            return null;
        }, repeatTimes);
    }

}
