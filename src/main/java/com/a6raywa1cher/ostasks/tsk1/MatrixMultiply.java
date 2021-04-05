package com.a6raywa1cher.ostasks.tsk1;

import lombok.SneakyThrows;

import java.util.*;

import static com.a6raywa1cher.ostasks.tsk1.Matrix.genMatrix;

interface MultiplyMatrices {
    Matrix dot(Matrix a, Matrix b);

    default void assertMatrixSizes(Matrix a, Matrix b) throws IllegalArgumentException {
        if (a.getCols() != b.getRows()) throw new IllegalArgumentException("inconsistent matrices");
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
    public Matrix dot(Matrix a, Matrix b) {
        assertMatrixSizes(a, b);
        final int cols = b.getCols();
        final int rows = a.getRows();
        double[][] out = new double[cols][rows];
        double[][] aMat = a.getMat();
        double[][] bMat = b.getMat();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                out[i][j] = multiply(aMat, bMat, i, j);
            }
        }
        return new Matrix(out);
    }
}

class MultiplyThreading implements MultiplyMatrices {
    final int threads;

    public MultiplyThreading(int threads) {
        this.threads = threads;
    }

    @Override
    @Profile
    public Matrix dot(Matrix a, Matrix b) {
        assertMatrixSizes(a, b);
        final int cols = b.getCols();
        final int rows = a.getRows();
        final int cells = rows * cols;
        double[][] out = new double[rows][cols];
        double[][] aMat = a.getMat();
        double[][] bMat = b.getMat();

        int threads = Math.min(rows*cols, this.threads);

        int cellsPerThread = cells / threads;

        List<Thread> threadList = new ArrayList<>(threads);

        for (int t = 0; t < threads; t++) {
            boolean isLast = t == threads - 1;
            int cellsToCalc = isLast ? cells - cellsPerThread * (threads - 1) : cellsPerThread;
            int posFrom = t * cellsPerThread;
            int fromI = posFrom / cols;
            int fromJ = posFrom % cols;
            int posTo = posFrom + cellsToCalc;

            Runnable runnable = () -> {
//                System.out.println(Thread.currentThread().getName() + " " + fromI + " " + fromJ + " " + toI + " " + toJ);
                int i = fromI;
                int j = fromJ;
                while (i * cols + j != posTo) {
                    out[i][j] = multiply(aMat, bMat, i, j);
                    if (++j >= cols) {
                        i++;
                        j = 0;
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

        return new Matrix(out);
    }
}

public class MatrixMultiply {
    public static void main(String[] args) {
        Matrix a = genMatrix(150, 100);
        Matrix b = genMatrix(100, 150);
        int threads = 4;
        Profiler profiler = Profiler.getInstance();
        Map<String, MultiplyMatrices> methods = new LinkedHashMap<>();
        methods.put("multiplySequential", new MultiplySequential());
        methods.put("multiplyThreading", new MultiplyThreading(threads));
        methods.replaceAll((s, mm) -> profiler.constructProfiler(mm));
        for (int i = 0; i < 500; i++) {
            System.out.println("Test #" + (i + 1));
            List<Matrix> outputs = new ArrayList<>(methods.size());
            for (var method : methods.entrySet()) {
                MultiplyMatrices mm = method.getValue();
                outputs.add(mm.dot(a, b));
                Profiler.ProfilingResults results = profiler.getProfilingResults(mm, "dot");
                System.out.println(method.getKey() + ":\t" + results.getLastInvocationTimeString());
            }
            System.out.println(
                    outputs.stream().allMatch(m -> outputs.get(0).equals(m)) ?
                        "All contents equals" :
                        "Mismatch!"
            );
        }
        System.out.println("RESULTS");
        for (var method : methods.entrySet()) {
            MultiplyMatrices mm = method.getValue();
            Profiler.ProfilingResults results = profiler.getProfilingResults(mm, "dot");
            System.out.println(method.getKey() + ":\t" +
                "AVG=" + results.getTotalInvocationsTime() / results.getTotalInvocations());
        }
    }
}
