/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2023 Simo Aaltonen
 */

package utils.matrix.operation;

/**
 * Implements max pooling matrix operation.
 *
 */
public class MaxPoolMatrixOperation extends AbstractPositionalPoolingMatrixOperation {

    /**
     * Current max row.
     *
     */
    private int maxRow;

    /**
     * Current max column.
     *
     */
    private int maxColumn;

    /**
     * Max value
     *
     */
    private transient double maxValue = Double.NEGATIVE_INFINITY;

    /**
     * Constructor for max pooling matrix operation.
     *
     * @param rows number of rows for operation.
     * @param columns number of columns for operation.
     * @param depth depth for operation.
     * @param filterRowSize filter size in rows.
     * @param filterColumnSize filter size in columns.
     * @param dilation dilation step
     * @param stride stride step
     */
    public MaxPoolMatrixOperation(int rows, int columns, int depth, int filterRowSize, int filterColumnSize, int dilation, int stride) {
        super(rows, columns, depth, filterRowSize, filterColumnSize, dilation, stride);
    }

    /**
     * Applies convolution operation.
     *
     * @param row current row.
     * @param column current column.
     * @param depth current depth.
     * @param inputRow current input row.
     * @param inputColumn current input column.
     * @param filterRow current filter row.
     * @param filterColumn current filter column.
     * @param value current value.
     */
    protected void applyOperation(int row, int column, int depth, int inputRow, int inputColumn, int filterRow, int filterColumn, double value) {
        double filterValue = getTargetMatrix().getValue(inputRow, inputColumn, depth);
        if (maxValue < filterValue) {
            maxValue = filterValue;
            maxRow = inputRow;
            maxColumn = inputColumn;
        }
    }

    /**
     * Applies masked convolution operation.
     *
     * @param row current row.
     * @param column current column.
     * @param depth current depth.
     * @param inputRow current input row.
     * @param inputColumn current input column.
     * @param filterRow current filter row.
     * @param filterColumn current filter column.
     * @param value current value.
     */
    protected void applyMaskOperation(int row, int column, int depth, int inputRow, int inputColumn, int filterRow, int filterColumn, double value) {
        if (!hasMaskAt(inputRow, inputColumn, depth, getTargetMatrix())) {
            applyOperation(row, column, depth, inputRow, inputColumn, filterRow, filterColumn, value);
        }
    }

    /**
     * Starts convolutional operation
     *
     * @param row current row.
     * @param column current column.
     * @param depth current depth.
     */
    protected void startOperation(int row, int column, int depth) {
        maxRow = -1;
        maxColumn = -1;
        maxValue = Double.NEGATIVE_INFINITY;
    }

    /**
     * Finishes convolutional operation
     *
     * @param row current row.
     * @param column current column.
     * @param depth current depth.
     */
    protected void finishOperation(int row, int column, int depth) {
        getResult().setValue(row, column, depth, maxValue);

        super.finishOperation(row, column, depth);
    }

    /**
     * Returns input row.
     *
     * @return input row.
     */
    protected int getInputRow() {
        return maxRow;
    }

    /**
     * Returns input column.
     *
     * @return input column.
     */
    protected int getInputColumn() {
        return maxColumn;
    }

}
