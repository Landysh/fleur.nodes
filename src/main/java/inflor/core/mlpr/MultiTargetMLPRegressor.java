package inflor.core.mlpr;

import org.deeplearning4j.datasets.iterator.SamplingDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class MultiTargetMLPRegressor {
  
  int batchSize = 1000;
  int seed = 42; 
  int numEpochs = 10;
  double rate = 0.0015;
  private DataSet data;
  private MultiLayerNetwork model;

  public MultiTargetMLPRegressor() {

  }

  void setData(double[][] x, double[][] y){
    INDArray xArr = Nd4j.create(x);
    INDArray yArr = Nd4j.create(y);
    data = new DataSet(xArr, yArr);
  }
  
  void learn(double fraction) {
    
    SplitTestAndTrain stt = data.splitTestAndTrain(fraction);
    
    DataSet train = stt.getTrain();
    DataSet test = stt.getTest();
    DataSetIterator trainIterator = new SamplingDataSetIterator(train, batchSize, train.numExamples()/batchSize);
    DataSetIterator testIterator = new SamplingDataSetIterator(test, 1, test.numExamples());
    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .seed(seed)
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        .iterations(5)
        .weightInit(WeightInit.XAVIER)
        .learningRate(rate)
        .updater(Updater.NESTEROVS).momentum(0.98)
        .regularization(true).l2(rate * 0.005)
        .list()
        .layer(0, new DenseLayer.Builder() //create the first input layer.
                .nIn(train.getFeatureMatrix().columns())
                .nOut(50)
                .build())
        .layer(1, new DenseLayer.Builder() //create the second input layer
                .nIn(50)
                .nOut(10)
                .build())
        .layer(2, new OutputLayer.Builder(LossFunction.MSE)
                //.activation(Activations.SoftMax)
                .nIn(10)
                .nOut(2)
                .build())
        .pretrain(false).backprop(true)
        .build();
    model = new MultiLayerNetwork(conf);
    model.setListeners(new ScoreIterationListener(2));
    model.init();
    for (int i=0;i<numEpochs;i++){
      model.fit(trainIterator);
    }
    Evaluation eval = new Evaluation(test.getLabels().columns()); 
    while(testIterator.hasNext()){
        DataSet next = testIterator.next();
        INDArray output = model.output(next.getFeatureMatrix());
        eval.eval(next.getLabels(), output);
    }

    System.out.println(eval.stats());
  }
  
  INDArray infer(double[][] y){
    INDArray inferFeatures = Nd4j.create(y);
    DataSet inferDS = new DataSet();
    inferDS.setFeatures(inferFeatures);
    INDArray result = model.output(inferDS.getFeatureMatrix());
    System.out.println(result);
    return result;
  }
}
