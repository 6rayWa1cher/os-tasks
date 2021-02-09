package com.a6raywa1cher.ostasks.tsk1;

import lombok.SneakyThrows;

import java.util.*;
import java.util.stream.Collectors;

interface MultiplyMatrices {
    double[][] dot(double[][] a, double[][] b);

    default void assertMatrixSizes(double[][] a, double[][] b) throws IllegalArgumentException {
        if (a[0].length != b.length) throw new IllegalArgumentException("inconsistent matrices");
    }

    default double multiply(double[][] a, double[][] b, int rowToMult, int colToMult) {
        double[] row = a[rowToMult];
        double result = 0;
        for (int i = 0; i < row.length; i++) {
            result += row[i] * b[i][colToMult];
        }
        return result;
    }
}

class MultiplySequential implements MultiplyMatrices {
    @Override
    @SneakyThrows
    @Profile
    public double[][] dot(double[][] a, double[][] b) {
        assertMatrixSizes(a, b);
        final int rows = a.length;
        final int cols = b[0].length;
        double[][] out = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                out[i][j] = multiply(a, b, i, j);
            }
        }
        return out;
    }
}

class MultiplyThreading implements MultiplyMatrices {
    final int threads;

    public MultiplyThreading(int threads) {
        this.threads = threads;
    }

    @Override
    @Profile
    public double[][] dot(double[][] a, double[][] b) {
        assertMatrixSizes(a, b);
        final int rows = a.length;
        final int cols = b[0].length;
        final int cells = rows * cols;
        double[][] out = new double[rows][cols];

        int cellsPerThread = cells / threads;

        List<Thread> threadList = new ArrayList<>(threads);

        for (int t = 0; t < threads; t++) {
            boolean isLast = t == threads - 1;
            int cellsToCalc = isLast ? cells - cellsPerThread * (threads - 1) : cellsPerThread;
            int posFrom = t * cellsToCalc;
            int fromI = posFrom / cols;
            int fromJ = posFrom % cols;
            Runnable runnable = () -> {
//                System.out.println(Thread.currentThread().getName() + ": " + posFrom + "-" + (posFrom + cellsToCalc));
                for (int i = fromI; i < rows; i++) {
                    for (int j = i == 0 ? fromJ : 0; j < cols; j++) {
                        out[i][j] = multiply(a, b, i, j);
                    }
                }
            };
            threadList.add(new Thread(runnable, "MMT" + t));
        }

        threadList.forEach(Thread::start);

        threadList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        return out;
    }
}

public class MatrixMultiply {
    private static double[][] genMatrix(int rows, int cols) {
        double[][] out = new double[rows][cols];
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                out[i][j] = random.nextDouble();
            }
        }
        return out;
    }

    private static String matrixToString(double[][] mat) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mat.length; i++) {
            sb.append(
                    Arrays.stream(mat[i])
                            .mapToObj(Double::toString)
                            .collect(Collectors.joining(" "))
            );
            if (i != mat.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        double[][] a = genMatrix(100, 100);
        double[][] b = genMatrix(100, 100);
        int threads = 2;
        Profiler profiler = Profiler.getInstance();
        Map<String, MultiplyMatrices> methods = new LinkedHashMap<>();
        methods.put("multiplySequential", new MultiplySequential());
        methods.put("multiplyThreading", new MultiplyThreading(threads));
        methods.replaceAll((s, mm) -> profiler.constructProfiler(mm));
        for (int i = 0; i < 5; i++) {
            for (var method : methods.entrySet()) {
                MultiplyMatrices mm = method.getValue();
                mm.dot(a, b);
                Profiler.ProfilingResults results = profiler.getProfilingResults(mm, "dot");
                System.out.println(method.getKey() + ": " + results.getLastInvocationTimeString());
            }
        }
    }
}
