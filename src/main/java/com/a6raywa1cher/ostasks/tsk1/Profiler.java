package com.a6raywa1cher.ostasks.tsk1;

import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Profiler {
    private static final boolean NANOSECOND = true;
    private static final Profiler instance = new Profiler();
    private final Map<Object, Map<Method, ProfilingResults>> map = new HashMap<>();
    private final Map<Object, Object> proxyToReal = new HashMap<>();

    public static Profiler getInstance() {
        return instance;
    }

    private static Method getMethod(Object o, String name) {
        return Arrays.stream(o.getClass().getMethods())
                .filter(m -> m.getName().equals(name))
                .findAny().orElseThrow();
    }

    public ProfilingResults getProfilingResults(Object proxy, String method) {
        Object real = proxyToReal.get(proxy);
        Map<Method, ProfilingResults> profilingResultsMap = map.get(real);
        return profilingResultsMap.computeIfAbsent(getMethod(real, method), m -> new ProfilingResults(m, false, NANOSECOND));
    }

    public <T> T constructProfiler(T obj) {
        return constructProfiler(obj, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T constructProfiler(T obj, boolean print) {
        Class<T> tClass = (Class<T>) obj.getClass();
        Map<Method, ProfilingResults> profilingResultsMap = new HashMap<>();
        T proxy = (T) Proxy.newProxyInstance(tClass.getClassLoader(), tClass.getInterfaces(),
                (proxy1, method, args) -> {
                    Method realMethod = obj.getClass().getMethod(method.getName(), method.getParameterTypes());
                    if (realMethod.isAnnotationPresent(Profile.class) || profilingResultsMap.containsKey(realMethod)) {
                        ProfilingResults profilingResults =
                                profilingResultsMap.computeIfAbsent(realMethod, m -> new ProfilingResults(m, print, NANOSECOND));
                        long start = profilingResults.isNano() ? System.nanoTime() : System.currentTimeMillis();
                        Object output = realMethod.invoke(obj, args);
                        long end = profilingResults.isNano() ? System.nanoTime() : System.currentTimeMillis();
                        long delta = end - start;
                        profilingResults.processInvocation(delta);
                        return output;
                    } else {
                        return realMethod.invoke(obj, args);
                    }
                }
        );
        map.put(proxy, profilingResultsMap);
        map.put(obj, profilingResultsMap);
        proxyToReal.put(proxy, obj);
        return proxy;
    }

    @Data
    public static final class ProfilingResults {
        private final Method method;
        private boolean print;
        private long lastInvocationTime = -1;
        private long totalInvocations = 0;
        private long totalInvocationsTime = 0;
        private boolean isNano;

        public ProfilingResults(Method method, boolean print, boolean isNano) {
            this.method = method;
            this.print = print;
            this.isNano = isNano;
        }

        public String getLastInvocationTimeString() {
            return lastInvocationTime + (isNano ? "ns" : "ms");
        }

        protected void processInvocation(long time) {
            lastInvocationTime = time;
            totalInvocationsTime += time;
            totalInvocations++;
            if (print) {
                System.out.println(method.getName() + ": " + getLastInvocationTimeString());
            }
        }
    }
}
