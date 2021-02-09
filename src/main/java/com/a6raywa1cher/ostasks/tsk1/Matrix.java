package com.a6raywa1cher.ostasks.tsk1;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Getter
public class Matrix {
    public static final double EPSILON = 0.00000001;
    private final int rows;
    private final int cols;
    private final double[][] mat;

    public Matrix(double[][] mat) {
        this.mat = mat;
        this.rows = mat.length;
        this.cols = mat[0].length;
    }

    public static Matrix genMatrix(int rows, int cols) {
        double[][] out = new double[rows][cols];
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                out[i][j] = random.nextDouble();
            }
        }
        return new Matrix(out);
    }

    private static boolean bodyEquals(double[][] a, double[][] b) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > EPSILON) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Matrix matrix = (Matrix) o;
        return rows == matrix.rows && cols == matrix.cols && bodyEquals(mat, matrix.mat);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rows, cols);
        result = 31 * result + Arrays.hashCode(mat);
        return result;
    }

    @Override
    public String toString() {
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
}
