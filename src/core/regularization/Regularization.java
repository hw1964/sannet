/********************************************************
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2020 Simo Aaltonen
 *
 ********************************************************/

package core.regularization;

import utils.DynamicParam;
import utils.DynamicParamException;
import utils.Sequence;
import utils.matrix.Matrix;
import utils.matrix.MatrixException;

/**
 * Interface for regularization functions.
 *
 */
public interface Regularization {

    /**
     * Sets parameters for regularizer.
     *
     * @param params parameters for regularizer.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    void setParams(DynamicParam params) throws DynamicParamException;

    /**
     * Resets regularizer state.
     *
     */
    void reset();

    /**
     * Indicates to regularizer if neural network is in training mode.
     *
     * @param isTraining if true neural network is in state otherwise false.
     */
    void setTraining(boolean isTraining);

    /**
     * Sets current mini batch size.
     *
     * @param miniBatchSize current mini batch size.
     */
    void setMiniBatchSize(int miniBatchSize);

    /**
     * Executes regularization method for forward step.<br>
     *
     * @param sequence input sequence.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    void forward(Sequence sequence) throws MatrixException;

    /**
     * Executes regularization method for forward step.<br>
     *
     * @param W weight matrix.
     */
    void forward(Matrix W);

    /**
     * Cumulates error from regularization. Mainly from L1 / L2 / Lp regularization.
     *
     * @param W weight matrix.
     * @return cumulated error from regularization.
     */
    double error(Matrix W);

    /**
     * Executes regularization method for backward phase at pre in.
     *
     * @param W weight matrix.
     * @param dWSum gradient sum of weight.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    void backward(Matrix W, Matrix dWSum) throws MatrixException;

}

