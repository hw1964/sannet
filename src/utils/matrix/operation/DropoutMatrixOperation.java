/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2023 Simo Aaltonen
 */

package utils.matrix.operation;

import utils.matrix.Matrix;
import utils.matrix.MatrixException;

/**
 * Implements inverted drop out.<br>
 * Function selectively masks out certain percentage of node governed by parameter probability during training phase.<br>
 * During training phase it also compensates all remaining inputs by dividing by probability.<br>
 *
 */
public class DropoutMatrixOperation extends AbstractMatrixOperation {

    /**
     * Probability of dropout.
     *
     */
    private final double probability;

    /**
     * Constructor for drop out matrix operation.
     *
     * @param rows number of rows for operation.
     * @param columns number of columns for operation.
     * @param depth depth for operation.
     * @param probability probability
     */
    public DropoutMatrixOperation(int rows, int columns, int depth, double probability) {
        super(rows, columns, depth, true);
        this.probability = probability;
    }

    /**
     * Applies matrix operation.
     *
     * @param first first matrix.
     * @param inplace if true operation is applied in place otherwise result is returned as new matrix.
     * @return result of operation.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public Matrix apply(Matrix first, boolean inplace) throws MatrixException {
        Matrix result = first;
        if (inplace) first.multiplyBy(1 / probability);
        else result = first.multiply(1 / probability);
        result.setMask();
        result.getMask().setProbability(probability);
        result.getMask().maskRowByProbability();
        return result;
    }

    /**
     * Applies operation.<br>
     * Ignores masking of other matrix.<br>
     *
     * @param row    current row.
     * @param column current column.
     * @param depth  current depth.
     * @param value  current value.
     * @param result result matrix.
     */
    public void apply(int row, int column, int depth, double value, Matrix result) {
    }

}
