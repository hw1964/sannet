# SANNet - Neural Network Framework

## Background
SANNet is an artificial neural network framework that provides functionalities to build multi-layer neural networks. It has been born from long term interest towards artificial neural networks and curiosity to understand their inner workings.

Framework's primary components are neural network instance, input layer, variable number of hidden layers and output layer. Neural network instance and layers run in their dedicated threads to enable concurrency between multiple neural network instances.

SANNet is written on Java and has been built from ground up starting from implementation of matrix library and functions. 

## Layers, activation and loss functions
Framework provides feedforward (with option for additional connections to earlier layers) / dense (with option for additional connections to earlier layers) / activation and flattening layers, unidirectional and bidirectional recurrent layers (simple recurrent layer, LSTM layer, Graves LSTM layer, Peephole LSTM layer, GRU layer, Minimal GRU layer) and convolutional layers (convolution / crosscorrelation / Winograd convolution / depth-wise separable convolutional / depth-wise separable crosscorrelation layer, max / average / random / cyclic pooling layer). All layers are executed as dynamically constructed procedures and expressions that have built-in automatic gradient for backpropagation.

Framework supports multiple layer activation functions and loss functions for output layer. Multiple parallel loss functions with single splitted output can be used at output layer. Additionally there are multiple initialization methods for weight parameters like Xavier / He / LeCun uniform and normal initializations.

## Reinforcement learning
Framework implements deep reinforcement learning agent that communicates with environment through defined interface. Deep agent learns via experience by taking actions through environment states and receiving rewards.

Framework supports value based (Deep Q Learning, Double Deep Q Learning, SARSA) with optional Dueling Layer, policy based (Actor Critic, Proximal Policy Optimization, Discrete Soft Actor Critic, REINFORCE, Deep Deterministic Policy Gradient) and Monte Carlo Tree Search (MCTS) based reinforcement learning algorithms. It supports online and replay buffering. It has support for multiple policies (greedy policy, epsilon greedy policy, noisy next best policy, sampled policy).

## Optimization
Framework implements most typically used optimization methods starting from basic vanilla gradient descent up to more sofisticated parameter optimization methods such as Adam and AMSGrad.

## Regularization
Framework provides following regularization layers: drop-out, gradient clipping, L1 / L2 / Lp regularization and weight noising. Lp regularization is an experimental method and mathematically direct extension of L1 / L2 methods. Early stopping is implemented as function on neural network level.

## Normalization
Framework supports following normalization layers: batch normalization, layer normalization and weight normalization.

## Metrics
Framework provides accuracy metrics for **regression**. Additionally it provides basic metrics for **classification** such as accuracy, precision, recall, specificity and F1 score and calculates confusion matrix as needed.

## Utilities
Framework provides libraries to read inputs from CSV, text and MIDI files, normalize, split, encode and decode data. It also has persistence support to serialize trained neural network instances into file and restore trained neural network instances from file for later use.

All feedback is welcome.
