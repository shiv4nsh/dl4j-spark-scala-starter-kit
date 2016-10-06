package com.knoldus.cnn

/**
  * Created by shivansh on 28/7/16.
  */

import org.apache.spark.{SparkContext, SparkConf}
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, Updater, NeuralNetConfiguration}
import org.deeplearning4j.nn.conf.layers.{OutputLayer, DenseLayer, SubsamplingLayer, ConvolutionLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.slf4j.LoggerFactory
import org.deeplearning4j.spark.api.TrainingMaster

class MnsitExample {

  val log = LoggerFactory.getLogger(this.getClass)
  val sparkConf = new SparkConf().setMaster("local[*]").setAppName("MNSITExample")
  val sc = new SparkContext(sparkConf)
  val examplesPerDataSetObject = 32
  val mnistTrain = new MnistDataSetIterator(32, true, 12345)
  val mnistTest = new MnistDataSetIterator(32, false, 12345)
  val trainData = scala.collection.mutable.ArrayBuffer[DataSet]()
  val testData = scala.collection.mutable.ArrayBuffer[DataSet]()
  while (mnistTrain.hasNext()) trainData.+=:(mnistTrain.next())
  while (mnistTest.hasNext()) testData.+=:(mnistTest.next())

  val trainRDD = sc.parallelize(trainData)
  val testRDD = sc.parallelize(testData)

  val NCHANNELS = 1
  val OUTPUTNUMBER = 10
  val ITERATIONS = 1
  val SEED = 123
  val NEPOCHS = 5

  log.info("Build model....")

  val builder = new NeuralNetConfiguration.Builder()
    .seed(SEED)
    .iterations(ITERATIONS)
    .regularization(true).l2(0.0005)
    .learningRate(0.1)
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .updater(Updater.ADAGRAD)
    .list()
    .layer(0, new ConvolutionLayer.Builder(5, 5).nIn(NCHANNELS).stride(1, 1).nOut(20).weightInit(WeightInit.XAVIER).activation("relu").build())
    .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).build())
    .layer(2, new ConvolutionLayer.Builder(5, 5).nIn(20).nOut(50).stride(2, 2).weightInit(WeightInit.XAVIER).activation("relu").build())
    .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).build())
    .layer(4, new DenseLayer.Builder().activation("relu").weightInit(WeightInit.XAVIER).nOut(200).build())
    .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nOut(OUTPUTNUMBER).weightInit(WeightInit.XAVIER).activation("softmax").build())
    .backprop(true).pretrain(false)

  val convolutionLayer: ConvolutionLayerSetup = new ConvolutionLayerSetup(builder, 28, 28, 1)

  val conf: MultiLayerConfiguration = builder.build()
  val net: MultiLayerNetwork = new MultiLayerNetwork(conf)
  net.init()
  val tm: ParameterAveragingTrainingMaster = new ParameterAveragingTrainingMaster.Builder(examplesPerDataSetObject)
    .workerPrefetchNumBatches(0)
    .saveUpdater(true)
    .averagingFrequency(5) //Do 5 minibatch fit operations per worker, then average and redistribute parameters
    .batchSizePerWorker(examplesPerDataSetObject) //Number of examples that each worker uses per fit operation
    .build()

  val sparkNetwork = new SparkDl4jMultiLayer(sc, net, tm)
  log.info("--- Starting network training ---")
  (1 to NEPOCHS).toList.map { i =>
    sparkNetwork.fit(trainRDD)
    log.info("----- Epoch " + i + " complete -----")

    //Evaluate using Spark:
    val evaluation = sparkNetwork.evaluate(testRDD)
    log.info(evaluation.stats())
  }

  log.info("****************Example finished********************")

}

object MnsitExample extends MnsitExample with App
